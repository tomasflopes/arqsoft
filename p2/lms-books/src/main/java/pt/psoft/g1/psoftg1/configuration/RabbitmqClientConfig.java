package pt.psoft.g1.psoftg1.configuration;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookRabbitmqController;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookService;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreService;
import pt.psoft.g1.psoftg1.shared.model.BookEvents;
import pt.psoft.g1.psoftg1.shared.model.GenreEvents;

@Profile("!test")
@Configuration
@org.springframework.amqp.rabbit.annotation.EnableRabbit
public  class RabbitmqClientConfig {

    @Bean
    public DirectExchange direct() {
        return new DirectExchange("LMS.books");
    }
    
    @Bean
    public DirectExchange genreExchange() {
        return new DirectExchange("LMS.genres");
    }

    // Filas anónimas para que cada réplica do Books consuma os próprios eventos (broadcast)
    @Bean(name = "selfQueue_Book_Created")
    public Queue selfQueue_Book_Created() {
        return new AnonymousQueue();
    }

    @Bean(name = "selfQueue_Book_Updated")
    public Queue selfQueue_Book_Updated() {
        return new AnonymousQueue();
    }

    @Bean(name = "selfQueue_Book_Deleted")
    public Queue selfQueue_Book_Deleted() {
        return new AnonymousQueue();
    }

    // Filas nomeadas para que Lendings consuma os eventos de Books (load-balanced)
    @Bean(name = "autoDeleteQueue_Book_Created")
    public Queue autoDeleteQueue_Book_Created() {
        System.out.println("autoDeleteQueue_Book_Created created!");
        return new Queue("lendings.queue.book-created", true);
    }

    @Bean
    public Queue autoDeleteQueue_Book_Updated() {
        return new Queue("lendings.queue.book-updated", true);
    }

    @Bean
    public Queue autoDeleteQueue_Book_Deleted() {
        return new Queue("lendings.queue.book-deleted", true);
    }

    // Filas anónimas para consumir eventos de Genres (broadcast a todas as réplicas)
    @Bean(name = "selfQueue_Genre_Created")
    public Queue selfQueue_Genre_Created() {
        return new AnonymousQueue();
    }

    @Bean(name = "selfQueue_Genre_Updated")
    public Queue selfQueue_Genre_Updated() {
        return new AnonymousQueue();
    }

    @Bean(name = "selfQueue_Genre_Deleted")
    public Queue selfQueue_Genre_Deleted() {
        return new AnonymousQueue();
    }

    @Bean
    public Binding binding1(@Qualifier("direct") DirectExchange direct,
                            @Qualifier("selfQueue_Book_Created") Queue selfQueue_Book_Created) {
        return BindingBuilder.bind(selfQueue_Book_Created)
                .to(direct)
                .with(BookEvents.BOOK_CREATED);
    }

    @Bean
    public Binding binding2(@Qualifier("direct") DirectExchange direct,
                            @Qualifier("selfQueue_Book_Updated") Queue selfQueue_Book_Updated) {
        return BindingBuilder.bind(selfQueue_Book_Updated)
                .to(direct)
                .with(BookEvents.BOOK_UPDATED);
    }

    @Bean
    public Binding binding3(@Qualifier("direct") DirectExchange direct,
                            @Qualifier("selfQueue_Book_Deleted") Queue selfQueue_Book_Deleted) {
        return BindingBuilder.bind(selfQueue_Book_Deleted)
                .to(direct)
                .with(BookEvents.BOOK_DELETED);
    }

    // Bindings para filas nomeadas (para Lendings consumir - load balanced)
    @Bean
    public Binding binding4(@Qualifier("direct") DirectExchange direct,
                            @Qualifier("autoDeleteQueue_Book_Created") Queue autoDeleteQueue_Book_Created) {
        return BindingBuilder.bind(autoDeleteQueue_Book_Created)
                .to(direct)
                .with(BookEvents.BOOK_CREATED);
    }

    @Bean
    public Binding binding5(@Qualifier("direct") DirectExchange direct,
                            @Qualifier("autoDeleteQueue_Book_Updated") Queue autoDeleteQueue_Book_Updated) {
        return BindingBuilder.bind(autoDeleteQueue_Book_Updated)
                .to(direct)
                .with(BookEvents.BOOK_UPDATED);
    }

    @Bean
    public Binding binding6(@Qualifier("direct") DirectExchange direct,
                            @Qualifier("autoDeleteQueue_Book_Deleted") Queue autoDeleteQueue_Book_Deleted) {
        return BindingBuilder.bind(autoDeleteQueue_Book_Deleted)
                .to(direct)
                .with(BookEvents.BOOK_DELETED);
    }

    @Bean
    public Binding binding7(@Qualifier("genreExchange") DirectExchange genreExchange,
                            @Qualifier("selfQueue_Genre_Created") Queue selfQueue_Genre_Created) {
        return BindingBuilder.bind(selfQueue_Genre_Created)
                .to(genreExchange)
                .with(GenreEvents.GENRE_CREATED.getEventName());
    }

    @Bean
    public Binding binding8(@Qualifier("genreExchange") DirectExchange genreExchange,
                            @Qualifier("selfQueue_Genre_Updated") Queue selfQueue_Genre_Updated) {
        return BindingBuilder.bind(selfQueue_Genre_Updated)
                .to(genreExchange)
                .with(GenreEvents.GENRE_UPDATED.getEventName());
    }

    @Bean
    public Binding binding9(@Qualifier("genreExchange") DirectExchange genreExchange,
                            @Qualifier("selfQueue_Genre_Deleted") Queue selfQueue_Genre_Deleted) {
        return BindingBuilder.bind(selfQueue_Genre_Deleted)
                .to(genreExchange)
                .with(GenreEvents.GENRE_DELETED.getEventName());
    }

    @Bean
    public BookRabbitmqController receiver(BookService bookService, GenreService genreService, 
                                          @Qualifier("autoDeleteQueue_Book_Created") Queue autoDeleteQueue_Book_Created) {
        return new BookRabbitmqController(bookService, genreService);
    }
}
