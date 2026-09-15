package com.mybanking.agencies.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybanking.agencies.domain.Agency;
import com.mybanking.agencies.domain.http.AgencyHttp;
import com.mybanking.agencies.domain.http.RegisterSituationEnum;
import com.mybanking.agencies.exception.AgencyNotActiveOrNotFoundException;
import com.mybanking.agencies.repository.AgencyRepository;
import com.mybanking.agencies.service.cache.RedisCacheService;
import com.mybanking.agencies.service.http.RegisterSituationHttpService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class AgencyService {

    private final AgencyRepository agenciaRepository;
    private final MeterRegistry meterRegistry;
    private final RedisCacheService redisCacheService;
    private final ObjectMapper objectMapper;

    AgencyService(AgencyRepository agenciaRepository, MeterRegistry meterRegistry, RedisCacheService redisCacheService) {
        this.agenciaRepository = agenciaRepository;
        this.meterRegistry = meterRegistry;
        this.redisCacheService = redisCacheService;
        this.objectMapper = new ObjectMapper();
    }

    @RestClient
    RegisterSituationHttpService situacaoCadastralHttpService;

    @WithTransaction // to do -> usando o hibernate sem panache ainda precisaria manter a transação aberta com o @WithTransaction
    public Uni<Void> register(Agency agencia) {
        Counter counter = this.meterRegistry.counter("agencia_nao_adicionada_count");
        return situacaoCadastralHttpService.searchByCnpj(agencia.getCnpj())
                .onItem()
                .ifNull().failWith(new AgencyNotActiveOrNotFoundException())
                .invoke(a -> Log.info("Agencia com CNPJ " + a.getCnpj() + " foi encontrada"))
                .invoke(t -> counter.increment())
                .onItem().transformToUni(agenciaHttpTransformada -> persistIfActive(agenciaHttpTransformada, agencia, counter));
    }

    private Uni<Void> persistIfActive(AgencyHttp agenciaHttp, Agency agencia, Counter counter) {
        if(agenciaHttp.getRegisterSituation().equals(RegisterSituationEnum.ACTIVE)) {
            return agenciaRepository.persist(agencia)
                    .invoke(t -> this.meterRegistry.counter("agencia_adicionada_count").increment()) // to do -> pesquisar se seria bloqueante e como resolver caso seja
                    .invoke(a -> Log.info("Agencia com CNPJ " + agencia.getCnpj() + " foi adicionada"))
                    .replaceWithVoid();
        } else {
            Log.info("Agencia com CNPJ " + agencia.getCnpj() + " não ativa"); // to do -> pesquisar aqui tb.
            counter.increment();
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
                return objectMapper.readValue(agency, Agency.class);
            } catch(Exception e) {
                return null;
            }
        });
    }

    private Uni<Agency> searchInDatabase(String key, Long id) {
        return agenciaRepository.findById(id).onItem().ifNotNull().call(agency -> {
            try {
                return redisCacheService.set(key, objectMapper.writeValueAsString(agency), 3600);
            } catch (Exception e) {
                return Uni.createFrom().failure(e);
            }
        });
    }

    @WithTransaction
    public Uni<Void> delete(Long id) {
        String key = "agency " + id;
        return agenciaRepository.deleteById(id)
                .call(b -> redisCacheService.delete(key))
                .invoke(a -> Log.info("A agência foi deletada")) // to do -> verificar como poderia usar log level
                .replaceWithVoid();
    }

    @WithTransaction
    public Uni<Void> update(Agency agency) {
        return agenciaRepository
                .update("name = ?1, companyName = ?2, cnpj = ?3 where id = ?4",
                        agency.getName(),
                        agency.getCompanyName(),
                        agency.getCnpj(),
                        agency.getId()
                ).invoke(a -> Log.info("A agência com CNPJ " + agency.getCnpj() + " foi alterada"))
                .replaceWithVoid();
    }
}