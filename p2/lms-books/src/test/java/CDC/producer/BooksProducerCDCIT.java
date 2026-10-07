package CDC.producer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;

import com.fasterxml.jackson.core.JsonProcessingException;

import au.com.dius.pact.provider.MessageAndMetadata;
import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import pt.psoft.g1.psoftg1.TestConfig;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQP;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQPMapper;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQPMapperImpl;
import pt.psoft.g1.psoftg1.bookmanagement.infrastructure.publishers.impl.BookEventsRabbitmqPublisherImpl;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.publishers.BookEventsPublisher;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookService;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

@Import(TestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = { BookEventsRabbitmqPublisherImpl.class,
    BookService.class, BookViewAMQPMapperImpl.class }, properties = {
        "stubrunner.amqp.mockConnection=true",
        "spring.profiles.active=test"
    })
@Provider("book_event-producer")
@PactFolder("target/pacts")
public class BooksProducerCDCIT {
  @Autowired
  BookEventsPublisher bookEventsPublisher;

  @Autowired
  BookViewAMQPMapper bookViewAMQPMapper;

  @MockBean
  RabbitTemplate template;

  @MockBean
  DirectExchange direct;

  @BeforeEach
  void before(PactVerificationContext context) {
    context.setTarget(new MessageTestTarget());
  }

  @Test
  @PactVerifyProvider("a book created event")
  public MessageAndMetadata bookCreated() throws JsonProcessingException {
    Genre genre = new Genre("Infantil");

    Author author = new Author("José Saramago", "shot bio", "");
    List<Author> authors = new ArrayList<>();
    authors.add(author);

    Book book = new Book(
        "6475803429671",
        "title",
        "description",
        genre,
        authors,
        "");

    BookViewAMQP bookViewAMQP = bookEventsPublisher.sendBookCreated(book);

    Message<String> message = new BookMessageBuilder().withBook(bookViewAMQP).build();

    return generateMessageAndMetadata(message);
  }

  @PactVerifyProvider("a book updated event")
  public MessageAndMetadata bookUpdated() throws JsonProcessingException {

    Genre genre = new Genre("Infantil");

    Author author = new Author("José Saramago", "shot bio", "");
    List<Author> authors = new ArrayList<>();
    authors.add(author);

    Book book = new Book(
        "6475803429671",
        "title",
        "description",
        genre,
        authors,
        "");

    BookViewAMQP bookViewAMQP = bookEventsPublisher.sendBookUpdated(book, 1L);

    Message<String> message = new BookMessageBuilder().withBook(bookViewAMQP).build();

    return generateMessageAndMetadata(message);
  }

  private MessageAndMetadata generateMessageAndMetadata(Message<String> message) {
    HashMap<String, Object> metadata = new HashMap<String, Object>();
    message.getHeaders().forEach((k, v) -> metadata.put(k, v));

    return new MessageAndMetadata(message.getPayload().getBytes(), metadata);
  }
}
