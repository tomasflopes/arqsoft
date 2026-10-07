package pt.psoft.g1.psoftg1.shared.api;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.mapstruct.Named;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import pt.psoft.g1.psoftg1.bookmanagement.model.Book;

public abstract class MapperInterface {

  public <T> String map(final T value) {
    if (value == null)
      return null;
    return value.toString();
  }

  public <T extends Number> Number map(final T value) {
    if (value instanceof Double)
      return value.doubleValue();
    if (value instanceof Integer)
      return value.intValue();
    if (value instanceof Long)
      return value.longValue();
    else
      throw new NumberFormatException("Invalid number format");
  }

  public <T> T mapOpt(final Optional<T> i) {
    return i.orElse(null);
  }

  public String map(LocalDate date) {
    return date == null ? null : date.toString();
  }

  @Named(value = "bookLink")
  protected Map<String, String> mapBookLink(Book book) {
    Map<String, String> bookLink = new HashMap<>();
    String bookUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/books/").path(book.getIsbn())
        .toUriString();
    bookLink.put("href", bookUri);
    return bookLink;
  }
}
