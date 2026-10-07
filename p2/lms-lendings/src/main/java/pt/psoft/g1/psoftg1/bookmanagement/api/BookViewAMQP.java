package pt.psoft.g1.psoftg1.bookmanagement.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "A Book from AMQP communication")
public class BookViewAMQP {
    @NotNull
    private Long id; 

    @NotNull
    private String isbn;

    @NotNull
    private String title;

    @NotNull
    private Long version;

    private String genre;

    private String description;

    private List<Long> authorIds;

    private Map<String, Object> _links = new HashMap<>();
}