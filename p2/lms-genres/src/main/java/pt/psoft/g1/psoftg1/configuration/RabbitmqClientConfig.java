package pt.psoft.g1.psoftg1.configuration;

import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import pt.psoft.g1.psoftg1.genremanagement.api.GenreRabbitmqController;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreService;
import pt.psoft.g1.psoftg1.shared.model.BookEvents;
import pt.psoft.g1.psoftg1.shared.model.GenreEvents;

@Profile("!test")
@Configuration
@EnableRabbit
public class RabbitmqClientConfig {

  @Bean
  public DirectExchange direct() {
    return new DirectExchange("LMS.genres");
  }

  @Bean
  public DirectExchange booksExchange() {
    return new DirectExchange("LMS.books");
  }

  @Bean
  public DirectExchange lendingsExchange() {
    return new DirectExchange("LMS.lendings");
  }

  // REMOVIDO: Não usar filas nomeadas para Genres (load-balanced entre réplicas)
  // Genres usa apenas filas anónimas para broadcast a todas as réplicas

  // Filas internas para que cada réplica do Genres consuma os próprios eventos
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

  @Bean(name = "autoDeleteQueue_Book_Created")
  public Queue autoDeleteQueue_Book_Created() {
    // v2 para forçar recriação da fila com routing key correta
    return new Queue("genres.queue.book-created.v2", true);
  }

  @Bean(name = "autoDeleteQueue_Book_Updated")
  public Queue autoDeleteQueue_Book_Updated() {
    return new Queue("genres.queue.book-updated.v2", true);
  }

  @Bean(name = "autoDeleteQueue_Book_Deleted")
  public Queue autoDeleteQueue_Book_Deleted() {
    return new Queue("genres.queue.book-deleted.v2", true);
  }

  // REMOVIDO: Bindings para filas nomeadas de Genre (seriam load-balanced)
  // Genres usa apenas filas anónimas

  // Bindings para que cada instância do Genres receba todos os eventos
  // (broadcast)
  @Bean
  public Binding bindingSelf1(DirectExchange direct,
      @Qualifier("selfQueue_Genre_Created") Queue selfQueue_Genre_Created) {
    return BindingBuilder.bind(selfQueue_Genre_Created)
        .to(direct)
        .with(GenreEvents.GENRE_CREATED.getEventName());
  }

  @Bean
  public Binding bindingSelf2(DirectExchange direct,
      @Qualifier("selfQueue_Genre_Updated") Queue selfQueue_Genre_Updated) {
    return BindingBuilder.bind(selfQueue_Genre_Updated)
        .to(direct)
        .with(GenreEvents.GENRE_UPDATED.getEventName());
  }

  @Bean
  public Binding bindingSelf3(DirectExchange direct,
      @Qualifier("selfQueue_Genre_Deleted") Queue selfQueue_Genre_Deleted) {
    return BindingBuilder.bind(selfQueue_Genre_Deleted)
        .to(direct)
        .with(GenreEvents.GENRE_DELETED.getEventName());
  }

  @Bean
  public Binding binding4(@Qualifier("booksExchange") DirectExchange booksExchange,
      @Qualifier("autoDeleteQueue_Book_Created") Queue autoDeleteQueue_Book_Created) {
    return BindingBuilder.bind(autoDeleteQueue_Book_Created)
        .to(booksExchange)
        .with(BookEvents.BOOK_CREATED.getEventName());
  }

  @Bean
  public Binding binding5(@Qualifier("booksExchange") DirectExchange booksExchange,
      @Qualifier("autoDeleteQueue_Book_Updated") Queue autoDeleteQueue_Book_Updated) {
    return BindingBuilder.bind(autoDeleteQueue_Book_Updated)
        .to(booksExchange)
        .with(BookEvents.BOOK_UPDATED.getEventName());
  }

  @Bean
  public Binding binding6(@Qualifier("booksExchange") DirectExchange booksExchange,
      @Qualifier("autoDeleteQueue_Book_Deleted") Queue autoDeleteQueue_Book_Deleted) {
    return BindingBuilder.bind(autoDeleteQueue_Book_Deleted)
        .to(booksExchange)
        .with(BookEvents.BOOK_DELETED.getEventName());
  }

  @Bean
  public GenreRabbitmqController genreSelfReceiver(GenreService genreService,
      @Qualifier("selfQueue_Genre_Created") Queue selfQueue_Genre_Created) {
    // Apenas para garantir criação do bean; a queue é usada pelos listeners na
    // classe.
    return new GenreRabbitmqController(genreService);
  }

  @Bean(name = "autoDeleteQueue_Lending_Created")
  public Queue autoDeleteQueue_Lending_Created() {
    return new Queue("genres.queue.lending-created", true);
  }

  @Bean(name = "autoDeleteQueue_Lending_Updated")
  public Queue autoDeleteQueue_Lending_Updated() {
    return new Queue("genres.queue.lending-updated", true);
  }

  @Bean
  public Binding bindingLendingCreated(@Qualifier("lendingsExchange") DirectExchange lendingsExchange,
      @Qualifier("autoDeleteQueue_Lending_Created") Queue autoDeleteQueue_Lending_Created) {
    return BindingBuilder.bind(autoDeleteQueue_Lending_Created)
        .to(lendingsExchange)
        .with("Lending.Created");
  }

  @Bean
  public Binding bindingLendingUpdated(@Qualifier("lendingsExchange") DirectExchange lendingsExchange,
      @Qualifier("autoDeleteQueue_Lending_Updated") Queue autoDeleteQueue_Lending_Updated) {
    return BindingBuilder.bind(autoDeleteQueue_Lending_Updated)
        .to(lendingsExchange)
        .with("Lending.Updated");
  }
}
