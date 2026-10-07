package pt.psoft.g1.psoftg1.readermanagement.api;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import pt.psoft.g1.psoftg1.external.service.ApiNinjasService;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingViewMapper;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.FileStorageService;
import pt.psoft.g1.psoftg1.usermanagement.model.Librarian;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;

@RunWith(MockitoJUnitRunner.class)
public class ReaderControllerTest {

  @Mock
  private ReaderService readerService;
  @Mock
  private UserService userService;
  @Mock
  private ReaderViewMapper readerViewMapper;
  @Mock
  private LendingService lendingService;
  @Mock
  private LendingViewMapper lendingViewMapper;
  @Mock
  private ConcurrencyService concurrencyService;
  @Mock
  private FileStorageService fileStorageService;
  @Mock
  private ApiNinjasService apiNinjasService;

  @InjectMocks
  private ReaderController readerController;

  @Mock
  private Authentication auth;

  private User librarian;
  private ReaderDetails dummyReader;
  private Reader reader;

  @Before
  public void setup() {
    librarian = Librarian.newLibrarian("maria@gmail.com", "Mariaroberta!123", "Maria Roberta");
    reader = Reader.newReader("manuel@gmail.com", "Manuelino123!", "Manuel Sarapinto das Coives");
    dummyReader = new ReaderDetails(
        1,
        reader,
        "2000-01-01",
        "919191919",
        true,
        true,
        true,
        "readerPhotoTest.jpg",
        new ArrayList<>());
  }

  @Test
  public void testGetDataReturnsNotNullForLibrarian() {
    when(userService.getAuthenticatedUser(auth)).thenReturn(librarian);
    when(readerService.findAll()).thenReturn(List.of(dummyReader));
    when(readerViewMapper.toReaderView(anyList())).thenReturn(List.of());

    ResponseEntity<?> response = readerController.getData(auth);
    assertNotNull(response);
  }

  @Test
  public void testFindByReaderNumberReturnsNotNull() {
    when(readerService.findByReaderNumber(anyString())).thenReturn(Optional.of(dummyReader));
    when(readerViewMapper.toReaderQuoteView(any())).thenReturn(new ReaderQuoteView());

    ResponseEntity<?> response = readerController.findByReaderNumber(2026, 1);
    assertNotNull(response);
  }

  @Test
  public void testSearchReadersByPhoneReturnsNotNull() {
    when(readerService.findByPhoneNumber(anyString())).thenReturn(List.of(dummyReader));
    when(readerViewMapper.toReaderView(anyList())).thenReturn(List.of());

    ResponseEntity<?> response = readerController.searchReaders(null, "123456789", auth);
    assertNotNull(response);
  }

  @Test
  public void testGetTopReturnsNotNull() {
    when(readerService.findTopReaders(5)).thenReturn(List.of(dummyReader));
    when(readerViewMapper.toReaderView(anyList())).thenReturn(List.of());

    assertNotNull(readerController.getTop());
  }

  @Test
  public void testGetTop5ReaderByGenreReturnsNotNull() {
    when(readerService.findTopByGenre(anyString(), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of(new ReaderBookCountDTO()));
    when(readerViewMapper.toReaderCountViewList(anyList())).thenReturn(List.of());

    assertNotNull(readerController.getTop5ReaderByGenre("Fantasy", LocalDate.now(), LocalDate.now()));
  }
}
