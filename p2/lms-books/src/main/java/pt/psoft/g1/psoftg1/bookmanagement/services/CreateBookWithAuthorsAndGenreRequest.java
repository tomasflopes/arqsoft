package pt.psoft.g1.psoftg1.bookmanagement.services;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating a Book with Authors and Genre in a single request
 * Allows a librarian to create a Book, Authors, and Genre atomically
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A DTO for creating a Book with Authors and Genre together")
public class CreateBookWithAuthorsAndGenreRequest {
    
    @NotBlank
    @Schema(description = "ISBN of the book", example = "978-3-16-148410-0")
    private String isbn;
    
    @NotBlank
    @Schema(description = "Title of the book", example = "The Lord of the Rings")
    private String title;
    
    @Schema(description = "Description of the book")
    private String description;
    
    @NotBlank
    @Schema(description = "Genre name (will be created if doesn't exist)", example = "Fantasy")
    private String genre;
    
    @NotNull
    @Schema(description = "List of authors to create and associate")
    private List<AuthorInfo> authors;
    
    /**
     * Simple author info for creation
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Author information for creation")
    public static class AuthorInfo {
        
        @NotBlank
        @Schema(description = "Author name", example = "J.R.R. Tolkien")
        private String name;
        
        @Schema(description = "Author biography")
        private String bio;
    }
}
