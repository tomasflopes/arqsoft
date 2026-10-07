package CDC.consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Interaction;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingRabbitmqController;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingViewAMQP;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;

@ExtendWith(PactConsumerTestExt.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = { LendingRabbitmqController.class,
    LendingService.class })
@PactConsumerTest
@PactTestFor(providerName = "lending_event-producer", providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V4)
public class LendingsCDCDefinitionTest {
  @MockBean
  LendingService lendingService;

  @Autowired
  LendingRabbitmqController listener;

  @Pact(consumer = "lending_created-consumer")
  V4Pact createLendingCreatedPact(MessagePactBuilder builder) {
    PactDslJsonBody body = new PactDslJsonBody();
    body.stringType("lendingNumber", "2024/1");
    body.stringType("readerNumber", "2024/1");
    body.stringType("bookId", "9781234567897");
    body.stringType("startDate", "2024-01-15");
    body.stringType("limitDate", "2024-02-15");
    body.nullValue("returnedDate");
    body.nullValue("commentary");

    Map<String, Object> metadata = new HashMap<>();
    metadata.put("Content-Type", "application/json");
    metadata.put("correlation-id", "1");

    return builder.given("A lending was created")
        .expectsToReceive("a lending created event")
        .withMetadata(metadata)
        .withContent(body)
        .toPact();
  }

  @Pact(consumer = "lending_updated-consumer")
  V4Pact createLendingUpdatedPact(MessagePactBuilder builder) {
    PactDslJsonBody body = new PactDslJsonBody();
    body.stringType("lendingNumber", "2024/1");
    body.stringType("readerNumber", "2024/1");
    body.stringType("bookId", "9781234567897");
    body.stringType("startDate", "2024-01-15");
    body.stringType("limitDate", "2024-02-15");
    body.stringType("returnedDate", "2024-02-10");
    body.stringType("commentary", "Returned in good condition");

    Map<String, Object> metadata = new HashMap<>();
    metadata.put("Content-Type", "application/json");
    metadata.put("correlation-id", "2");

    return builder.given("A lending was updated")
        .expectsToReceive("a lending updated event")
        .withMetadata(metadata)
        .withContent(body)
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "createLendingCreatedPact")
  void testLendingCreated(List<V4Interaction.AsynchronousMessage> messages) {
    assertDoesNotThrow(() -> {
      for (V4Interaction.AsynchronousMessage message : messages) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        messageProperties.setCorrelationId("1");

        Message amqpMessage = new Message(
            message.contentsAsBytes(),
            messageProperties);

        listener.handleLendingCreated(amqpMessage);

        verify(lendingService, times(1)).create(any());
      }
    });
  }

  @Test
  @PactTestFor(pactMethod = "createLendingUpdatedPact")
  void testLendingUpdated(List<V4Interaction.AsynchronousMessage> messages) {
    assertDoesNotThrow(() -> {
      for (V4Interaction.AsynchronousMessage message : messages) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        messageProperties.setCorrelationId("2");

        Message amqpMessage = new Message(
            message.contentsAsBytes(),
            messageProperties);

        listener.handleLendingUpdated(amqpMessage);

        verify(lendingService, times(1)).update(any(LendingViewAMQP.class));
      }
    });
  }
}
