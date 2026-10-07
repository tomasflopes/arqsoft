package pt.psoft.g1.psoftg1.genremanagement.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
  import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreBookCountDTO;

@NoRepositoryBean
public interface GenreRepository extends CrudRepository<Genre, Integer>, GenreRepoCustom {
  Iterable<Genre> findAll();

  @Query("SELECT g FROM Genre g WHERE g.genre = :genreName")
  Optional<Genre> findByString(@Param("genreName") String genreName);

  Genre save(Genre genre);

  Page<GenreBookCountDTO> findTop5GenreByBookCount(Pageable pageable);

  void delete(Genre genre);
}
