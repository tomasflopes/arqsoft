package pt.psoft.g1.psoftg1.bookmanagement.services;

import java.util.List;

import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQP;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;

public interface BookService {
  Book create(BookViewAMQP bookViewAMQP); // AMQP request

  List<Book> findByGenre(String genre);

  Book findByIsbn(String isbn);

  Book update(BookViewAMQP bookViewAMQP);

  void deleteByIsbn(String isbn);
}
