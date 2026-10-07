package pt.psoft.g1.psoftg1.bookmanagement.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;

import pt.psoft.g1.psoftg1.bookmanagement.model.Book;

public interface BookRepository {
  List<Book> findByGenre(@Param("genre") String genre);

  List<Book> findByTitle(@Param("title") String title);

  Optional<Book> findByIsbn(@Param("isbn") String isbn);

  List<Book> findBooksByAuthorNumber(String authorNumber);

  Book save(Book book);

  void delete(Book book);
}
