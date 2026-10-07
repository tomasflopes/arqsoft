package pt.psoft.g1.psoftg1.genremanagement.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

public interface GenreRepository extends GenreRepoCustom {
  Iterable<Genre> findAll();

  Optional<Genre> findByString(String genreName);

  Genre save(Genre genre);

  Page<GenreBookCountDTO> findTop5GenreByBookCount(Pageable pageable);

  void delete(Genre genre);
}
