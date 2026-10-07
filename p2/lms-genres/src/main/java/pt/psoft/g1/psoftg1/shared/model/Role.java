package pt.psoft.g1.psoftg1.shared.model;

import java.io.Serial;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Value;

@Value
public class Role implements GrantedAuthority {

  @Serial
  private static final long serialVersionUID = 1L;

  public static final String ADMIN = "ADMIN";
  public static final String LIBRARIAN = "LIBRARIAN";
  public static final String READER = "READER";

  String authority;

  @JsonCreator
  public Role(String authority) {
    this.authority = authority;
  }

  @Override
  @JsonValue
  public String getAuthority() {
    return authority;
  }
}
