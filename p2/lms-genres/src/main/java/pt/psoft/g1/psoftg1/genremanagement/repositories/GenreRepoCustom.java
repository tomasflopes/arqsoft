package pt.psoft.g1.psoftg1.genremanagement.repositories;

import java.time.LocalDate;
import java.util.List;

import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsDTO;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsPerMonthDTO;
import pt.psoft.g1.psoftg1.shared.services.Page;

public interface GenreRepoCustom {
  List<GenreLendingsPerMonthDTO> getLendingsPerMonthLastYearByGenre();

  List<GenreLendingsDTO> getAverageLendingsInMonth(LocalDate month, Page page);

  List<GenreLendingsPerMonthDTO> getLendingsAverageDurationPerMonth(LocalDate startDate, LocalDate endDate);
}
