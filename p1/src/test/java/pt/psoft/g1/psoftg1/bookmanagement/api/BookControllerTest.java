package pt.psoft.g1.psoftg1.bookmanagement.api;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookService;
import pt.psoft.g1.psoftg1.bookmanagement.services.SearchBooksQuery;
import pt.psoft.g1.psoftg1.bookmanagement.services.UpdateBookRequest;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.shared.api.ListResponse;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.FileStorageService;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.shared.services.SearchRequest;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class BookControllerTest {
  @Mock
  private BookService bookService;
  @Mock
  private LendingService lendingService;
  @Mock
  private ConcurrencyService concurrencyService;
  @Mock
  private FileStorageService fileStorageService;
  @Mock
  private UserService userService;
  @Mock
  private ReaderService readerService;
  @Mock
  private BookViewMapper bookViewMapper;

  @InjectMocks
  private BookController controller;

  @Test
  public void updateBook_withoutIfMatchHeader_throwsBadRequest() {
    WebRequest request = mock(WebRequest.class);
    when(request.getHeader(ConcurrencyService.IF_MATCH)).thenReturn(null);

    UpdateBookRequest resource = new UpdateBookRequest();

    var ex = assertThrows(ResponseStatusException.class,
      () -> controller.updateBook("9782826012092", request, resource));

    Assertions.assertEquals("400 BAD_REQUEST \"You must issue a conditional PATCH using 'if-match'\"", ex.getMessage());
  }

  @Test
  public void updateBook_withValidHeader_returnsOk() {
    WebRequest request = mock(WebRequest.class);
    when(request.getHeader(ConcurrencyService.IF_MATCH)).thenReturn("5");

    Book mockBook = mock(Book.class);
    BookView mockBookView = mock(BookView.class);
    when(mockBook.getVersion()).thenReturn(10L);
    when(concurrencyService.getVersionFromIfMatchHeader("5")).thenReturn(5L);
    when(bookService.update(any(), any())).thenReturn(mockBook);
    when(bookViewMapper.toBookView(mockBook)).thenReturn(mockBookView);

    ResponseEntity<BookView> response =
      controller.updateBook("9782826012092", request, new UpdateBookRequest());

    Assertions.assertEquals(200, response.getStatusCodeValue());
    Assertions.assertEquals("\"10\"", response.getHeaders().getETag());
  }

  @Test
  public void findBooks_emptyResults_throwsNotFound() {
    when(bookService.findByTitle(anyString())).thenReturn(List.of());

    assertThrows(NotFoundException.class,
      () -> controller.findBooks("x", null, null));
  }

  @Test
  public void findByIsbn_returnsBookView() {
    Book book = mock(Book.class);
    when(book.getVersion()).thenReturn(7L);

    when(bookService.findByIsbn("9782826012092")).thenReturn(book);
    when(bookViewMapper.toBookView(book)).thenReturn(new BookView());

    var response = controller.findByIsbn("9782826012092");

    Assertions.assertEquals(200, response.getStatusCodeValue());
    Assertions.assertEquals("\"7\"", response.getHeaders().getETag());
  }

  @Test
  public void getBooksSuggestions_readerNotFound_throwsException() {
    Authentication auth = mock(Authentication.class);
    User user = mock(User.class);
    user.setUsername("john");
    when(userService.getAuthenticatedUser(auth)).thenReturn(user);

    assertThrows(NotFoundException.class,
      () -> controller.getBooksSuggestions(auth));
  }

  @Test
  public void shouldReturnAverageLendingDurationView_WhenBookExists() {
    String isbn = "9782826012092";
    Book book = mock(Book.class);
    Double avgDuration = 7.5;
    BookAverageLendingDurationView expectedView = new BookAverageLendingDurationView();

    when(bookService.findByIsbn(isbn)).thenReturn(book);
    when(lendingService.getAvgLendingDurationByIsbn(isbn)).thenReturn(avgDuration);
    when(bookViewMapper.toBookAverageLendingDurationView(book, avgDuration)).thenReturn(expectedView);

    ResponseEntity<BookAverageLendingDurationView> response = controller.getAvgLendingDurationByIsbn(isbn);

    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());
    assertSame(expectedView, response.getBody());
  }

  @Test
  public void shouldReturnOkWithNullBody_WhenBookHasNoData() {
    String isbn = "0000000000000";
    when(bookService.findByIsbn(isbn)).thenReturn(mock(Book.class));
    when(lendingService.getAvgLendingDurationByIsbn(isbn)).thenReturn(null);
    when(bookViewMapper.toBookAverageLendingDurationView(any(), any())).thenReturn(null);

    ResponseEntity<BookAverageLendingDurationView> response = controller.getAvgLendingDurationByIsbn(isbn);

    assertNotNull(response);
    assertEquals(200, response.getStatusCodeValue());
    assertNull(response.getBody());
  }

  @Test
  public void shouldReturnListResponseWithMappedBooks() {
    // given
    SearchBooksQuery query = new SearchBooksQuery();
    SearchRequest<SearchBooksQuery> request = new SearchRequest<>(new Page(1, 1), query);

    List<Book> books = Collections.singletonList(mock(Book.class));
    List<BookView> mappedViews = Collections.singletonList(new BookView());

    when(bookService.searchBooks(new Page(1, 1), query)).thenReturn(books);
    when(bookViewMapper.toBookView(books)).thenReturn(mappedViews);

    // when
    ListResponse<BookView> response = controller.searchBooks(request);

    // then
    assertNotNull(response);
    assertEquals(mappedViews, response.getItems());
    assertEquals(1, response.getItems().size());
  }

  @Test
  public void shouldReturnEmptyListResponse_WhenNoBooksFound() {
    SearchBooksQuery query = new SearchBooksQuery();
    SearchRequest<SearchBooksQuery> request = new SearchRequest<>(new Page(1, 1), query);

    when(bookService.searchBooks(new Page(1, 1), query)).thenReturn(Collections.emptyList());
    when(bookViewMapper.toBookView(Collections.emptyList())).thenReturn(Collections.emptyList());

    ListResponse<BookView> response = controller.searchBooks(request);

    assertNotNull(response);
    assertTrue(response.getItems().isEmpty());
  }
}
