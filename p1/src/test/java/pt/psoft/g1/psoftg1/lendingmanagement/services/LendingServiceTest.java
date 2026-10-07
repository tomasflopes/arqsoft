package pt.psoft.g1.psoftg1.lendingmanagement.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.LendingForbiddenException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.FineRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.CacheRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

@RunWith(MockitoJUnitRunner.class)
public class LendingServiceTest {
  @Mock
  private LendingRepository lendingRepository;
  @Mock
  private FineRepository fineRepository;
  @Mock
  private BookRepository bookRepository;
  @Mock
  private ReaderRepository readerRepository;
  @Mock
  private CacheRepository cacheRepository;

  @InjectMocks
  private LendingServiceImpl service;

  @Test
  public void shouldReturnCachedLendingIfPresent() {
    Lending lending = mock(Lending.class);
    when(cacheRepository.find(anyString(), any())).thenReturn(lending);

    Optional<Lending> result = service.findByLendingNumber("2026/1");

    assertTrue(result.isPresent());
    assertEquals(lending, result.get());
  }

  @Test
  public void shouldReturnFromRepositoryIfNotCached() {
    Lending lending = mock(Lending.class);
    when(cacheRepository.find(anyString(), any())).thenReturn(null);
    when(lendingRepository.findByLendingNumber("2026/1")).thenReturn(Optional.of(lending));

    Optional<Lending> result = service.findByLendingNumber("2026/1");

    assertTrue(result.isPresent());
  }

  @Test(expected = LendingForbiddenException.class)
  public void shouldThrowIfReaderHasLateBook() {
    Lending l = mock(Lending.class);
    when(l.getDaysDelayed()).thenReturn(5);
    List<Lending> outstanding = List.of(l);

    when(lendingRepository.listOutstandingByReaderNumber("R001")).thenReturn(outstanding);

    CreateLendingRequest req = new CreateLendingRequest();
    req.setReaderNumber("R001");
    req.setIsbn("123");
    service.create(req);
  }

  @Test(expected = LendingForbiddenException.class)
  public void shouldThrowIfReaderHasThreeOutstanding() {
    Lending l = mock(Lending.class);
    when(l.getDaysDelayed()).thenReturn(0);
    List<Lending> outstanding = List.of(l, l, l);

    when(lendingRepository.listOutstandingByReaderNumber("R001")).thenReturn(outstanding);

    CreateLendingRequest req = new CreateLendingRequest();
    req.setReaderNumber("R001");
    req.setIsbn("123");
    service.create(req);
  }

  @Test
  public void shouldCreateNewLendingSuccessfully() {
    Lending l = mock(Lending.class);
    Book b = mock(Book.class);
    ReaderDetails r = mock(ReaderDetails.class);

    when(lendingRepository.listOutstandingByReaderNumber("R001")).thenReturn(Collections.emptyList());
    when(bookRepository.findByIsbn("123")).thenReturn(Optional.of(b));
    when(readerRepository.findByReaderNumber("R001")).thenReturn(Optional.of(r));
    when(lendingRepository.getCountFromCurrentYear()).thenReturn(5);
    when(lendingRepository.save(any(Lending.class))).thenReturn(l);

    CreateLendingRequest req = new CreateLendingRequest();
    req.setReaderNumber("R001");
    req.setIsbn("123");

    Lending result = service.create(req);
    assertNotNull(result);
  }

  @Test
  public void shouldSetReturnedAndCreateFineIfLate() {
    Lending lending = mock(Lending.class);
    when(lendingRepository.findByLendingNumber("2026/1")).thenReturn(Optional.of(lending));
    when(lending.getDaysDelayed()).thenReturn(2);
    when(lendingRepository.save(any(Lending.class))).thenReturn(lending);

    SetLendingReturnedRequest req = new SetLendingReturnedRequest();
    req.setCommentary("returned");

    Lending result = service.setReturned("2026/1", req, 1L);
    assertNotNull(result);
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowIfLendingNotFoundOnSetReturned() {
    when(lendingRepository.findByLendingNumber("2026/1")).thenReturn(Optional.empty());
    service.setReturned("2026/1", new SetLendingReturnedRequest(), 1L);
  }

  @Test
  public void shouldReturnCachedAverageIfPresent() {
    when(cacheRepository.find(anyString(), any())).thenReturn(7.5);
    Double result = service.getAverageDuration();
    assertEquals(Double.valueOf(7.5), result);
  }

  @Test
  public void shouldCalculateAndFormatAverageIfNotCached() {
    when(cacheRepository.find(anyString(), any())).thenReturn(null);
    when(lendingRepository.getAverageDuration()).thenReturn(7.5678);
    Double result = service.getAverageDuration();
    assertEquals(Double.valueOf(7.6), result);
  }

  @Test
  public void shouldReturnCachedOverdueIfPresent() {
    List<Lending> cached = List.of(mock(Lending.class));
    when(cacheRepository.find(anyString(), any())).thenReturn(cached);

    List<Lending> result = service.getOverdue(new Page(1, 10));
    assertEquals(1, result.size());
  }

  @Test
  public void shouldQueryRepositoryIfCacheEmpty() {
    when(cacheRepository.find(anyString(), any())).thenReturn(null);
    when(lendingRepository.getOverdue(any())).thenReturn(List.of(mock(Lending.class)));

    List<Lending> result = service.getOverdue(new Page(1, 10));
    assertFalse(result.isEmpty());
  }

  @Test
  public void shouldReturnCachedAvgByIsbnIfPresent() {
    when(cacheRepository.find(anyString(), any())).thenReturn(3.0);
    Double result = service.getAvgLendingDurationByIsbn("123");
    assertEquals(Double.valueOf(3.0), result);
  }

  @Test
  public void shouldComputeAvgByIsbnIfNotCached() {
    when(cacheRepository.find(anyString(), any())).thenReturn(null);
    when(lendingRepository.getAvgLendingDurationByIsbn("123")).thenReturn(5.678);
    Double result = service.getAvgLendingDurationByIsbn("123");
    assertEquals(Double.valueOf(5.7), result);
  }

  @Test
  public void shouldReturnCachedSearchIfPresent() {
    List<Lending> cached = List.of(mock(Lending.class));
    when(cacheRepository.find(anyString(), any())).thenReturn(cached);
    List<Lending> result = service.searchLendings(new Page(1, 10), new SearchLendingQuery());
    assertEquals(1, result.size());
  }

  @Test
  public void shouldReturnFromRepositoryIfCacheEmpty() {
    when(cacheRepository.find(anyString(), any())).thenReturn(null);
    when(lendingRepository.searchLendings(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(mock(Lending.class)));

    List<Lending> result = service.searchLendings(new Page(1, 10),
        new SearchLendingQuery("R", "I", null, LocalDate.now().toString(), LocalDate.now().toString()));
    assertFalse(result.isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIfDateParsingFails() {
    SearchLendingQuery query = new SearchLendingQuery("R", "I", null, "notadate", "alsonotadate");
    service.searchLendings(new Page(1, 10), query);
  }
}
