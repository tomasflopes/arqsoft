package pt.psoft.g1.psoftg1.genremanagement.services;

import pt.psoft.g1.psoftg1.genremanagement.api.GenreViewAMQP;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreService {

    List<Genre> findAll();

    Optional<Genre> findByGenre(String genre);

    /**
     * Cria/sincroniza um Genre a partir de dados AMQP
     */
    Genre createFromAMQP(GenreViewAMQP genreViewAMQP);

    /**
     * Atualiza um Genre a partir de dados AMQP
     */
    Genre updateFromAMQP(GenreViewAMQP genreViewAMQP);

    /**
     * Remove um Genre a partir de dados AMQP
     */
    void deleteFromAMQP(GenreViewAMQP genreViewAMQP);
}