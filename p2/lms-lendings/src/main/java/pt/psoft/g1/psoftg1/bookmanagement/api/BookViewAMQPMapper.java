package pt.psoft.g1.psoftg1.bookmanagement.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookViewAMQPMapper {

    @Mapping(target = "id", source = "pk")
    @Mapping(target = "isbn", source = "isbn")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "genre", source = "genre")
    BookViewAMQP toBookViewAMQP(Book book);

    List<BookViewAMQP> toBookViewAMQPList(List<Book> books);
}