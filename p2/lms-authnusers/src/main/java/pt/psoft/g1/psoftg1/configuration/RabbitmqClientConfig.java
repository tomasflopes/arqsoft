package pt.psoft.g1.psoftg1.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import pt.psoft.g1.psoftg1.shared.model.UserEvents;
import pt.psoft.g1.psoftg1.usermanagement.api.UserRabbitmqController;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;

@Profile("!test")
@Configuration
@EnableRabbit
public class RabbitmqClientConfig {

    // ============================================
    // EXCHANGE CONFIGURATION
    // ============================================
    @Bean
    public DirectExchange direct() {
        return new DirectExchange("LMS.readers");
    }

    // ============================================
    // USER QUEUES - Para consumir eventos de User criados por outros microserviços
    // ============================================
    @Bean(name = "autoDeleteQueue_User_Created")
    public Queue autoDeleteQueue_User_Created() {
        System.out.println("autoDeleteQueue_User_Created created in AuthnUsers!");
        return new Queue("authnusers.queue.user-created", true);
    }

    @Bean(name = "autoDeleteQueue_User_Updated")
    public Queue autoDeleteQueue_User_Updated() {
        return new Queue("authnusers.queue.user-updated", true);
    }

    @Bean(name = "autoDeleteQueue_User_Deleted")
    public Queue autoDeleteQueue_User_Deleted() {
        return new Queue("authnusers.queue.user-deleted", true);
    }

    @Bean
    public Binding bindingUserCreated(DirectExchange direct,
                                      @Qualifier("autoDeleteQueue_User_Created") Queue autoDeleteQueue_User_Created) {
        return BindingBuilder.bind(autoDeleteQueue_User_Created)
                .to(direct)
                .with(UserEvents.USER_CREATED);
    }

    @Bean
    public Binding bindingUserUpdated(DirectExchange direct,
                                      @Qualifier("autoDeleteQueue_User_Updated") Queue autoDeleteQueue_User_Updated) {
        return BindingBuilder.bind(autoDeleteQueue_User_Updated)
                .to(direct)
                .with(UserEvents.USER_UPDATED);
    }

    @Bean
    public Binding bindingUserDeleted(DirectExchange direct,
                                      @Qualifier("autoDeleteQueue_User_Deleted") Queue autoDeleteQueue_User_Deleted) {
        return BindingBuilder.bind(autoDeleteQueue_User_Deleted)
                .to(direct)
                .with(UserEvents.USER_DELETED);
    }

    // ============================================
    // RABBITMQ CONTROLLERS
    // ============================================
    @Bean
    public UserRabbitmqController userReceiver(UserService userService) {
        return new UserRabbitmqController(userService);
    }
}
