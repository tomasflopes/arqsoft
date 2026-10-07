package pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;

@Repository("springDataAuthorRepository")
@Profile("sql | test")
public interface SpringDataAuthorRepository extends AuthorRepository, CrudRepository<Author, String> {
  @Override
  Optional<Author> findByAuthorNumber(String authorNumber);

  @Override
  @Query("SELECT new pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView(a.name.name, COUNT(l.pk)) " +
      "FROM Book b " +
      "JOIN b.authors a " +
      "JOIN Lending l ON l.book.pk = b.pk " +
      "GROUP BY a.name " +
      "ORDER BY COUNT(l) DESC")
  Page<AuthorLendingView> findTopAuthorByLendings(Pageable pageable);

  @Query("SELECT DISTINCT coAuthor FROM Book b " +
    "JOIN b.authors coAuthor " +
    "WHERE b IN (SELECT b FROM Book b JOIN b.authors a WHERE a.authorNumber = :authorNumber) " +
    "AND coAuthor.authorNumber <> :authorNumber")
List<Author> findCoAuthorsByAuthorNumber(String authorNumber); 
}
