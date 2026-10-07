package pt.psoft.g1.psoftg1.bookmanagement.api;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.shared.api.MapperInterface;

@Mapper(componentModel = "spring")
public abstract class BookViewAMQPMapper extends MapperInterface {

  @Mapping(target = "isbn", source = "isbn")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "title", source = "title")
  @Mapping(target = "genre", source = "genre")
  @Mapping(target = "authorIds", ignore = true)
  @Mapping(target = "version", source = "version")

  public abstract BookViewAMQP toBookViewAMQP(Book book);

  public abstract List<BookViewAMQP> toBookViewAMQP(List<Book> bookList);

}
