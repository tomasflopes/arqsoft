package pt.psoft.g1.psoftg1.genremanagement.infrastructure.publishers.impl;

import java.util.logging.Logger;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.genremanagement.api.GenreViewAMQP;
import pt.psoft.g1.psoftg1.genremanagement.api.GenreViewAMQPMapper;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.publishers.GenreEventsPublisher;
import pt.psoft.g1.psoftg1.shared.model.GenreEvents;

@Service
@RequiredArgsConstructor
public class GenreEventsRabbitmqPublisherImpl implements GenreEventsPublisher {
  private static final Logger LOGGER = Logger.getLogger(GenreEventsRabbitmqPublisherImpl.class.getName());

  @Autowired
  private RabbitTemplate template;
  @Autowired
  private DirectExchange direct;
  @Autowired
  private final GenreViewAMQPMapper genreViewAMQPMapper;

  @Override
  public GenreViewAMQP sendGenreCreated(Genre genre) {
    return sendGenreEvent(genre, null, 1L, GenreEvents.GENRE_CREATED);
  }

  @Override
  public GenreViewAMQP sendGenreUpdated(Genre genre, String oldGenreName, Long currentVersion) {
    return sendGenreEvent(genre, oldGenreName, currentVersion, GenreEvents.GENRE_UPDATED);
  }

  @Override
  public GenreViewAMQP sendGenreDeleted(Genre genre, Long currentVersion) {
    return sendGenreEvent(genre, null, currentVersion, GenreEvents.GENRE_DELETED);
  }

  private GenreViewAMQP sendGenreEvent(Genre genre, String oldGenreName, Long currentVersion, GenreEvents genreEventType) {
    LOGGER.info("Send Genre event to AMQP Broker: " + genre.getGenre());

    try {
      GenreViewAMQP genreViewAMQP = genreViewAMQPMapper.toGenreViewAMQP(genre);
      if (oldGenreName != null) {
        genreViewAMQP.setOldGenre(oldGenreName);
      }
      genreViewAMQP.setVersion(currentVersion);

      ObjectMapper objectMapper = new ObjectMapper();
      String genreViewAMQPinString = objectMapper.writeValueAsString(genreViewAMQP);

      this.template.convertAndSend(direct.getName(), genreEventType.getEventName(), genreViewAMQPinString);

      return genreViewAMQP;
    } catch (Exception ex) {
      LOGGER.severe("Exception sending genre event: " + ex.getMessage());
      return null;
    }
  }
}
