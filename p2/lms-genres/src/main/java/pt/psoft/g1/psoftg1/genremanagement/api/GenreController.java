package pt.psoft.g1.psoftg1.genremanagement.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreService;
import pt.psoft.g1.psoftg1.genremanagement.services.GetAverageLendingsQuery;
import pt.psoft.g1.psoftg1.shared.api.ListResponse;
import pt.psoft.g1.psoftg1.shared.services.SearchRequest;

@Tag(name = "Genres", description = "Endpoints for managing Genres")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/genres")
public class GenreController {
  private final GenreService genreService;
  private final GenreViewMapper genreViewMapper;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public GenreView createGenre(@Valid @RequestBody final CreateGenreRequest request) {
    final Genre genre = genreService.save(new Genre(request.getGenre()));
    return genreViewMapper.toGenreView(genre);
  }

  @PutMapping("/{genre}")
  public GenreView updateGenre(@PathVariable("genre") final String currentName,
      @Valid @RequestBody final CreateGenreRequest request) {
    final Genre updated = genreService.update(currentName, request.getGenre());
    return genreViewMapper.toGenreView(updated);
  }

  @DeleteMapping("/{genre}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteGenre(@PathVariable("genre") final String genre) {
    genreService.deleteByName(genre);
  }

  @PostMapping(value = "/avgLendingsPerGenre")
  public ListResponse<GenreLendingsView> getAverageLendings(
      @Valid @RequestBody final SearchRequest<GetAverageLendingsQuery> query) {
    final var list = genreService.getAverageLendings(query.getQuery(), query.getPage());
    return new ListResponse<>(genreViewMapper.toGenreAvgLendingsView(list));
  }

  @GetMapping("/top5")
  public ListResponse<GenreBookCountView> getTop() {
    final var list = genreService.findTopGenreByBooks();

    if (list.isEmpty())
      throw new NotFoundException("No genres to show");

    return new ListResponse<>(genreViewMapper.toGenreBookCountView(list));
  }

  @GetMapping("/lendingsPerMonthLastTwelveMonths")
  public ListResponse<GenreLendingsCountPerMonthView> getLendingsPerMonthLastYearByGenre() {
    final var list = genreService.getLendingsPerMonthLastYearByGenre();

    if (list.isEmpty())
      throw new NotFoundException("No genres to show");

    final var viewList = genreViewMapper.toGenreLendingsCountPerMonthView(list);

    return new ListResponse<>(viewList);
  }

  @GetMapping("/lendingsAverageDurationPerMonth")
  public ListResponse<GenreLendingsAvgPerMonthView> getLendingsAverageDurationPerMonth(
      @RequestParam("startDate") final String start,
      @RequestParam("endDate") final String end) {
    final var list = genreService.getLendingsAverageDurationPerMonth(start, end);

    if (list.isEmpty())
      throw new NotFoundException("No genres to show");

    final var viewList = genreViewMapper.toGenreLendingsAveragePerMonthView(list);

    return new ListResponse<>(viewList);
  }
}
