package CDC.producer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import au.com.dius.pact.provider.MessageAndMetadata;
import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderRabbitmqController;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderViewAMQP;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderViewAMQPMapper;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.publishers.ReaderEventsPublisher;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = { ReaderRabbitmqController.class,
    ReaderService.class })
@Provider("reader_event-producer")
@PactFolder("target/pacts")
public class ReadersProducerCDCIT {

  @Mock
  private ReaderEventsPublisher readerEventsPublisher;

  @Mock
  private ReaderViewAMQPMapper readerViewAMQPMapper;

  @BeforeEach
  void setUp(PactVerificationContext context) {
    context.setTarget(new MessageTestTarget());
  }

  @PactVerifyProvider("a reader created event")
  public MessageAndMetadata readerCreated() throws Exception {
    // Create a mock ReaderDetails (constructor is protected)
    var readerDetails = mock(pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails.class);

    // Create the ReaderViewAMQP
    ReaderViewAMQP readerViewAMQP = new ReaderViewAMQP();
    readerViewAMQP.setReaderNumber("2025/1");
    readerViewAMQP.setName("John Doe");
    readerViewAMQP.setEmail("john.doe@example.com");
    readerViewAMQP.setVersion(1L);

    // Mock the publisher to return a Message with the ReaderViewAMQP
    when(readerEventsPublisher.sendReaderCreated(any(ReaderDetails.class)))
        .thenReturn(readerViewAMQP);

    // Call the publisher
    readerEventsPublisher.sendReaderCreated(readerDetails);

    // Convert to JSON bytes
    String json = String.format(
        "{\"readerNumber\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"version\":%d}",
        readerViewAMQP.getReaderNumber(),
        readerViewAMQP.getName(),
        readerViewAMQP.getEmail(),
        readerViewAMQP.getVersion());

    return new MessageAndMetadata(json.getBytes(), new HashMap<>());
  }

  @PactVerifyProvider("a reader updated event")
  public MessageAndMetadata readerUpdated() throws Exception {
    // Create a mock ReaderDetails (constructor is protected)
    var readerDetails = mock(pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails.class);

    // Create the ReaderViewAMQP
    ReaderViewAMQP readerViewAMQP = new ReaderViewAMQP();
    readerViewAMQP.setReaderNumber("2025/1");
    readerViewAMQP.setName("John Doe Updated");
    readerViewAMQP.setEmail("john.doe@example.com");
    readerViewAMQP.setVersion(2L);

    // Mock the publisher to return a Message with the ReaderViewAMQP
    when(readerEventsPublisher.sendReaderUpdated(any(ReaderDetails.class), anyLong()))
        .thenReturn(readerViewAMQP);

    // Call the publisher
    readerEventsPublisher.sendReaderUpdated(readerDetails, 1L);

    // Convert to JSON bytes
    String json = String.format(
        "{\"readerNumber\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"version\":%d}",
        readerViewAMQP.getReaderNumber(),
        readerViewAMQP.getName(),
        readerViewAMQP.getEmail(),
        readerViewAMQP.getVersion());

    return new MessageAndMetadata(json.getBytes(), new HashMap<>());
  }
}
