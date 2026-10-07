package pt.psoft.g1.psoftg1.usermanagement.publishers;

import pt.psoft.g1.psoftg1.usermanagement.api.UserViewAMQP;
import pt.psoft.g1.psoftg1.usermanagement.model.User;

/**
 * Interface para publicação de eventos de User via AMQP
 */
public interface UserEventsPublisher {

    UserViewAMQP sendUserCreated(User user);

    UserViewAMQP sendUserUpdated(User user, Long currentVersion);

    UserViewAMQP sendUserDeleted(User user, Long currentVersion);
}
