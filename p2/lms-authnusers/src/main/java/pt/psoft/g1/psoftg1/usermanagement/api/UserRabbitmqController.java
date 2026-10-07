package pt.psoft.g1.psoftg1.usermanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;

import java.nio.charset.StandardCharsets;

/**
 * Controller para receber eventos de Users via AMQP
 * Sincroniza dados de users criados por outros microserviços
 */
@Service
@RequiredArgsConstructor
public class UserRabbitmqController {

    private final UserService userService;

    @RabbitListener(queues = "#{autoDeleteQueue_User_Created.name}")
    public void receiveUserCreatedMsg(Message msg) {
        try {
            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);

            ObjectMapper objectMapper = new ObjectMapper();
            UserViewAMQP userViewAMQP = objectMapper.readValue(jsonReceived, UserViewAMQP.class);

            System.out.println(" [x] AuthnUsers received User Created by AMQP: " + userViewAMQP.getUsername());
            try {
                userService.createFromAMQP(userViewAMQP);
                System.out.println(" [✓] New user synchronized in AuthnUsers: " + userViewAMQP.getUsername());
            } catch (Exception e) {
                System.out.println(" [x] User already exists in AuthnUsers: " + e.getMessage());
            }
        } catch (Exception ex) {
            System.out.println(" [x] Exception receiving user event from AMQP: '" + ex.getMessage() + "'");
        }
    }

    @RabbitListener(queues = "#{autoDeleteQueue_User_Updated.name}")
    public void receiveUserUpdatedMsg(Message msg) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            UserViewAMQP userViewAMQP = objectMapper.readValue(jsonReceived, UserViewAMQP.class);

            System.out.println(" [x] AuthnUsers received User Updated by AMQP: " + userViewAMQP.getUsername());
            try {
                userService.updateFromAMQP(userViewAMQP);
                System.out.println(" [✓] User updated in AuthnUsers: " + userViewAMQP.getUsername());
            } catch (Exception e) {
                System.out.println(" [x] Error updating user in AuthnUsers: " + e.getMessage());
            }
        } catch (Exception ex) {
            System.out.println(" [x] Exception receiving user event from AMQP: '" + ex.getMessage() + "'");
        }
    }

    @RabbitListener(queues = "#{autoDeleteQueue_User_Deleted.name}")
    public void receiveUserDeletedMsg(Message msg) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            UserViewAMQP userViewAMQP = objectMapper.readValue(jsonReceived, UserViewAMQP.class);

            System.out.println(" [x] AuthnUsers received User Deleted by AMQP: " + userViewAMQP.getUsername());
            try {
                userService.deleteFromAMQP(userViewAMQP);
                System.out.println(" [✓] User deleted from AuthnUsers: " + userViewAMQP.getUsername());
            } catch (Exception e) {
                System.out.println(" [x] Error deleting user from AuthnUsers: " + e.getMessage());
            }
        } catch (Exception ex) {
            System.out.println(" [x] Exception receiving user event from AMQP: '" + ex.getMessage() + "'");
        }
    }
}
