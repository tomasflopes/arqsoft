package pt.psoft.g1.psoftg1.configuration;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import pt.psoft.g1.psoftg1.bookmanagement.api.BookRabbitmqController;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookService;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderRabbitmqController;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingRabbitmqController;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.shared.model.LendingEvents;
import pt.psoft.g1.psoftg1.shared.model.BookEvents;

@Profile("!test")
@Configuration
@org.springframework.amqp.rabbit.annotation.EnableRabbit
public class RabbitmqClientConfig {

    @Bean
    public DirectExchange direct() {
        return new DirectExchange("LMS.lendings");
    }
    
    @Bean
    public DirectExchange booksExchange() {
        return new DirectExchange("LMS.books");
    }

    @Bean
    public DirectExchange readersExchange() {
        return new DirectExchange("LMS.readers");
    }

    // Lending Queues
    @Bean(name = "autoDeleteQueue_Lending_Created")
    public Queue autoDeleteQueue_Lending_Created() {
        return new Queue("lendings.queue.lending-created", true);
    }

    @Bean(name = "autoDeleteQueue_Lending_Updated")
    public Queue autoDeleteQueue_Lending_Updated() {
        return new Queue("lendings.queue.lending-updated", true);
    }

    @Bean(name = "autoDeleteQueue_Lending_Deleted")
    public Queue autoDeleteQueue_Lending_Deleted() {
        return new Queue("lendings.queue.lending-deleted", true);
    }

    @Bean
    public Binding binding1(DirectExchange direct,
                            @Qualifier("autoDeleteQueue_Lending_Created") Queue autoDeleteQueue_Lending_Created) {
        return BindingBuilder.bind(autoDeleteQueue_Lending_Created)
                .to(direct)
                .with(LendingEvents.LENDING_CREATED);
    }

    @Bean
    public Binding binding2(DirectExchange direct,
                            @Qualifier("autoDeleteQueue_Lending_Updated") Queue autoDeleteQueue_Lending_Updated) {
        return BindingBuilder.bind(autoDeleteQueue_Lending_Updated)
                .to(direct)
                .with(LendingEvents.LENDING_UPDATED);
    }

    @Bean
    public Binding binding3(DirectExchange direct,
                            @Qualifier("autoDeleteQueue_Lending_Deleted") Queue autoDeleteQueue_Lending_Deleted) {
        return BindingBuilder.bind(autoDeleteQueue_Lending_Deleted)
                .to(direct)
                .with(LendingEvents.LENDING_DELETED);
    }

    // Book Queues - Lendings consome de Books usando filas nomeadas (load-balanced)
    @Bean(name = "autoDeleteQueue_Book_Created")
    public Queue autoDeleteQueue_Book_Created() {
        return new Queue("lendings.queue.book-created", true);
    }

    @Bean(name = "autoDeleteQueue_Book_Updated")
    public Queue autoDeleteQueue_Book_Updated() {
        return new Queue("lendings.queue.book-updated", true);
    }

    @Bean(name = "autoDeleteQueue_Book_Deleted")
    public Queue autoDeleteQueue_Book_Deleted() {
        return new Queue("lendings.queue.book-deleted", true);
    }

    // Bindings para eventos de Books (do exchange LMS.books)
    @Bean
    public Binding bindingBookCreated(@Qualifier("booksExchange") DirectExchange booksExchange,
                                      @Qualifier("autoDeleteQueue_Book_Created") Queue autoDeleteQueue_Book_Created) {
        return BindingBuilder.bind(autoDeleteQueue_Book_Created)
                .to(booksExchange)
                .with(BookEvents.BOOK_CREATED);
    }

    @Bean
    public Binding bindingBookUpdated(@Qualifier("booksExchange") DirectExchange booksExchange,
                                      @Qualifier("autoDeleteQueue_Book_Updated") Queue autoDeleteQueue_Book_Updated) {
        return BindingBuilder.bind(autoDeleteQueue_Book_Updated)
                .to(booksExchange)
                .with(BookEvents.BOOK_UPDATED);
    }

    @Bean
    public Binding bindingBookDeleted(@Qualifier("booksExchange") DirectExchange booksExchange,
                                      @Qualifier("autoDeleteQueue_Book_Deleted") Queue autoDeleteQueue_Book_Deleted) {
        return BindingBuilder.bind(autoDeleteQueue_Book_Deleted)
                .to(booksExchange)
                .with(BookEvents.BOOK_DELETED);
    }

    // Reader Queues
    @Bean(name = "autoDeleteQueue_Reader_Created")
    public Queue autoDeleteQueue_Reader_Created() {
        return new Queue("lendings.queue.reader-created", true);
    }

    @Bean(name = "autoDeleteQueue_Reader_Updated")
    public Queue autoDeleteQueue_Reader_Updated() {
        return new Queue("lendings.queue.reader-updated", true);
    }

    @Bean
    public Binding bindingReaderCreated(@Qualifier("readersExchange") DirectExchange readersExchange,
                                        @Qualifier("autoDeleteQueue_Reader_Created") Queue autoDeleteQueue_Reader_Created) {
        return BindingBuilder.bind(autoDeleteQueue_Reader_Created)
                .to(readersExchange)
                .with("READER_CREATED");
    }

    @Bean
    public Binding bindingReaderUpdated(@Qualifier("readersExchange") DirectExchange readersExchange,
                                        @Qualifier("autoDeleteQueue_Reader_Updated") Queue autoDeleteQueue_Reader_Updated) {
        return BindingBuilder.bind(autoDeleteQueue_Reader_Updated)
                .to(readersExchange)
                .with("READER_UPDATED");
    }

    // Controllers
    @Bean
    public LendingRabbitmqController lendingReceiver(LendingService lendingService) {
        return new LendingRabbitmqController(lendingService);
    }

    @Bean
    public BookRabbitmqController bookReceiver(BookService bookService) {
        return new BookRabbitmqController(bookService);
    }

    @Bean
    public ReaderRabbitmqController readerReceiver(ReaderService readerService) {
        return new ReaderRabbitmqController(readerService);
    }
}