package pt.psoft.g1.psoftg1.genremanagement.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.shared.repositories.CacheRepository;

@RunWith(MockitoJUnitRunner.class)
public class GenreServiceImplTest {

  @Mock
  private GenreRepository genreRepository;

  @Mock
  private CacheRepository cacheRepository;

  @InjectMocks
  private GenreServiceImpl genreService;

  private Genre genre;

  @Before
  public void setup() {
    genre = new Genre("Fantasy");
  }

  @Test
  public void testFindByStringCacheHit() {
    when(cacheRepository.find(eq("genre:Fantasy"), any())).thenReturn(genre);

    genreService.findByString("Fantasy");

    verify(genreRepository, never()).findByString(anyString());
  }

  @Test
  public void testFindByStringCacheMiss() {
    when(cacheRepository.find(eq("genre:Fantasy"), any())).thenReturn(null);
    when(genreRepository.findByString("Fantasy")).thenReturn(Optional.of(genre));

    genreService.findByString("Fantasy");

    verify(cacheRepository).save(eq("genre:Fantasy"), eq(genre));
  }

  @Test
  public void testFindAllCacheHit() {
    List<Genre> cachedList = List.of(genre);
    when(cacheRepository.find(eq("genres:all"), any())).thenReturn(cachedList);

    genreService.findAll();

    verify(genreRepository, never()).findAll();
  }

  @Test
  public void testSaveGenre() {
    when(genreRepository.save(genre)).thenReturn(genre);
    genreService.save(genre);
    verify(genreRepository).save(genre);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetLendingsAverageDurationPerMonthInvalidDateFormat() {
    genreService.getLendingsAverageDurationPerMonth("2023-01-01", "invalid-date");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetLendingsAverageDurationPerMonthStartAfterEnd() {
    genreService.getLendingsAverageDurationPerMonth("2023-02-01", "2023-01-01");
  }

  @Test(expected = NotFoundException.class)
  public void testGetLendingsAverageDurationPerMonthEmptyList() {
    when(genreRepository.getLendingsAverageDurationPerMonth(any(), any()))
        .thenReturn(List.of());

    genreService.getLendingsAverageDurationPerMonth("2023-01-01", "2023-01-31");
  }
}
