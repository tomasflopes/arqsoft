package pt.psoft.g1.psoftg1.bookmanagement.services;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A DTO for creating a Book")
public class CreateBookRequest {
    private String description;

    @NotBlank
    private String title;

    @NotBlank
    private String genre;

    @Nullable
    private String photo;

    @Nullable
    private String photoURI;

    @NotNull
    private List<Long> authors;
}
