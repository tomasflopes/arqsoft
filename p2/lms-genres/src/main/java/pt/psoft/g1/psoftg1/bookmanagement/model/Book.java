package pt.psoft.g1.psoftg1.bookmanagement.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.StaleObjectStateException;
import org.springframework.data.annotation.Transient;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;

@Entity
@Table(name = "Book", uniqueConstraints = {
    @UniqueConstraint(name = "uc_book_isbn", columnNames = { "ISBN" })
})
public class Book extends EntityWithPhoto {
  @Transient
  private final int DESC_MAX_LENGTH = 4096;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long pk;

  @Version
  @Getter
  private Long version;

  @Getter
  @Setter
  private String isbn;

  @Getter
  @Setter
  @NotNull
  private String title;

  @Getter
  @ManyToOne
  @NotNull
  Genre genre;

  @Getter
  @Setter
  private List<String> authors = new ArrayList<>();

  @Getter
  @Setter
  @Size(max = DESC_MAX_LENGTH)
  @Column(length = DESC_MAX_LENGTH)
  private String description;

  private void setGenre(Genre genre) {
    this.genre = genre;
  }

  public Book(String isbn, String title, String description, Genre genre, List<String> authors, String photoURI) {
    if (isbn == null)
      throw new IllegalArgumentException("ISBN cannot be null");
    if (title == null)
      throw new IllegalArgumentException("Title cannot be null");
    setTitle(title);
    setIsbn(isbn);
    if (description != null)
      setDescription(description);
    if (genre == null)
      throw new IllegalArgumentException("Genre cannot be null");
    setGenre(genre);
    // Authors are optional in Genres microservice (only used in Books)
    if (authors != null && !authors.isEmpty()) {
      setAuthors(authors);
    }
    setPhotoInternal(photoURI);
  }

  protected Book() {
    // got ORM only
  }

  public void applyPatch(final Long desiredVersion,
      final String title,
      final String description,
      final String photoURI,
      final Genre genre,
      final List<String> authors) {

    if (!Objects.equals(this.version, desiredVersion))
      throw new StaleObjectStateException("Object was already modified by another user", this.pk);

    if (title != null) {
      setTitle(title);
    }

    if (description != null) {
      setDescription(description);
    }

    if (genre != null) {
      setGenre(genre);
    }

    if (authors != null) {
      setAuthors(authors);
    }

    if (photoURI != null) {
      setPhotoInternal(photoURI);
    }
  }

  public void removePhoto(long desiredVersion) {
    if (desiredVersion != this.version) {
      throw new ConflictException("Provided version does not match latest version of this object");
    }

    setPhotoInternal(null);
  }
}
