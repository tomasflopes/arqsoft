package pt.psoft.g1.psoftg1.genremanagement.api;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreService;

@Service
@RequiredArgsConstructor
public class GenreRabbitmqController {
  private static final Logger LOGGER = Logger.getLogger(GenreRabbitmqController.class.getName());
  private final GenreService genreService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @RabbitListener(queues = "#{selfQueue_Genre_Created.name}")
  public void receiveGenreCreatedMsg(Message msg) {
    try {
      String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
      GenreViewAMQP genreViewAMQP = objectMapper.readValue(jsonReceived, GenreViewAMQP.class);

      try {
        genreService.save(genreViewAMQP);
        LOGGER.info("[self] Genre created from AMQP: " + genreViewAMQP.getGenre());
      } catch (Exception e) {
        LOGGER.info("[self] Genre already exists: " + genreViewAMQP.getGenre());
      }
    } catch (Exception ex) {
      LOGGER.severe("[self] Exception on genre created event: " + ex.getMessage());
    }
  }

  @RabbitListener(queues = "#{selfQueue_Genre_Updated.name}")
  public void receiveGenreUpdatedMsg(Message msg) {
    try {
      String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
      GenreViewAMQP genreViewAMQP = objectMapper.readValue(jsonReceived, GenreViewAMQP.class);

      try {
        genreService.update(genreViewAMQP.getOldGenre(), genreViewAMQP.getGenre());
        LOGGER.info("[self] Genre updated from AMQP: " + genreViewAMQP.getOldGenre() + " -> " + genreViewAMQP.getGenre());
      } catch (Exception e) {
        LOGGER.info("[self] Genre update skipped: " + e.getMessage());
      }
    } catch (Exception ex) {
      LOGGER.severe("[self] Exception on genre updated event: " + ex.getMessage());
    }
  }

  @RabbitListener(queues = "#{selfQueue_Genre_Deleted.name}")
  public void receiveGenreDeletedMsg(Message msg) {
    try {
      String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
      GenreViewAMQP genreViewAMQP = objectMapper.readValue(jsonReceived, GenreViewAMQP.class);

      genreService.findByString(genreViewAMQP.getGenre()).ifPresent(g -> {
        genreService.deleteByName(g.getGenre());
        LOGGER.info("[self] Genre deleted from AMQP: " + genreViewAMQP.getGenre());
      });
    } catch (Exception ex) {
      LOGGER.severe("[self] Exception on genre deleted event: " + ex.getMessage());
    }
  }
}
