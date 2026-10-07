package pt.psoft.g1.psoftg1.bookmanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookService;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class BookRabbitmqController {

    private final BookService bookService;

    @RabbitListener(queues = "#{autoDeleteQueue_Book_Created.name}")
    public void receiveBookCreatedMsg(Message msg) {
        try {
            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            BookViewAMQP bookViewAMQP = objectMapper.readValue(jsonReceived, BookViewAMQP.class);

            System.out.println(" [x] Received Book Created by AMQP: " + jsonReceived);
            try {
                bookService.create(bookViewAMQP);
                System.out.println(" [x] New book inserted from AMQP: " + bookViewAMQP.getIsbn());
            } catch (Exception e) {
                System.out.println(" [x] Book already exists. No need to store it. Error: " + e.getMessage());
            }
        } catch (Exception ex) {
            System.out.println(" [x] Exception receiving book event from AMQP: '" + ex.getMessage() + "'");
        }
    }

    @RabbitListener(queues = "#{autoDeleteQueue_Book_Updated.name}")
    public void receiveBookUpdated(Message msg) {
        try {
            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            BookViewAMQP bookViewAMQP = objectMapper.readValue(jsonReceived, BookViewAMQP.class);

            System.out.println(" [x] Received Book Updated by AMQP: " + jsonReceived);
            try {
                bookService.update(bookViewAMQP);
                System.out.println(" [x] Book updated from AMQP: " + bookViewAMQP);
            } catch (Exception e) {
                System.out.println(" [x] Book does not exist or wrong version. Nothing stored. Error: " + e.getMessage());
            }
        } catch (Exception ex) {
            System.out.println(" [x] Exception receiving book event from AMQP: '" + ex.getMessage() + "'");
        }
    }

    // Book Deleted - Lendings não precisa de apagar o book, apenas de saber quando ele já não existe
    // Se necessário, pode-se adicionar um flag "deleted" no Book
    /*
    @RabbitListener(queues = "#{autoDeleteQueue_Book_Deleted.name}")
    public void receiveBookDeleted(Message msg) {
        try {
            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            BookViewAMQP bookViewAMQP = objectMapper.readValue(jsonReceived, BookViewAMQP.class);

            System.out.println(" [x] Received Book Deleted by AMQP: " + jsonReceived);
            // Lendings mantém o registo do book para histórico de empréstimos
            System.out.println(" [x] Book marked as deleted (kept for lending history): " + bookViewAMQP);
        } catch (Exception ex) {
            System.out.println(" [x] Exception receiving book deleted event from AMQP: '" + ex.getMessage() + "'");
        }
    }
    */
}