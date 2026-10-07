package pt.psoft.g1.psoftg1.genremanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreService;

import java.nio.charset.StandardCharsets;

/**
 * Controller para receber eventos de Genres via AMQP
 * Sincroniza dados mínimos de genres necessários para o microserviço de Readers
 */
@Service
@RequiredArgsConstructor
public class GenreRabbitmqController {

    private final GenreService genreService;

    @RabbitListener(queues = "#{autoDeleteQueue_Genre_Created.name}")
    public void receiveGenreCreatedMsg(Message msg) {
        try {
            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);

            ObjectMapper objectMapper = new ObjectMapper();
            GenreViewAMQP genreViewAMQP = objectMapper.readValue(jsonReceived, GenreViewAMQP.class);

            System.out.println(" [x] Received Genre Created by AMQP: " + genreViewAMQP.getGenre());
            try {
                genreService.createFromAMQP(genreViewAMQP);
                System.out.println(" [x] New genre synchronized from AMQP: " + genreViewAMQP.getGenre());
            } catch (Exception e) {
                System.out.println(" [x] Genre already exists or error syncing: " + e.getMessage());
            }
        } catch (Exception ex) {
            System.out.println(" [x] Exception receiving genre event from AMQP: '" + ex.getMessage() + "'");
        }
    }

    @RabbitListener(queues = "#{autoDeleteQueue_Genre_Updated.name}")
    public void receiveGenreUpdatedMsg(Message msg) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            GenreViewAMQP genreViewAMQP = objectMapper.readValue(jsonReceived, GenreViewAMQP.class);

            System.out.println(" [x] Received Genre Updated by AMQP: " + genreViewAMQP.getGenre());
            try {
                genreService.updateFromAMQP(genreViewAMQP);
                System.out.println(" [x] Genre synchronized from AMQP: " + genreViewAMQP.getGenre());
            } catch (Exception e) {
                System.out.println(" [x] Error syncing genre update: " + e.getMessage());
            }
        } catch (Exception ex) {
            System.out.println(" [x] Exception receiving genre event from AMQP: '" + ex.getMessage() + "'");
        }
    }

    @RabbitListener(queues = "#{autoDeleteQueue_Genre_Deleted.name}")
    public void receiveGenreDeletedMsg(Message msg) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            GenreViewAMQP genreViewAMQP = objectMapper.readValue(jsonReceived, GenreViewAMQP.class);

            System.out.println(" [x] Received Genre Deleted by AMQP: " + genreViewAMQP.getGenre());
            try {
                genreService.deleteFromAMQP(genreViewAMQP);
                System.out.println(" [x] Genre deleted from AMQP: " + genreViewAMQP.getGenre());
            } catch (Exception e) {
                System.out.println(" [x] Error deleting genre: " + e.getMessage());
            }
        } catch (Exception ex) {
            System.out.println(" [x] Exception receiving genre event from AMQP: '" + ex.getMessage() + "'");
        }
    }
}
