package pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;

@Repository("springDataBookRepository")
public interface SpringDataBookRepository extends BookRepository, CrudRepository<Book, String> {
  @Query("SELECT b " +
      "FROM Book b " +
      "WHERE b.isbn = :isbn")
  Optional<Book> findByIsbn(@Param("isbn") String isbn);

  @Override
  @Query("SELECT b " +
      "FROM Book b " +
      "WHERE b.genre.genre LIKE %:genre%")
  List<Book> findByGenre(@Param("genre") String genre);

  @Override
  @Query("SELECT b FROM Book b WHERE b.title LIKE %:title%")
  List<Book> findByTitle(@Param("title") String title);

  @Override
  @Query(value = "SELECT b.* " +
      "FROM book b " +
      "JOIN book_authors ba ON b.pk = ba.book_pk " +
      "JOIN author a ON ba.authors_author_number = a.author_number " +
      "WHERE a.author_number = :authorNumber", nativeQuery = true)
  List<Book> findBooksByAuthorNumber(@Param("authorNumber") String authorNumber);
}
