package pt.psoft.g1.psoftg1.genremanagement.api;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Schema(description = "A Genre for AMQP communication")
@NoArgsConstructor
public class GenreViewAMQP {
  @NotNull
  private String genre;

  private String oldGenre; // Para eventos de UPDATE

  @NotNull
  private Long version;

  @Setter
  @Getter
  private Map<String, Object> _links = new HashMap<>();

  public GenreViewAMQP(String genre) {
    this.genre = genre;
  }
}
