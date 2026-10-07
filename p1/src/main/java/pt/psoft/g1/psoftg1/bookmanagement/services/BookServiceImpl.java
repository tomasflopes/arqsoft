package pt.psoft.g1.psoftg1.bookmanagement.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;

import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.Isbn;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.external.adapter.isbn.IsbnFetcher;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.CacheRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

@Service
@PropertySource({ "classpath:config/library.properties" })
public class BookServiceImpl implements BookService {

  private final BookRepository bookRepository;
  private final GenreRepository genreRepository;
  private final AuthorRepository authorRepository;
  private final PhotoRepository photoRepository;
  private final ReaderRepository readerRepository;
  private final CacheRepository cacheRepository;
  private final IsbnFetcher isbnAdapter;

  @Value("${suggestionsLimitPerGenre}")
  private long suggestionsLimitPerGenre;

  public BookServiceImpl(
      BookRepository bookRepository,
      GenreRepository genreRepository,
      AuthorRepository authorRepository,
      PhotoRepository photoRepository,
      ReaderRepository readerRepository,
      @Autowired(required = false) @Qualifier("springDataBookRedisRepository") CacheRepository cacheRepository,
      IsbnFetcher isbnAdapter) {
    this.bookRepository = bookRepository;
    this.genreRepository = genreRepository;
    this.authorRepository = authorRepository;
    this.photoRepository = photoRepository;
    this.readerRepository = readerRepository;
    this.cacheRepository = cacheRepository;
    this.isbnAdapter = isbnAdapter;

  }

  @Override
  public Book create(CreateBookRequest request) {
    Isbn isbn = isbnAdapter.fetchIsbnByTitle(request.getTitle());
    return create(request, isbn.toString());
  }

  @Override
  public Book create(CreateBookRequest request, String isbn) {

    if (bookRepository.findByIsbn(isbn).isPresent()) {
      throw new ConflictException("Book with ISBN " + isbn + " already exists");
    }

    List<String> authorNumbers = request.getAuthors();
    List<Author> authors = new ArrayList<>();
    for (String authorNumber : authorNumbers) {
      Optional<Author> temp = authorRepository.findByAuthorNumber(authorNumber);
      if (temp.isEmpty()) {
        continue;
      }

      Author author = temp.get();
      authors.add(author);
    }

    MultipartFile photo = request.getPhoto();
    String photoURI = request.getPhotoURI();
    if (photo == null && photoURI != null || photo != null && photoURI == null) {
      request.setPhoto(null);
      request.setPhotoURI(null);
    }

    final var genre = genreRepository.findByString(request.getGenre())
        .orElseThrow(() -> new NotFoundException("Genre not found"));

    Book newBook = new Book(isbn, request.getTitle(), request.getDescription(), genre, authors, photoURI);

    return bookRepository.save(newBook);
  }

  @Override
  public Book update(UpdateBookRequest request, String currentVersion) {

    var book = findByIsbn(request.getIsbn());
    if (request.getAuthors() != null) {
      List<String> authorNumbers = request.getAuthors();
      List<Author> authors = new ArrayList<>();
      for (String authorNumber : authorNumbers) {
        Optional<Author> temp = authorRepository.findByAuthorNumber(authorNumber);
        if (temp.isEmpty()) {
          continue;
        }
        Author author = temp.get();
        authors.add(author);
      }

      request.setAuthorObjList(authors);
    }

    MultipartFile photo = request.getPhoto();
    String photoURI = request.getPhotoURI();
    if (photo == null && photoURI != null || photo != null && photoURI == null) {
      request.setPhoto(null);
      request.setPhotoURI(null);
    }

    if (request.getGenre() != null) {
      Optional<Genre> genre = genreRepository.findByString(request.getGenre());
      if (genre.isEmpty()) {
        throw new NotFoundException("Genre not found");
      }
      request.setGenreObj(genre.get());
    }

    book.applyPatch(Long.parseLong(currentVersion), request);

    bookRepository.save(book);

    return book;
  }

  @Override
  public Book save(Book book) {
    return this.bookRepository.save(book);
  }

