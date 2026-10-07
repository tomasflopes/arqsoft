package pt.psoft.g1.psoftg1.shared.model;

import java.nio.file.Path;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@GenericGenerator(name = "id-generator", strategy = "pt.psoft.g1.psoftg1.configuration.IdGenerationConfig")
public class Photo {
  @Id
  @GeneratedValue(generator = "id-generator")
  private String pk;

  @NotNull
  @Setter
  @Getter
  private String photoFile;

  protected Photo() {
  }

  public Photo(Path photoPath) {
    setPhotoFile(photoPath.toString());
  }

  @JsonValue
  public String toJson() {
    return photoFile;
  }

  @JsonCreator
  public static Photo fromJson(String photoFile) {
    Photo photo = new Photo();
    photo.setPhotoFile(photoFile);
    return photo;
  }
}
