package pt.psoft.g1.psoftg1.usermanagement.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import pt.psoft.g1.psoftg1.shared.model.Name;

@Entity
@Document(collection = "users")
@Table(name = "T_USER")
@EntityListeners(AuditingEntityListener.class)
@GenericGenerator(name = "id-generator", strategy = "pt.psoft.g1.psoftg1.configuration.IdGenerationConfig")
public class User implements UserDetails {

  private static final long serialVersionUID = 1L;

  @Id
  @Getter
  @GeneratedValue(generator = "id-generator")
  @org.springframework.data.annotation.Id
  @Column(name = "USER_ID")
  private String id;

  @Version
  private Long version;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  @Getter
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  @Getter
  private LocalDateTime modifiedAt;

  @CreatedBy
  @Column(nullable = false, updatable = false)
  @Getter
  private String createdBy;

  @LastModifiedBy
  @Column(nullable = false)
  private String modifiedBy;

  @Setter
  @Getter
  private boolean enabled = true;

  @Setter
  @Column(unique = true, nullable = false)
  @Email
  @Getter
  @NotNull
  @NotBlank
  private String username;

  @Column(nullable = false)
  @Getter
  @NotNull
  @NotBlank
  private String password;

  @Getter
  @Embedded
  @Field("name")
  private Name name;

  @ElementCollection
  @Transient
  @org.springframework.data.annotation.Transient
  private final Set<Role> authorities = new HashSet<>();

  protected User() {
  }

  public User(final String username, final String password) {
    this.username = username;
    setPassword(password);
  }

  public static User newUser(final String username, final String password, final String name) {
    final var u = new User(username, password);
    u.setName(name);
    return u;
  }

  public static User newUser(final String username, final String password, final String name, final String role) {
    final var u = new User(username, password);
    u.setName(name);
    u.addAuthority(new Role(role));
    return u;
  }

  public void setPassword(final String password) {
    Password passwordCheck = new Password(password);
    final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    this.password = passwordEncoder.encode(password);
  }

  public void addAuthority(final Role r) {
    authorities.add(r);
  }

  @Override
  @Transient
  @org.springframework.data.annotation.Transient
  public Set<Role> getAuthorities() {
    if (authorities.isEmpty()) {
      if (this instanceof Reader) {
        authorities.add(new Role(Role.READER));
      } else if (this instanceof Librarian) {
        authorities.add(new Role(Role.LIBRARIAN));
      }
    }
    return authorities;
  }

  @Override
  public boolean isAccountNonExpired() {
    return isEnabled();
  }

  @Override
  public boolean isAccountNonLocked() {
    return isEnabled();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return isEnabled();
  }

  public void setName(String name) {
    this.name = new Name(name);
  }
}
