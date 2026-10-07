package pt.psoft.g1.psoftg1.genremanagement.model;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Entity
@Table
@Document(collection = "genres")
@GenericGenerator(name = "id-generator", strategy = "pt.psoft.g1.psoftg1.configuration.IdGenerationConfig")
public class Genre {
  
  @Transient
  @org.springframework.data.annotation.Transient
  private final int GENRE_MAX_LENGTH = 100;
  
  @Id
  @GeneratedValue(generator = "id-generator")
  @org.springframework.data.annotation.Id
  private String pk;

  @Size(min = 1, max = GENRE_MAX_LENGTH, message = "Genre name must be between 1 and 100 characters")
  @Column(unique = true, nullable = false, length = GENRE_MAX_LENGTH)
  @Getter
  private String genre;

  protected Genre() {
  }

  public Genre(String genre) {
    setGenre(genre);
  }

  private void setGenre(String genre) {
    if (genre == null)
      throw new IllegalArgumentException("Genre cannot be null");
    if (genre.isBlank())
      throw new IllegalArgumentException("Genre cannot be blank");
    if (genre.length() > GENRE_MAX_LENGTH)
      throw new IllegalArgumentException("Genre has a maximum of 4096 characters");
    this.genre = genre;
  }

  public String toString() {
    return genre;
  }
}