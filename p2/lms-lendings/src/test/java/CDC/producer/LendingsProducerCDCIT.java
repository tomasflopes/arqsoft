package CDC.producer;

import au.com.dius.pact.provider.MessageAndMetadata;
import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingViewAMQP;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;

@Provider("lending_event-producer")
@PactFolder("target/pacts")
@PactBroker(url = "${PACT_BROKER_BASE_URL}")
@ExtendWith(MockitoExtension.class)
public class LendingsProducerCDCIT {

  @TestTemplate
  @ExtendWith(PactVerificationInvocationContextProvider.class)
  void testTemplate(PactVerificationContext context) {
    context.verifyInteraction();
  }

  @BeforeEach
  void before(PactVerificationContext context) {
    context.setTarget(new MessageTestTarget());
  }

  @PactVerifyProvider("a lending created event")
  public MessageAndMetadata lendingCreated() throws JsonProcessingException {
    LendingViewAMQP lendingViewAMQP = new LendingViewAMQP(
        "2024/1",
        "2024/1",
        "9781234567897",
        LocalDate.of(2024, 1, 15).toString(),
        LocalDate.of(2024, 2, 15).toString(),
        null,
        null
    );

    Map<String, Object> metadata = new HashMap<>();
    metadata.put("Content-Type", "application/json");
    metadata.put("correlation-id", "1");

    String json = new ObjectMapper().writeValueAsString(lendingViewAMQP);
    return new MessageAndMetadata(json.getBytes(StandardCharsets.UTF_8), metadata);
  }

  @PactVerifyProvider("a lending updated event")
  public MessageAndMetadata lendingUpdated() throws JsonProcessingException {
    LendingViewAMQP lendingViewAMQP = new LendingViewAMQP(
        "2024/1",
        "2024/1",
        "9781234567897",
        LocalDate.of(2024, 1, 15).toString(),
        LocalDate.of(2024, 2, 15).toString(),
        LocalDate.of(2024, 2, 10).toString(),
        "Returned in good condition"
    );

    Map<String, Object> metadata = new HashMap<>();
    metadata.put("Content-Type", "application/json");
    metadata.put("correlation-id", "2");

    String json = new ObjectMapper().writeValueAsString(lendingViewAMQP);
    return new MessageAndMetadata(json.getBytes(StandardCharsets.UTF_8), metadata);
  }
}
