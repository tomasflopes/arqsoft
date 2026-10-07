package pt.psoft.g1.psoftg1.lendingmanagement.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
public class LendingRepositoryIT {
  @Autowired
  private LendingRepository lendingRepository;
  @Autowired
  private BookRepository bookRepository;
  @Autowired
  private GenreRepository genreRepository;

  private Lending lending;
  private String readerNumber;
  private Book book;
  private String author;
  private Genre genre;

  @BeforeEach
  public void setUp() {
    author = "Manuel Antonio Pina";

    genre = new Genre("Género");
    if (genreRepository.findByString(genre.getGenre()).isEmpty())
      genreRepository.save(genre);

    List<String> authors = List.of(author);
    book = new Book("9782826012092",
        "O Inspetor Max",
        "conhecido pastor-alemão que trabalha para a Judiciária, vai ser fundamental para resolver um importante caso de uma rede de malfeitores que quer colocar uma bomba num megaconcerto de uma ilustre cantora",
        genre,
        authors,
        null);
    if (bookRepository.findByIsbn(book.getIsbn()).isEmpty())
      bookRepository.save(book);

    readerNumber = "1";

    // Create and save the lending
    lending = Lending.newBootstrappingLending(book,
        readerNumber,
        "2025/999",
        LocalDate.of(LocalDate.now().getYear(), 1, 1),
        LocalDate.of(LocalDate.now().getYear(), 1, 11),
        15,
        300);
    if (lendingRepository.findByLendingNumber(lending.getLendingNumber()).isEmpty())
      lendingRepository.save(lending);
  }

  @AfterEach
  public void tearDown() {
    lendingRepository.delete(lending);
    bookRepository.delete(book);
    genreRepository.delete(genre);
  }

  @Test
  public void testSave() {
    Lending newLending = new Lending(lending.getBook(), lending.getReaderNumber(), "2025/2", 14, 50);
    Lending savedLending = lendingRepository.save(newLending);
    assertThat(savedLending).isNotNull();
    assertThat(savedLending.getLendingNumber()).isEqualTo(newLending.getLendingNumber());
    lendingRepository.delete(savedLending);
  }

  @Test
  public void testFindByLendingNumber() {
    String ln = lending.getLendingNumber();
    Optional<Lending> found = lendingRepository.findByLendingNumber(ln);
    assertThat(found).isPresent();
    assertThat(found.get().getLendingNumber()).isEqualTo(ln);
  }

  @Test
  public void testListByReaderNumberAndIsbn() {
    List<Lending> lendings = lendingRepository.listByReaderNumberAndIsbn(lending.getReaderNumber(),
        lending.getBook().getIsbn());
    assertThat(lendings).isNotEmpty();
    assertThat(lendings).contains(lending);
  }
}
