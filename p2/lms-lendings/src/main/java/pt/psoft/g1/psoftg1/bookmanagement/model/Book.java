package pt.psoft.g1.psoftg1.bookmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "BOOK")
@Getter
@Setter
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @NotNull
    @Column(unique = true) 
    private String isbn;

    @NotNull
    private String title;

    private String genre;

    public Book(String isbn, String title, String genre) {
        this.isbn = isbn;
        this.title = title;
        this.genre = genre;
    }

    protected Book() {}
}