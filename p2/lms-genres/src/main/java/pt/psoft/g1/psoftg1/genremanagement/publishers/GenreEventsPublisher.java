package pt.psoft.g1.psoftg1.genremanagement.publishers;

import pt.psoft.g1.psoftg1.genremanagement.api.GenreViewAMQP;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

public interface GenreEventsPublisher {
  GenreViewAMQP sendGenreCreated(Genre genre);

  GenreViewAMQP sendGenreUpdated(Genre genre, String oldGenreName, Long currentVersion);

  GenreViewAMQP sendGenreDeleted(Genre genre, Long currentVersion);
}
