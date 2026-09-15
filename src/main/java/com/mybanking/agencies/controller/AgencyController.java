package com.mybanking.agencies.controller;

import com.mybanking.agencies.domain.Agency;
import com.mybanking.agencies.service.AgencyService;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/agencies")
public class AgencyController {

    private final AgencyService agenciaService;

    AgencyController(AgencyService agenciaService) {
        this.agenciaService = agenciaService;
    }

    @POST
    @NonBlocking
    public Uni<RestResponse<Void>> create(Agency agencia, @Context UriInfo uriInfo) {
        return this.agenciaService
                .register(agencia).replaceWith(RestResponse.created(uriInfo.getAbsolutePathBuilder().build()));
    }

    @GET
    @Path("{id}")
    @NonBlocking
    public Uni<RestResponse<Agency>> searchById(Long id) {
        return this.agenciaService.searchById(id).map(RestResponse::ok);
    }

    @DELETE
    @Path("{id}")
    @NonBlocking
    public Uni<RestResponse<Void>> delete(Long id) {
        return this.agenciaService.delete(id).replaceWith(RestResponse.ok());
    }

    @PUT
    @NonBlocking
    public Uni<RestResponse<Void>> update(Agency agencia) {
        return this.agenciaService.update(agencia).replaceWith(RestResponse.ok());
    }
}
