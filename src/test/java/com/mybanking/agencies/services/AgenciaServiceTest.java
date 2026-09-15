package com.mybanking.agencies.services;

import com.mybanking.agencies.domain.Agency;
import com.mybanking.agencies.domain.Address;
import com.mybanking.agencies.domain.http.AgencyHttp;
import com.mybanking.agencies.exception.AgencyNotActiveOrNotFoundException;
import com.mybanking.agencies.repository.AgencyRepository;
import com.mybanking.agencies.service.AgencyService;
import com.mybanking.agencies.service.http.RegisterSituationHttpService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class AgenciaServiceTest {

    @InjectMock
    @RestClient
    private RegisterSituationHttpService situacaoCadastralHttpService;

    @InjectMock
    private AgencyRepository agenciaRepository;

    @Inject
    private AgencyService agenciaService;

    @Test
    public void deveNaoCadastrarQuandoClientRetornarNull() {
        Agency agencia = criarAgencia();

        Vertx.vertx().runOnContext(r -> {
            Mockito.when(situacaoCadastralHttpService.searchByCnpj("123")).thenReturn(Uni.createFrom().nullItem());

            Assertions.assertThrows(AgencyNotActiveOrNotFoundException.class, () -> agenciaService.register(agencia));

            Mockito.verify(agenciaRepository, Mockito.never()).persist(agencia);
        });
    }

    @Test
    public void deveCadastrarQuandoClientRetornarSituacaoCadastralAtivo() {
        Agency agencia = criarAgencia();

        Vertx.vertx().runOnContext(r -> {
            Mockito.when(situacaoCadastralHttpService.searchByCnpj("123")).thenReturn(criarAgenciaHttp());

            agenciaService.register(agencia);

            Mockito.verify(agenciaRepository).persist(agencia);
        });
    }

    private Agency criarAgencia() {
        Address address = new Address(1L, "Rua de teste", "Logradouro de teste", "Complemento de teste", 1);
        return new Agency(1L, "Agencia Teste", "Razao social da Agencia Teste", "123", address);
    }

    private Uni<AgencyHttp> criarAgenciaHttp() {
        return Uni.createFrom().item(new AgencyHttp("Agencia Teste", "Razao social da Agencia Teste", "123", "ATIVO"));
    }
}
