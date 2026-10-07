package pt.psoft.g1.psoftg1.bookmanagement.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;

public interface BookRepository extends BookRepoCustom {
  List<Book> findByGenre(@Param("genre") String genre);

  List<Book> findByTitle(@Param("title") String title);

  List<Book> findByAuthorName(@Param("authorName") String authorName);

  Optional<Book> findByIsbn(@Param("isbn") String isbn);

  Page<BookCountDTO> findTop5BooksLent(@Param("oneYearAgo") LocalDate oneYearAgo, Pageable pageable);

  List<Book> findBooksByAuthorNumber(String authorNumber);

  Book save(Book book);

  void delete(Book book);
}
