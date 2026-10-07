package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.publishers.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingViewAMQP;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingViewAMQPMapper;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.publishers.LendingEventsPublisher;
import pt.psoft.g1.psoftg1.shared.model.LendingEvents;

@Service
@RequiredArgsConstructor
public class LendingEventsRabbitmqPublisherImpl implements LendingEventsPublisher {

    @Autowired
    private RabbitTemplate template;
    @Autowired
    private DirectExchange direct;
    @Autowired
    private final LendingViewAMQPMapper lendingViewAMQPMapper;

    private int count = 0;

    @Override
    public LendingViewAMQP sendLendingCreated(Lending lending) {
        return sendLendingEvent(lending, 1L, LendingEvents.LENDING_CREATED);
    }

    @Override
    public LendingViewAMQP sendLendingUpdated(Lending lending, Long currentVersion) {
        return sendLendingEvent(lending, currentVersion, LendingEvents.LENDING_UPDATED);
    }

    @Override
    public LendingViewAMQP sendLendingDeleted(Lending lending, Long currentVersion) {
        return sendLendingEvent(lending, currentVersion, LendingEvents.LENDING_DELETED);
    }

    private LendingViewAMQP sendLendingEvent(Lending lending, Long currentVersion, String lendingEventType) {

        System.out.println("Send Lending event to AMQP Broker: " + lending.getLendingNumber());

        try {
            LendingViewAMQP lendingViewAMQP = lendingViewAMQPMapper.toLendingViewAMQP(lending);
            // Se precisares de version, adiciona o campo ao DTO
            // lendingViewAMQP.setVersion(currentVersion);

            ObjectMapper objectMapper = new ObjectMapper();
            String lendingViewAMQPinString = objectMapper.writeValueAsString(lendingViewAMQP);

            this.template.convertAndSend(direct.getName(), lendingEventType, lendingViewAMQPinString);

            return lendingViewAMQP;
        }
        catch( Exception ex ) {
            System.out.println(" [x] Exception sending lending event: '" + ex.getMessage() + "'");

            return null;
        }
    }
}