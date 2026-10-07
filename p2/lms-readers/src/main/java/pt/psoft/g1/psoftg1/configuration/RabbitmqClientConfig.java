package pt.psoft.g1.psoftg1.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import pt.psoft.g1.psoftg1.genremanagement.api.GenreRabbitmqController;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreService;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderRabbitmqController;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.shared.model.GenreEvents;
import pt.psoft.g1.psoftg1.shared.model.ReaderEvents;
import pt.psoft.g1.psoftg1.shared.model.UserEvents;
import pt.psoft.g1.psoftg1.usermanagement.api.UserRabbitmqController;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;

@Profile("!test")
@Configuration
public  class RabbitmqClientConfig {

    @Bean
    public DirectExchange direct() {
        return new DirectExchange("LMS.readers");
    }

    @Bean(name = "genresExchange")
    public DirectExchange genresExchange() {
        return new DirectExchange("LMS.genres");
    }

    // ============================================
    // READER QUEUES - Principal domain do microserviço
    // ============================================
    @Bean(name = "autoDeleteQueue_Reader_Created")
    public Queue autoDeleteQueue_Reader_Created() {
        System.out.println("autoDeleteQueue_Reader_Created created!");
        return new Queue("readers.queue.reader-created", true);
    }

    @Bean(name = "autoDeleteQueue_Reader_Updated")
    public Queue autoDeleteQueue_Reader_Updated() {
        return new Queue("readers.queue.reader-updated", true);
    }

    @Bean(name = "autoDeleteQueue_Reader_Deleted")
    public Queue autoDeleteQueue_Reader_Deleted() {
        return new Queue("readers.queue.reader-deleted", true);
    }

    @Bean
    public Binding binding1(DirectExchange direct,
                            @Qualifier("autoDeleteQueue_Reader_Created") Queue autoDeleteQueue_Reader_Created) {
        return BindingBuilder.bind(autoDeleteQueue_Reader_Created)
                .to(direct)
                .with(ReaderEvents.READER_CREATED);
    }

    @Bean
    public Binding binding2(DirectExchange direct,
                            @Qualifier("autoDeleteQueue_Reader_Updated") Queue autoDeleteQueue_Reader_Updated) {
        return BindingBuilder.bind(autoDeleteQueue_Reader_Updated)
                .to(direct)
                .with(ReaderEvents.READER_UPDATED);
    }

    @Bean
    public Binding binding3(DirectExchange direct,
                            @Qualifier("autoDeleteQueue_Reader_Deleted") Queue autoDeleteQueue_Reader_Deleted) {
        return BindingBuilder.bind(autoDeleteQueue_Reader_Deleted)
                .to(direct)
                .with(ReaderEvents.READER_DELETED);
    }

    // ============================================
    // USER QUEUES - Dados necessários do microserviço de Users
    // ============================================
    @Bean(name = "autoDeleteQueue_User_Created")
    public Queue autoDeleteQueue_User_Created() {
        System.out.println("autoDeleteQueue_User_Created created!");
        return new Queue("readers.queue.user-created", true);
    }

    @Bean(name = "autoDeleteQueue_User_Updated")
    public Queue autoDeleteQueue_User_Updated() {
        return new Queue("readers.queue.user-updated", true);
    }

    @Bean(name = "autoDeleteQueue_User_Deleted")
    public Queue autoDeleteQueue_User_Deleted() {
        return new Queue("readers.queue.user-deleted", true);
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
    // GENRE QUEUES - Dados necessários do microserviço de Genres
    // ============================================
    @Bean(name = "autoDeleteQueue_Genre_Created")
    public Queue autoDeleteQueue_Genre_Created() {
        System.out.println("autoDeleteQueue_Genre_Created created!");
        return new Queue("readers.queue.genre-created.v2", true);
    }

    @Bean(name = "autoDeleteQueue_Genre_Updated")
    public Queue autoDeleteQueue_Genre_Updated() {
        return new Queue("readers.queue.genre-updated.v2", true);
    }

    @Bean(name = "autoDeleteQueue_Genre_Deleted")
    public Queue autoDeleteQueue_Genre_Deleted() {
        return new Queue("readers.queue.genre-deleted.v2", true);
    }

    @Bean
    public Binding bindingGenreCreated(@Qualifier("genresExchange") DirectExchange genresExchange,
                                       @Qualifier("autoDeleteQueue_Genre_Created") Queue autoDeleteQueue_Genre_Created) {
        return BindingBuilder.bind(autoDeleteQueue_Genre_Created)
                .to(genresExchange)
                .with(GenreEvents.GENRE_CREATED.getEventName());
    }

    @Bean
    public Binding bindingGenreUpdated(@Qualifier("genresExchange") DirectExchange genresExchange,
                                       @Qualifier("autoDeleteQueue_Genre_Updated") Queue autoDeleteQueue_Genre_Updated) {
        return BindingBuilder.bind(autoDeleteQueue_Genre_Updated)
                .to(genresExchange)
                .with(GenreEvents.GENRE_UPDATED.getEventName());
    }

    @Bean
    public Binding bindingGenreDeleted(@Qualifier("genresExchange") DirectExchange genresExchange,
                                       @Qualifier("autoDeleteQueue_Genre_Deleted") Queue autoDeleteQueue_Genre_Deleted) {
        return BindingBuilder.bind(autoDeleteQueue_Genre_Deleted)
                .to(genresExchange)
                .with(GenreEvents.GENRE_DELETED.getEventName());
    }

    // ============================================
    // RABBITMQ CONTROLLERS
    // ============================================
    @Bean
    public ReaderRabbitmqController readerReceiver(ReaderService readerService,
                                                   @Qualifier("autoDeleteQueue_Reader_Created") Queue autoDeleteQueue_Reader_Created) {
        return new ReaderRabbitmqController(readerService);
    }

    @Bean
    public UserRabbitmqController userReceiver(UserService userService,
                                               @Qualifier("autoDeleteQueue_User_Created") Queue autoDeleteQueue_User_Created) {
        return new UserRabbitmqController(userService);
    }

    @Bean
    public GenreRabbitmqController genreReceiver(GenreService genreService,
                                                 @Qualifier("autoDeleteQueue_Genre_Created") Queue autoDeleteQueue_Genre_Created) {
        return new GenreRabbitmqController(genreService);
    }
}
