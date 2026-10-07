package pt.psoft.g1.psoftg1.bookmanagement.services;

import jakarta.annotation.Nullable;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookRequest {
    private String isbn;
    private String description;
    private String title;

    @Nullable
    private String photoURI;

    @Nullable
    private MultipartFile photo;

    private Genre genreObj;
    private String genre;
    private List<Long> authors;
    private List<Author> authorObjList;

    public UpdateBookRequest(String isbn, String title, String genre, @NonNull List<Long> authors, String description) {
        this.isbn = isbn;
        this.genre = genre;
        this.title = title;
        this.description = description;
        this.authors = authors;
    }
}
