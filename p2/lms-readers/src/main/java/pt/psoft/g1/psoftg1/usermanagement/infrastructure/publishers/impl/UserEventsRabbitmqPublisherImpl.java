package pt.psoft.g1.psoftg1.usermanagement.infrastructure.publishers.impl;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.shared.model.UserEvents;
import pt.psoft.g1.psoftg1.usermanagement.api.UserViewAMQP;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.publishers.UserEventsPublisher;

@Service
@RequiredArgsConstructor
public class UserEventsRabbitmqPublisherImpl implements UserEventsPublisher {

    @Autowired
    private RabbitTemplate template;

    @Autowired
    private DirectExchange direct;

    @Override
    public UserViewAMQP sendUserCreated(User user) {
        return sendUserEvent(user, 1L, UserEvents.USER_CREATED);
    }

    @Override
    public UserViewAMQP sendUserUpdated(User user, Long currentVersion) {
        return sendUserEvent(user, currentVersion, UserEvents.USER_UPDATED);
    }

    @Override
    public UserViewAMQP sendUserDeleted(User user, Long currentVersion) {
        return sendUserEvent(user, currentVersion, UserEvents.USER_DELETED);
    }

    private UserViewAMQP sendUserEvent(User user, Long currentVersion, String userEventType) {
        System.out.println("Send User event to AMQP Broker: " + user.getUsername() + " [" + userEventType + "]");

        try {
            // Convert User to UserViewAMQP
            UserViewAMQP userViewAMQP = new UserViewAMQP(
                user.getId(),
                user.getUsername(),
                user.getName(),
                currentVersion
            );

            ObjectMapper objectMapper = new ObjectMapper();
            String userViewAMQPinString = objectMapper.writeValueAsString(userViewAMQP);

            this.template.convertAndSend(direct.getName(), userEventType, userViewAMQPinString);

            System.out.println(" [✓] User event sent successfully: " + user.getUsername());
            return userViewAMQP;
        } catch (Exception ex) {
            System.out.println(" [x] Exception sending user event: '" + ex.getMessage() + "'");
            return null;
        }
    }
}
