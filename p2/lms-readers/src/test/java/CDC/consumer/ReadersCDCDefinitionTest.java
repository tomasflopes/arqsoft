package CDC.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessageBuilder;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderRabbitmqController;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderViewAMQP;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;

@ExtendWith({ PactConsumerTestExt.class, MockitoExtension.class })
@PactConsumerTest
@PactTestFor(providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V4)
public class ReadersCDCDefinitionTest {

  @Mock
  private ReaderService readerService;

  @InjectMocks
  private ReaderRabbitmqController listener;

  @Pact(consumer = "reader_created-consumer")
  public V4Pact createReaderCreatedPact(MessagePactBuilder builder) {
    PactDslJsonBody body = new PactDslJsonBody();
    body.stringType("readerNumber", "2025/1");
    body.stringType("name", "John Doe");
    body.stringType("email", "john.doe@example.com");
    body.integerType("version", 1);

    return builder
        .expectsToReceive("a reader created event")
        .withContent(body)
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "createReaderCreatedPact")
  public void testReaderCreatedPact(List<au.com.dius.pact.core.model.V4Interaction.AsynchronousMessage> messages)
      throws Exception {
    // Convert Pact Message to Spring AMQP Message
    org.springframework.amqp.core.Message msg = MessageBuilder.withBody(messages.get(0).contentsAsBytes()).build();

    // Call the listener method
    listener.handleReaderCreated(msg);

    // Verify that the service method was called
    verify(readerService, times(1)).create(any(ReaderViewAMQP.class));
  }

  @Pact(consumer = "reader_updated-consumer")
  public V4Pact createReaderUpdatedPact(MessagePactBuilder builder) {
    PactDslJsonBody body = new PactDslJsonBody();
    body.stringType("readerNumber", "2025/1");
    body.stringType("name", "John Doe Updated");
    body.stringType("email", "john.doe@example.com");
    body.integerType("version", 2);

    return builder
        .expectsToReceive("a reader updated event")
        .withContent(body)
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "createReaderUpdatedPact")
  public void testReaderUpdatedPact(List<au.com.dius.pact.core.model.V4Interaction.AsynchronousMessage> messages)
      throws Exception {
    // Convert Pact Message to Spring AMQP Message
    org.springframework.amqp.core.Message msg = MessageBuilder.withBody(messages.get(0).contentsAsBytes()).build();

    // Call the listener method
    listener.handleReaderUpdated(msg);

    // Verify that the service method was called
    verify(readerService, times(1)).update(any(ReaderViewAMQP.class));
  }
}
