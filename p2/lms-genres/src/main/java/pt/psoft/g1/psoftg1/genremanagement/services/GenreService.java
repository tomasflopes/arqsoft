package pt.psoft.g1.psoftg1.genremanagement.services;

import java.util.List;
import java.util.Optional;

import pt.psoft.g1.psoftg1.genremanagement.api.GenreViewAMQP;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.shared.services.Page;

public interface GenreService {
  Iterable<Genre> findAll();

  Genre save(Genre genre);

  Genre save(GenreViewAMQP genreViewAMQP); // AMQP request

  Optional<Genre> findByString(String name);

  List<GenreBookCountDTO> findTopGenreByBooks();

  List<GenreLendingsDTO> getAverageLendings(GetAverageLendingsQuery query, Page page);

  List<GenreLendingsPerMonthDTO> getLendingsPerMonthLastYearByGenre();

  List<GenreLendingsPerMonthDTO> getLendingsAverageDurationPerMonth(String startDate, String endDate);

  void deleteByName(String genreName);

  Genre update(String currentName, String newName);
}