  @Override
  public List<BookCountDTO> findTop5BooksLent() {
    String cacheKey = "top5BooksLent";
    List<BookCountDTO> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new TypeReference<List<BookCountDTO>>() {
      });
      if (cached != null && !cached.isEmpty()) {
        return cached;
      }
    }
    LocalDate oneYearAgo = LocalDate.now().minusYears(1);
    Pageable pageableRules = PageRequest.of(0, 5);
    List<BookCountDTO> books = this.bookRepository.findTop5BooksLent(oneYearAgo, pageableRules).getContent();
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, books);
    }
    return books;
  }

  @Override
  public Book removeBookPhoto(String isbn, long desiredVersion) {
    Book book = this.findByIsbn(isbn);
    String photoFile;
    try {
      photoFile = book.getPhoto().getPhotoFile();
    } catch (NullPointerException e) {
      throw new NotFoundException("Book did not have a photo assigned to it.");
    }

    book.removePhoto(desiredVersion);
    var updatedBook = bookRepository.save(book);
    photoRepository.deleteByPhotoFile(photoFile);
    return updatedBook;
  }

  @Override
  public List<Book> findByGenre(String genre) {
    String cacheKey = "genre:" + genre;
    List<Book> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new TypeReference<List<Book>>() {
      });
      if (cached != null && !cached.isEmpty()) {
        return cached;
      }
    }
    List<Book> books = bookRepository.findByGenre(genre);
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, books);
    }
    return books;
  }

  @Override
  public List<Book> findByTitle(String title) {
    String cacheKey = "title:" + title;
    List<Book> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new TypeReference<List<Book>>() {
      });
      if (cached != null && !cached.isEmpty()) {
        return cached;
      }
    }
    List<Book> books = bookRepository.findByTitle(title);
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, books);
    }
    return books;
  }

  @Override
  public List<Book> findByAuthorName(String authorName) {
    String cacheKey = "author:" + authorName;
    List<Book> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new TypeReference<List<Book>>() {
      });
      if (cached != null && !cached.isEmpty()) {
        return cached;
      }
    }
    List<Book> books = bookRepository.findByAuthorName(authorName + "%");
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, books);
    }
    return books;
  }

  @Override
  public Book findByIsbn(String isbn) {
    String cacheKey = "isbn:" + isbn;
    Book cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new TypeReference<Book>() {
      });
      if (cached != null) {
        return cached;
      }
    }
    Book dbBook = this.bookRepository.findByIsbn(isbn)
        .orElseThrow(() -> new NotFoundException(Book.class, isbn));
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, dbBook);
    }
    return dbBook;
  }

  public List<Book> getBooksSuggestionsForReader(String readerNumber) {
    List<Book> books = new ArrayList<>();

    ReaderDetails readerDetails = readerRepository.findByReaderNumber(readerNumber)
        .orElseThrow(() -> new NotFoundException("Reader not found with provided login"));
    List<Genre> interestList = readerDetails.getInterestList();

    if (interestList.isEmpty()) {
      throw new NotFoundException("Reader has no interests");
    }

    for (Genre genre : interestList) {
      List<Book> tempBooks = bookRepository.findByGenre(genre.toString());
      if (tempBooks.isEmpty()) {
        continue;
      }

      long genreBookCount = 0;

      for (Book loopBook : tempBooks) {
        if (genreBookCount >= suggestionsLimitPerGenre) {
          break;
        }

        books.add(loopBook);
        genreBookCount++;
      }
    }

    return books;
  }

  @Override
  public List<Book> searchBooks(Page page, SearchBooksQuery query) {
    if (page == null) {
      page = new Page(1, 10);
    }
    if (query == null) {
      query = new SearchBooksQuery("", "", "");
    }
    String cacheKey = "search:" + page.getNumber() + ":" + page.getLimit() + ":" +
        query.getTitle() + ":" + query.getGenre() + ":" + query.getAuthorName();
    List<Book> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new TypeReference<List<Book>>() {
      });
      if (cached != null && !cached.isEmpty()) {
        return cached;
      }
    }
    List<Book> books = bookRepository.searchBooks(page, query);
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, books);
    }
    return books;
  }
}
