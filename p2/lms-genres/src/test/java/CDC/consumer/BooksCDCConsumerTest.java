package CDC.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookRabbitmqController;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = { BookRabbitmqController.class,
    BookService.class })
@Provider("book_event-producer")
@PactFolder("target/pacts")
public class BooksCDCConsumerTest {

  @MockBean
  BookService bookService;

  @Autowired
  BookRabbitmqController listener;

  // @Test
  // @Pact(consumer = "book_created-consumer")
  // void testBookCreated(List<V4Interaction.AsynchronousMessage> messages) throws
  // Exception {

  // // Convert the Pact message to a String (JSON payload)
  // String jsonReceived = messages.get(0).contentsAsString();

  // // Create a Spring AMQP Message with the JSON payload and optional headers
  // MessageProperties messageProperties = new MessageProperties();
  // messageProperties.setContentType("application/json");
  // Message message = new Message(jsonReceived.getBytes(StandardCharsets.UTF_8),
  // messageProperties);

  // // Simulate receiving the message in the listener
  // assertDoesNotThrow(() -> {
  // listener.receiveBookCreated(message);
  // });

  // // Verify interactions with the mocked service
  // verify(bookService, times(1)).create(any(BookViewAMQP.class));
  // }

  // @Test
  // @Pact(consumer = "book_updated-consumer")
  // void testBookUpdated(List<V4Interaction.AsynchronousMessage> messages) throws
  // Exception {
  // String jsonReceived = messages.get(0).contentsAsString();
  // MessageProperties messageProperties = new MessageProperties();
  // messageProperties.setContentType("application/json");
  // Message message = new Message(jsonReceived.getBytes(StandardCharsets.UTF_8),
  // messageProperties);

  // assertDoesNotThrow(() -> {
  // listener.receiveBookUpdated(message);
  // });

  // // Verify interactions with the mocked service
  // verify(bookService, times(1)).update(any(BookViewAMQP.class));
  // }
  // @Test
  // void testMessageProcessing() throws Exception {
  // // Use PactReader to load the Pact file
  // File pactFile = new
  // File("../target/pacts/book_created-consumer-book_event-producer.json");
  // PactReader pactReader = DefaultPactReader.INSTANCE;

  // Pact pact = pactReader.loadPact(pactFile);

  // List<Message> messagesGeneratedByPact =
  // pact.asMessagePact().get().getMessages();
  // for (Message messageGeneratedByPact : messagesGeneratedByPact) {
  // // Convert the Pact message to a String (JSON payload)
  // String jsonReceived = messageGeneratedByPact.contentsAsString();

  // // prepare message properties
  // MessageProperties messageProperties = new MessageProperties();
  // messageProperties.setContentType("application/json");

  // // Create a Spring AMQP Message with the JSON payload and optional headers
  // org.springframework.amqp.core.Message messageToBeSentByRabbit = new
  // org.springframework.amqp.core.Message(
  // jsonReceived.getBytes(StandardCharsets.UTF_8), messageProperties);

  // // Simulate receiving the message in the RabbitMQ listener
  // assertDoesNotThrow(() -> {
  // listener.receiveBookCreated(messageToBeSentByRabbit);
  // });

  // // somehow optional: verify interactions with the mocked service
  // verify(bookService, times(1)).create(any(BookViewAMQP.class));
  // }
  // }
}
