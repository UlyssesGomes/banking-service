package com.mybanking.agencies.service.http;

import com.mybanking.agencies.domain.http.AgencyHttp;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/register-situation")
@RegisterRestClient(configKey = "register-situation-api")
public interface RegisterSituationHttpService {

    @GET
    @Path("{cnpj}")
    Uni<AgencyHttp> searchByCnpj(String cnpj);
}
