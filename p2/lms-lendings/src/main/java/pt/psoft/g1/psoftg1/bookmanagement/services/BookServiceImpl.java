package pt.psoft.g1.psoftg1.bookmanagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.bookmanagement.api.BookViewAMQP;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    @Override
    public void create(BookViewAMQP bookViewAMQP) {
        Optional<Book> existing = bookRepository.findByIsbn(bookViewAMQP.getIsbn());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Book already exists");
        }

        Book book = new Book(
            bookViewAMQP.getIsbn(),
            bookViewAMQP.getTitle(),
            bookViewAMQP.getGenre()
        );
        bookRepository.save(book);
    }

    @Override
    public void update(BookViewAMQP bookViewAMQP) {
        // Em Lendings a PK pode não coincidir com Books; usar ISBN como chave estável
        Book book = bookRepository.findByIsbn(bookViewAMQP.getIsbn())
            .orElseThrow(() -> new RuntimeException("Book not found"));

        book.setTitle(bookViewAMQP.getTitle());
        book.setGenre(bookViewAMQP.getGenre());
        bookRepository.save(book);
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    @Override
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }
}