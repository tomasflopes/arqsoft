package pt.psoft.g1.psoftg1.genremanagement.api;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsPerMonthDTO;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreService;
import pt.psoft.g1.psoftg1.genremanagement.services.GetAverageLendingsQuery;
import pt.psoft.g1.psoftg1.shared.api.ListResponse;
import pt.psoft.g1.psoftg1.shared.services.SearchRequest;

@RunWith(MockitoJUnitRunner.class)
public class GenreControllerTest {

  @Mock
  private GenreService genreService;

  @Mock
  private GenreViewMapper genreViewMapper;

  @InjectMocks
  private GenreController genreController;

  private GetAverageLendingsQuery query;
  private SearchRequest<GetAverageLendingsQuery> searchRequest;

  @Before
  public void setup() {
    query = new GetAverageLendingsQuery();
    searchRequest = new SearchRequest<>();
    searchRequest.setQuery(query);
  }

  @Test
  public void testGetAverageLendingsReturnsNotNull() {
    when(genreService.getAverageLendings(any(), any())).thenReturn(List.of());
    when(genreViewMapper.toGenreAvgLendingsView(any(List.class))).thenReturn(List.of());

    ListResponse<?> response = genreController.getAverageLendings(searchRequest);

    assertNotNull(response);
    assertNotNull(response.getItems());
  }

  @Test
  public void testGetTopReturnsNotNull() {
    // Return a list with one dummy element
    GenreBookCountDTO dummy = new GenreBookCountDTO("Fantasy", 10);
    when(genreService.findTopGenreByBooks()).thenReturn(List.of(dummy));
    when(genreViewMapper.toGenreBookCountView(any(List.class))).thenReturn(List.of());

    ListResponse<?> response = genreController.getTop();

    assertNotNull(response);
    assertNotNull(response.getItems());
  }

  @Test(expected = NotFoundException.class)
  public void testGetTopThrowsNotFoundIfEmpty() {
    when(genreService.findTopGenreByBooks()).thenReturn(List.of());

    genreController.getTop();
  }

  @Test
  public void testGetLendingsPerMonthLastYearReturnsNotNull() {
    GenreLendingsPerMonthDTO dummy = new GenreLendingsPerMonthDTO(2020, 1, List.of());
    when(genreService.getLendingsPerMonthLastYearByGenre()).thenReturn(List.of(dummy));
    when(genreViewMapper.toGenreLendingsCountPerMonthView(any(List.class))).thenReturn(List.of());

    ListResponse<?> response = genreController.getLendingsPerMonthLastYearByGenre();

    assertNotNull(response);
    assertNotNull(response.getItems());
  }

  @Test
  public void testGetLendingsAverageDurationPerMonthReturnsNotNull() {
    GenreLendingsPerMonthDTO dummy = new GenreLendingsPerMonthDTO(2020, 1, List.of());
    when(genreService.getLendingsAverageDurationPerMonth(anyString(), anyString()))
        .thenReturn(List.of(dummy));
    when(genreViewMapper.toGenreLendingsAveragePerMonthView(any(List.class))).thenReturn(List.of());

    ListResponse<?> response = genreController.getLendingsAverageDurationPerMonth("2023-01-01", "2023-01-31");

    assertNotNull(response);
    assertNotNull(response.getItems());
  }
}
