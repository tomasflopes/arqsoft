package pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;

import java.time.LocalDate;
import java.util.*;

/**
 * Repository para Genre - simplificado para microserviço de Readers
 * Funcionalidades de Book e Lending foram removidas pois pertencem a outros microserviços
 */
@Repository
@Primary
public interface SpringDataGenreRepository extends GenreRepository, GenreRepoCustom {

    @Query("SELECT g FROM Genre g")
    List<Genre> findAllGenres();

    @Override
    @Query("SELECT g FROM Genre g WHERE g.genre = :genreName" )
    Optional<Genre> findByString(@Param("genreName")@NotNull String genre);

    /* REMOVIDO - Book management não faz parte deste microserviço
    @Override
    @Query("SELECT new pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO(g.genre, COUNT(b))" +
            "FROM Genre g " +
            "JOIN Book b ON b.genre.pk = g.pk " +
            "GROUP BY g " +
            "ORDER BY COUNT(b) DESC")
    Page<GenreBookCountDTO> findTop5GenreByBookCount(Pageable pageable);
    */
}


interface GenreRepoCustom{
    /* REMOVIDO - Lending management não faz parte deste microserviço
     * Estas estatísticas devem ser obtidas do microserviço de lendings
    List<GenreLendingsPerMonthDTO> getLendingsPerMonthLastYearByGenre();
    List<GenreLendingsDTO> getAverageLendingsInMonth(LocalDate month, pt.psoft.g1.psoftg1.shared.services.Page page);
    List<GenreLendingsPerMonthDTO> getLendingsAverageDurationPerMonth(LocalDate startDate, LocalDate endDate);
    */
}

/* REMOVIDO - GenreRepoCustomImpl usa entidades Book e Lending que não existem neste microserviço
 * Estas funcionalidades de estatísticas devem ser obtidas do microserviço de lendings
@RequiredArgsConstructor
class GenreRepoCustomImpl implements GenreRepoCustom {

    private final EntityManager entityManager;

    // Métodos comentados - dependem de Lending e Book
}
*/