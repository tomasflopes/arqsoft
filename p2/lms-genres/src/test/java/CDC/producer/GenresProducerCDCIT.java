package CDC.producer;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.Message;

import com.fasterxml.jackson.core.JsonProcessingException;

import au.com.dius.pact.core.model.Interaction;
import au.com.dius.pact.core.model.Pact;
import au.com.dius.pact.provider.MessageAndMetadata;
import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import pt.psoft.g1.psoftg1.genremanagement.api.GenreViewAMQP;
import pt.psoft.g1.psoftg1.genremanagement.api.GenreViewAMQPMapperImpl;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.publishers.impl.GenreEventsRabbitmqPublisherImpl;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.publishers.GenreEventsPublisher;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = { GenreEventsRabbitmqPublisherImpl.class,
    GenreService.class, GenreViewAMQPMapperImpl.class }, properties = {
        "stubrunner.amqp.mockConnection=true",
        "spring.profiles.active=test"
    })
@Provider("book_event-producer")
@PactFolder("../target/pacts")
@PactBroker(url = "${PACT_BROKER_BASE_URL}")
public class GenresProducerCDCIT {
  @Autowired
  GenreEventsPublisher genreEventsPublisher;

  @Autowired
  GenreViewAMQPMapperImpl genreViewAMQPMapper;

  @MockBean
  RabbitTemplate template;

  @MockBean
  DirectExchange direct;

  @TestTemplate
  @ExtendWith(PactVerificationInvocationContextProvider.class)
  void testTemplate(Pact pact, Interaction interaction, PactVerificationContext context) {
    context.verifyInteraction();
  }

  @BeforeEach
  void before(PactVerificationContext context) {
    context.setTarget(new MessageTestTarget());
  }

  @PactVerifyProvider("a genre created event")
  public MessageAndMetadata genreCreated() throws JsonProcessingException {
    Genre genre = new Genre("Infantil");

    GenreViewAMQP genreViewAMQP = genreEventsPublisher.sendGenreCreated(genre);

    Message<String> message = new GenreMessageBuilder().withGenre(genreViewAMQP).build();

    return generateMessageAndMetadata(message);
  }

  @PactVerifyProvider("a genre updated event")
  public MessageAndMetadata genreUpdated() throws JsonProcessingException {
    Genre genre = new Genre("Infantil");

    GenreViewAMQP genreViewAMQP = genreEventsPublisher.sendGenreUpdated(genre, "Infanto-Juvenil", 1L);

    Message<String> message = new GenreMessageBuilder().withGenre(genreViewAMQP).build();

    return generateMessageAndMetadata(message);
  }

  private MessageAndMetadata generateMessageAndMetadata(Message<String> message) {
    HashMap<String, Object> metadata = new HashMap<String, Object>();
    message.getHeaders().forEach((k, v) -> metadata.put(k, v));

    return new MessageAndMetadata(message.getPayload().getBytes(), metadata);
  }
}
