package pt.psoft.g1.psoftg1.usermanagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "READER_USER")
@Getter
@Setter
@NoArgsConstructor
public class Reader extends User {

  // Factory method para criar readers
  public static Reader newReader(String username, String password, String name) {
    Reader reader = new Reader();
    reader.setUsername(username);
    reader.setPassword(password);
    reader.setName(name);
    reader.addAuthority(new Role(Role.READER));
    return reader;
  }
}
