package pt.psoft.g1.psoftg1.lendingmanagement.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

@PropertySource({ "classpath:config/library.properties" })
class LendingTest {
  private static final ArrayList<String> authors = new ArrayList<>();
  private static Book book;
  @Value("${lendingDurationInDays}")
  private int lendingDurationInDays;
  @Value("${fineValuePerDayInCents}")
  private int fineValuePerDayInCents;
  private String readerNumber = "1";

  @BeforeAll
  public static void setup() {
    String author = "Manuel Antonio Pina";
    authors.add(author);
    book = new Book("9782826012092",
        "O Inspetor Max",
        "conhecido pastor-alemão que trabalha para a Judiciária, vai ser fundamental para resolver um importante caso de uma rede de malfeitores que quer colocar uma bomba num megaconcerto de uma ilustre cantora",
        new Genre("Romance"),
        authors,
        null);
  }

  @Test
  void ensureBookNotNull() {
    assertThrows(IllegalArgumentException.class,
        () -> new Lending(null, readerNumber, "2025/1", lendingDurationInDays, fineValuePerDayInCents));
  }

  @Test
  void ensureReaderNotNull() {
    assertThrows(IllegalArgumentException.class,
        () -> new Lending(book, null, "2025/1", lendingDurationInDays, fineValuePerDayInCents));
  }

  @Test
  void testSetReturned() {
    Lending lending = new Lending(book, readerNumber, "2025/1", lendingDurationInDays, fineValuePerDayInCents);
    lending.setReturned(0, null);
    assertEquals(LocalDate.now(), lending.getReturnedDate());
  }

  @Test
  void testGetDaysDelayed() {
    Lending lending = new Lending(book, readerNumber, "2025/1", lendingDurationInDays, fineValuePerDayInCents);
    assertEquals(0, lending.getDaysDelayed());
  }

  @Test
  void testGetDaysUntilReturn() {
    Lending lending = new Lending(book, readerNumber, "2025/1", lendingDurationInDays, fineValuePerDayInCents);
    assertEquals(Optional.of(lendingDurationInDays), lending.getDaysUntilReturn());
  }

  @Test
  void testGetDaysOverDue() {
    Lending lending = new Lending(book, readerNumber, "2025/1", lendingDurationInDays, fineValuePerDayInCents);
    assertEquals(Optional.empty(), lending.getDaysOverdue());
  }

  @Test
  void testGetTitle() {
    Lending lending = new Lending(book, readerNumber, "2025/1", lendingDurationInDays, fineValuePerDayInCents);
    assertEquals("O Inspetor Max", lending.getTitle());
  }

  @Test
  void testGetLendingNumber() {
    Lending lending = new Lending(book, readerNumber, "2025/1", lendingDurationInDays, fineValuePerDayInCents);
    assertEquals("2025/1", lending.getLendingNumber());
  }

  @Test
  void testGetBook() {
    Lending lending = new Lending(book, readerNumber, "2025/1", lendingDurationInDays, fineValuePerDayInCents);
    assertEquals(book, lending.getBook());
  }

  @Test
  void testGetReaderNumber() {
    Lending lending = new Lending(book, readerNumber, "2025/1", lendingDurationInDays, fineValuePerDayInCents);
    assertEquals(readerNumber, lending.getReaderNumber());
  }

  @Test
  void testGetStartDate() {
    Lending lending = new Lending(book, readerNumber, "2025/1", lendingDurationInDays, fineValuePerDayInCents);
    assertEquals(LocalDate.now(), lending.getStartDate());
  }

  @Test
  void testGetLimitDate() {
    Lending lending = new Lending(book, readerNumber, "2025/1", lendingDurationInDays, fineValuePerDayInCents);
    assertEquals(LocalDate.now().plusDays(lendingDurationInDays), lending.getLimitDate());
  }

  @Test
  void testGetReturnedDate() {
    Lending lending = new Lending(book, readerNumber, "2025/1", lendingDurationInDays, fineValuePerDayInCents);
    assertNull(lending.getReturnedDate());
  }

}
