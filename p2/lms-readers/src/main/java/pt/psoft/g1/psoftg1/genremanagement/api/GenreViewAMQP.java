package pt.psoft.g1.psoftg1.genremanagement.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplificado para comunicação AMQP de Genres
 * Contém apenas os dados essenciais necessários no microserviço de Readers
 */
@Data
@Schema(description = "A Genre for AMQP communication")
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenreViewAMQP {

    @NotNull
    private Long id;

    @NotNull
    private String genre;

    @NotNull
    private Long version;

    public GenreViewAMQP(Long id, String genre, Long version) {
        this.id = id;
        this.genre = genre;
        this.version = version;
    }
}
