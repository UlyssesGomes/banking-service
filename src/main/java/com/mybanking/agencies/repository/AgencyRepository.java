package com.mybanking.agencies.repository;

import com.mybanking.agencies.domain.Agency;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AgencyRepository implements PanacheRepository<Agency> {

    @WithSession
    public Uni<Agency> findByCnpj(String cnpj) {
        return find("cnpj", cnpj).firstResult();
    }
}
