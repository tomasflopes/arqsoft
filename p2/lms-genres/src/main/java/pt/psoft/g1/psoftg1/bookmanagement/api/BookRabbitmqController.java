package pt.psoft.g1.psoftg1.bookmanagement.api;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookService;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreService;

@Service
@RequiredArgsConstructor
public class BookRabbitmqController {
  private static final Logger LOGGER = Logger.getLogger(BookRabbitmqController.class.getName());

  private final BookService bookService;
  private final GenreService genreService;

  @RabbitListener(queues = "#{autoDeleteQueue_Book_Created.name}")
  public void receiveBookCreated(Message msg) {
    try {
      String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
      ObjectMapper objectMapper = new ObjectMapper();
      BookViewAMQP bookViewAMQP = objectMapper.readValue(jsonReceived, BookViewAMQP.class);

      LOGGER.info(
          "[Genres] Received Book Created: ISBN=" + bookViewAMQP.getIsbn() + ", Genre=" + bookViewAMQP.getGenre());
      
      // Create genre if it doesn't exist (genre from book event)
      String genreName = bookViewAMQP.getGenre();
      if (genreName != null && !genreName.isEmpty()) {
        if (genreService.findByString(genreName).isEmpty()) {
          Genre newGenre = new Genre(genreName);
          genreService.save(newGenre);
          LOGGER.info("[Genres] Created new genre from Book.Created event: " + genreName);
        }
      }
      
      try {
        bookService.create(bookViewAMQP);
      } catch (ConflictException e) {
        // Já existe: atualiza com a mesma carga
        bookService.update(bookViewAMQP);
      }

    } catch (Exception ex) {
      LOGGER.severe("[Genres] Exception receiving book created event: " + ex.getMessage());
    }
  }

  @RabbitListener(queues = "#{autoDeleteQueue_Book_Updated.name}")
  public void receiveBookUpdated(Message msg) {
    try {
      String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
      ObjectMapper objectMapper = new ObjectMapper();
      BookViewAMQP bookViewAMQP = objectMapper.readValue(jsonReceived, BookViewAMQP.class);

      LOGGER.info(
          "[Genres] Received Book Updated: ISBN=" + bookViewAMQP.getIsbn() + ", Genre=" + bookViewAMQP.getGenre());

      try {
        bookService.update(bookViewAMQP);
      } catch (NotFoundException e) {
        // Não existe ainda: cria
        bookService.create(bookViewAMQP);
      }

    } catch (Exception ex) {
      LOGGER.severe("[Genres] Exception receiving book updated event: " + ex.getMessage());
    }
  }

  @RabbitListener(queues = "#{autoDeleteQueue_Book_Deleted.name}")
  public void receiveBookDeleted(Message msg) {
    try {
      String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
      ObjectMapper objectMapper = new ObjectMapper();
      BookViewAMQP bookViewAMQP = objectMapper.readValue(jsonReceived, BookViewAMQP.class);

      LOGGER.info("[Genres] Received Book Deleted: ISBN=" + bookViewAMQP.getIsbn());

      bookService.deleteByIsbn(bookViewAMQP.getIsbn());

    } catch (Exception ex) {
      LOGGER.severe("[Genres] Exception receiving book deleted event: " + ex.getMessage());
    }
  }
}
