package pt.psoft.g1.psoftg1.bookmanagement.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQP;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;

@Service
@RequiredArgsConstructor
@PropertySource({ "classpath:config/library.properties" })
public class BookServiceImpl implements BookService {
  private final BookRepository bookRepository;
  private final GenreRepository genreRepository;

  @Value("${suggestionsLimitPerGenre}")
  private long suggestionsLimitPerGenre;

  @Override
  public Book create(BookViewAMQP bookViewAMQP) {

    final String isbn = bookViewAMQP.getIsbn();
    final String description = bookViewAMQP.getDescription();
    final String title = bookViewAMQP.getTitle();
    final String photoURI = null;
    final String genre = bookViewAMQP.getGenre();
    // Authors are not used in Genres; keep an empty list to avoid null issues
    Book bookCreated = create(isbn, title, description, photoURI, genre, List.of());

    return bookCreated;
  }

  private Book create(String isbn,
      String title,
      String description,
      String photoURI,
      String genreName,
      List<String> authors) {
    if (bookRepository.findByIsbn(isbn).isPresent()) {
      throw new ConflictException("Book with ISBN " + isbn + " already exists");
    }

    final Genre genre = genreRepository.findByString(String.valueOf(genreName))
        .orElseThrow(() -> new NotFoundException("Genre not found"));

    Book newBook = new Book(isbn, title, description, genre, authors, photoURI);

    Book savedBook = bookRepository.save(newBook);

    return savedBook;
  }

  @Override
  public Book update(BookViewAMQP bookViewAMQP) {

    final Long version = bookViewAMQP.getVersion();
    final String isbn = bookViewAMQP.getIsbn();
    final String description = bookViewAMQP.getDescription();
    final String title = bookViewAMQP.getTitle();
    final String photoURI = null;
    final String genre = bookViewAMQP.getGenre();
    var book = findByIsbn(isbn);
    Book bookUpdated = update(book, version, title, description, photoURI, genre, List.of());
    return bookUpdated;
  }

  private Book update(Book book,
      Long currentVersion,
      String title,
      String description,
      String photoURI,
      String genreId,
      List<String> authors) {

    Genre genreObj = null;
    if (genreId != null) {
      Optional<Genre> genre = genreRepository.findByString(genreId);
      if (genre.isEmpty()) {
        throw new NotFoundException("Genre not found");
      }
      genreObj = genre.get();
    }

    book.applyPatch(currentVersion, title, description, photoURI, genreObj, authors);

    Book updatedBook = bookRepository.save(book);

    return updatedBook;
  }

  @Override
  public List<Book> findByGenre(String genre) {
    return this.bookRepository.findByGenre(genre);
  }

  public List<Book> findByTitle(String title) {
    return bookRepository.findByTitle(title);
  }

  public Book findByIsbn(String isbn) {
    return this.bookRepository.findByIsbn(isbn).orElseThrow(() -> new NotFoundException(Book.class, isbn));
  }

  @Override
  public void deleteByIsbn(String isbn) {
    this.bookRepository.findByIsbn(isbn).ifPresent(bookRepository::delete);
  }
}
