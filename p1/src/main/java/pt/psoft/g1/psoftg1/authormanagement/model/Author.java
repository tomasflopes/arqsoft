package pt.psoft.g1.psoftg1.authormanagement.model;

import org.hibernate.StaleObjectStateException;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Getter;
import pt.psoft.g1.psoftg1.authormanagement.services.UpdateAuthorRequest;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;
import pt.psoft.g1.psoftg1.shared.model.Name;

@Entity
@Document(collection = "author")
@GenericGenerator(name = "id-generator", strategy = "pt.psoft.g1.psoftg1.configuration.IdGenerationConfig")
public class Author extends EntityWithPhoto {
  @Id
  @org.springframework.data.annotation.Id
  @Getter
  @JsonAlias("id")
  @GeneratedValue(generator = "id-generator")
  private String authorNumber;

  @Version
  private long version;

  @Embedded
  private Name name;

  @Embedded
  private Bio bio;

  public void setName(String name) {
    this.name = new Name(name);
  }

  public void setBio(String bio) {
    this.bio = new Bio(bio);
  }

  public Long getVersion() {
    return version;
  }

  public String getId() {
    return authorNumber;
  }

  public Author(String number, String name, String bio, String photoURI) {
    this.authorNumber = number;
    setName(name);
    setBio(bio);
    setPhotoInternal(photoURI);
  }

  public Author(String name, String bio, String photoURI) {
    setName(name);
    setBio(bio);
    setPhotoInternal(photoURI);
  }

  protected Author() {
    // got ORM only
  }

  public void applyPatch(final long desiredVersion, final UpdateAuthorRequest request) {
    if (this.version != desiredVersion)
      throw new StaleObjectStateException("Object was already modified by another user", this.authorNumber);
    if (request.getName() != null)
      setName(request.getName());
    if (request.getBio() != null)
      setBio(request.getBio());
    if (request.getPhotoURI() != null)
      setPhotoInternal(request.getPhotoURI());
  }

  public void removePhoto(long desiredVersion) {
    if (desiredVersion != this.version) {
      throw new ConflictException("Provided version does not match latest version of this object");
    }

    setPhotoInternal(null);
  }

  public String getName() {
    return this.name.toString();
  }

  public String getBio() {
    return this.bio.toString();
  }
}
