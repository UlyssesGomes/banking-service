package com.mybanking.agencies.service.messaging;

import br.com.mybanking.Agency;
import com.mybanking.agencies.domain.messaging.AgencyMessage;
import com.mybanking.agencies.repository.AgencyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class RemoveAgencyService {

    private final ObjectMapper objectMapper;
    private final AgencyRepository agencyRepository;

    public RemoveAgencyService(AgencyRepository agencyRepository) {
        this.agencyRepository = agencyRepository;
        objectMapper = new ObjectMapper();
    }

    @WithTransaction
    @Incoming("remove-agency-channel")
    public Uni<Void> consumeMessage(Agency message) {
        try {
            Log.info(message);
            //AgenciaMessage agenciaMessage = objectMapper.readValue(message, AgenciaMessage.class);
            AgencyMessage agencyMessage =
                    new AgencyMessage(message.getName(),
                            message.getCompanyName(),
                            message.getCnpj(),
                            message.getRegisterSituation());
            return agencyRepository.findByCnpj(agencyMessage.getCnpj())
                    .onItem().ifNotNull().transformToUni(agency ->
                        agencyRepository.deleteById(agency.getId())).replaceWithVoid();
        } catch (Exception e) {
            Log.error(e.getMessage());
            return Uni.createFrom().failure(e);
        }
    }
}
