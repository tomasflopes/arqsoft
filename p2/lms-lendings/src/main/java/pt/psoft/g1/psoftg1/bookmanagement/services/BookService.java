package pt.psoft.g1.psoftg1.bookmanagement.services;

import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQP;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.model.Isbn;

import java.util.Optional;

public interface BookService {
    void create(BookViewAMQP bookViewAMQP);
    void update(BookViewAMQP bookViewAMQP);
    Optional<Book> findByIsbn(String isbn);
    Optional<Book> findById(Long id);
}