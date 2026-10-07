package pt.psoft.g1.psoftg1.shared.model;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@GenericGenerator(name = "id-generator", strategy = "pt.psoft.g1.psoftg1.configuration.IdGenerationConfig")
@NoArgsConstructor
public class ForbiddenName {

  @Id
  @GeneratedValue(generator = "id-generator")
  private String pk;

  @Getter
  @Setter
  @Column(nullable = false)
  @Size(min = 1)
  private String forbiddenName;

  public ForbiddenName(String name) {
    this.forbiddenName = name;
  }
}
