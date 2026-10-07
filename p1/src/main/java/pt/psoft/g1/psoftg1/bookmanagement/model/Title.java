package pt.psoft.g1.psoftg1.bookmanagement.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Embeddable
public class Title {
  @Transient
  @org.springframework.data.annotation.Transient
  private final int TITLE_MAX_LENGTH = 128;
  @NotBlank(message = "Title cannot be blank")
  @Size(min = 1, max = TITLE_MAX_LENGTH)
  @Column(name = "TITLE", length = TITLE_MAX_LENGTH)
  @Getter
  private String title;

  protected Title() {
  }

  public Title(String title) {
    setTitle(title);
  }

  public void setTitle(String title) {

    /*
     * if (!StringUtilsCustom.startsOrEndsInWhiteSpace(title)) {
     * throw new IllegalArgumentException("Invalid title: " + title);
     * }
     */
    if (title == null)
      throw new IllegalArgumentException("Title cannot be null");
    if (title.isBlank())
      throw new IllegalArgumentException("Title cannot be blank");
    if (title.length() > TITLE_MAX_LENGTH)
      throw new IllegalArgumentException("Title has a maximum of " + TITLE_MAX_LENGTH + " characters");
    this.title = title.strip();
  }

  @JsonValue
  public String toJson() {
    return title;
  }

  @JsonCreator
  public static Title fromJson(String title) {
    return new Title(title);
  }

  @Override
  public String toString() {
    return title;
  }
}
