package com.mybanking.agencies.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybanking.agencies.domain.Agency;
import com.mybanking.agencies.domain.http.AgencyHttp;
import com.mybanking.agencies.domain.http.RegisterSituationEnum;
import com.mybanking.agencies.enums.MetersEnums;
import com.mybanking.agencies.exception.AgencyNotActiveOrNotFoundException;
import com.mybanking.agencies.repository.AgencyRepository;
import com.mybanking.agencies.service.cache.RedisCacheService;
import com.mybanking.agencies.service.http.RegisterSituationHttpService;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final MeterRegistry meterRegistry;
    private final RedisCacheService redisCacheService;
    private final ObjectMapper objectMapper;
    private final Tracer tracer;

    AgencyService(AgencyRepository agencyRepository, MeterRegistry meterRegistry, RedisCacheService redisCacheService, Tracer tracer) {
        this.agencyRepository = agencyRepository;
        this.meterRegistry = meterRegistry;
        this.redisCacheService = redisCacheService;
        this.tracer = tracer;
        this.objectMapper = new ObjectMapper();
    }

    @RestClient
    RegisterSituationHttpService registerSituationHttpService;

    @WithTransaction
    public Uni<Void> register(Agency agency) {
        Span span = tracer.spanBuilder("agencyRegister").startSpan();
        span.setAttribute("agency.cnpj", agency.getCnpj());
        return registerSituationHttpService.searchByCnpj(agency.getCnpj())
                .onItem()
                .ifNull().failWith(() -> {
                    this.meterRegistry.counter(MetersEnums.AGENCY_NOT_ADDED_COUNT.getValue()).increment();
                    Log.error("Agency with CNPJ " + agency.getCnpj() + " is inactive.");
                    return new AgencyNotActiveOrNotFoundException();
                })
                .invoke(agencyHttp -> {
                    try(Scope scope = span.makeCurrent()) {
                        span.addEvent("Agency Founded");
                        span.setAttribute("agency.founded", agencyHttp.getCnpj());
                    } finally {
                        span.end();
                    }
                })
                .invoke(a -> Log.info("Agency with CNPJ " + a.getCnpj() + " was founded."))
                .onItem().transformToUni(agencyHTTPTransformed -> persistIfActive(agencyHTTPTransformed, agency));
    }

    private Uni<Void> persistIfActive(AgencyHttp agencyHttp, Agency agency) {
        if(agencyHttp.getRegisterSituation().equals(RegisterSituationEnum.ACTIVE)) {
            return agencyRepository.persist(agency)
                    .invoke(t -> this.meterRegistry.counter(MetersEnums.AGENCY_ADDED_COUNT.getValue()).increment()) // to do -> pesquisar se seria bloqueante e como resolver caso seja
                    .invoke(a -> Log.info("Agency with CNPJ " + agency.getCnpj() + " was added."))
                    .replaceWithVoid();
        } else {
            Log.error("Agency with CNPJ " + agency.getCnpj() + " is inactive");
            this.meterRegistry.counter(MetersEnums.AGENCY_NOT_ADDED_COUNT.getValue()).increment();
            return Uni.createFrom().failure(new AgencyNotActiveOrNotFoundException());
        }
    }

    @WithSession
    public Uni<Agency> searchById(Long id) {
        String key = "agency " + id;
        return searchInCache(key).onItem().ifNull().switchTo(searchInDatabase(key, id));
    }

    private Uni<Agency> searchInCache(String key) {
        return redisCacheService.get(key).onItem().ifNotNull().transform(agency -> {
            try {
                Log.info("Agency cache hit.");
                Span.current().setAttribute("cache.hit", "TRUE");
                meterRegistry.counter(MetersEnums.CACHE_HIT.getValue()).increment();
                return objectMapper.readValue(agency, Agency.class);
            } catch(Exception e) {
                return null;
            }
        });
    }

    private Uni<Agency> searchInDatabase(String key, Long id) {
        return agencyRepository.findById(id).onItem().ifNotNull().call(agency -> {
            try {
                Log.info("Agency search in database.");
                Span.current().setAttribute("cache.hit", "FALSE");
                meterRegistry.counter(MetersEnums.CACHE_MISSED.getValue()).increment();
                return redisCacheService.set(key, objectMapper.writeValueAsString(agency), 3600);
            } catch (Exception e) {
                return Uni.createFrom().failure(e);
            }
        });
    }

    @WithTransaction
    public Uni<Void> delete(Long id) {
        String key = "agency " + id;
        return agencyRepository.deleteById(id)
                .call(b -> redisCacheService.delete(key))
                .invoke(a -> Log.info("Agency " + id + " deleted."))
                .replaceWithVoid();
    }

    @WithTransaction
    public Uni<Void> update(Agency agency) {
        return agencyRepository
                .update("name = ?1, companyName = ?2, cnpj = ?3 where id = ?4",
                        agency.getName(),
                        agency.getCompanyName(),
                        agency.getCnpj(),
                        agency.getId()
                ).invoke(a -> Log.info("Agency with CNPJ " + agency.getCnpj() + " was changed."))
                .replaceWithVoid();
    }
}