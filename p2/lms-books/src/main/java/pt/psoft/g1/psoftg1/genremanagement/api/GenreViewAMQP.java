package pt.psoft.g1.psoftg1.genremanagement.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenreViewAMQP {
    private String genre;
    private String oldGenre; // Para eventos de UPDATE
    private Long version;
}
