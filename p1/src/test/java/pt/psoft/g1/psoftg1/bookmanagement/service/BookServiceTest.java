package pt.psoft.g1.psoftg1.bookmanagement.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookServiceImpl;
import pt.psoft.g1.psoftg1.bookmanagement.services.CreateBookRequest;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.external.adapter.isbn.IsbnFetcher;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.CacheRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BookServiceTest {
  @Mock
  private BookRepository bookRepository;
  @Mock
  private GenreRepository genreRepository;
  @Mock
  private AuthorRepository authorRepository;
  @Mock
  private PhotoRepository photoRepository;
  @Mock
  private ReaderRepository readerRepository;
  @Mock
  private CacheRepository cacheRepository;
  @Mock
  private IsbnFetcher isbnAdapter;

  @InjectMocks
  private BookServiceImpl bookService;

  @Test
  public void shouldCreateBookSuccessfully() {
    String isbn = "9782826012092";
    Genre genre = new Genre("Fantasy");
    Author author = mock(Author.class);

    CreateBookRequest req = new CreateBookRequest();
    req.setTitle("Encantos de Contar");
    req.setGenre("Fantasy");
    req.setDescription("Um livro mágico");
    req.setAuthors(Collections.singletonList("A1"));
    req.setPhotoURI("photo.png");
    req.setPhoto(mock(MultipartFile.class));

    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());
    when(authorRepository.findByAuthorNumber("A1")).thenReturn(Optional.of(author));
    when(genreRepository.findByString("Fantasy")).thenReturn(Optional.of(genre));

    Book expected = new Book(isbn, req.getTitle(), req.getDescription(), genre, List.of(author), req.getPhotoURI());
    when(bookRepository.save(any(Book.class))).thenReturn(expected);

    Book result = bookService.create(req, isbn);

    assertNotNull(result);
    assertEquals(isbn, result.getIsbn());
    assertEquals("Encantos de Contar", result.getTitle().toString());
    assertEquals(genre, result.getGenre());
    assertEquals(1, result.getAuthors().size());
  }

  @Test(expected = ConflictException.class)
  public void shouldThrowWhenBookAlreadyExists() {
    String isbn = "9782826012092";
    CreateBookRequest req = new CreateBookRequest();
    req.setTitle("Duplication");

    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(mock(Book.class)));

    bookService.create(req, isbn);
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowWhenGenreNotFound() {
    String isbn = "9782826012092";
    CreateBookRequest req = new CreateBookRequest();
    req.setTitle("Sem Género");
    req.setGenre("Unknown");
    req.setAuthors(Collections.singletonList("A1"));

    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());
    when(authorRepository.findByAuthorNumber("A1")).thenReturn(Optional.of(mock(Author.class)));
    when(genreRepository.findByString("Unknown")).thenReturn(Optional.empty());

    bookService.create(req, isbn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenAuthorsListIsEmpty() {
    String isbn = "9782826012092";
    CreateBookRequest req = new CreateBookRequest();
    req.setTitle("Autor Desconhecido");
    req.setGenre("Fantasy");
    req.setAuthors(Arrays.asList("A1", "A2"));

    Genre genre = new Genre("Fantasy");

    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());
    when(authorRepository.findByAuthorNumber("A1")).thenReturn(Optional.empty());
    when(authorRepository.findByAuthorNumber("A2")).thenReturn(Optional.empty());
    when(genreRepository.findByString("Fantasy")).thenReturn(Optional.of(genre));

    Book result = bookService.create(req, isbn);

    assertNotNull(result);
    assertEquals(0, result.getAuthors().size());
  }
}
