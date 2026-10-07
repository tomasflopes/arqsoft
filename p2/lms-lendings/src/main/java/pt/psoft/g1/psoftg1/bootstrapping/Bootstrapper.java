package pt.psoft.g1.psoftg1.bootstrapping;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;
import pt.psoft.g1.psoftg1.shared.services.ForbiddenNameService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import pt.psoft.g1.psoftg1.readermanagement.model.Reader;

@Component
@RequiredArgsConstructor
@Profile("bootstrap")
@PropertySource({"classpath:config/library.properties"})
@Order(1)
public class Bootstrapper implements CommandLineRunner {
    @Value("${lendingDurationInDays}")
    private int lendingDurationInDays;
    @Value("${fineValuePerDayInCents}")
    private int fineValuePerDayInCents;

    private final BookRepository bookRepository;
    private final LendingRepository lendingRepository;
    private final ReaderRepository readerRepository;


    @Override
    @Transactional
    public void run(final String... args) {
        createBooks();
        createReaders();
        createLendings();
    }

    private void createReaders() {
        if (readerRepository.findByReaderNumber("2025/1").isEmpty()) {
            readerRepository.save(new Reader("2025/1", "Manuel Sarapinto das Coives"));
        }
        if (readerRepository.findByReaderNumber("2025/2").isEmpty()) {
            readerRepository.save(new Reader("2025/2", "João Ratao"));
        }
        if (readerRepository.findByReaderNumber("2025/3").isEmpty()) {
            readerRepository.save(new Reader("2025/3", "Pedro Das Cenas"));
        }
        if (readerRepository.findByReaderNumber("2025/4").isEmpty()) {
            readerRepository.save(new Reader("2025/4", "Catarina Martins"));
        }
        if (readerRepository.findByReaderNumber("2025/5").isEmpty()) {
            readerRepository.save(new Reader("2025/5", "Marcelo Rebelo de Sousa"));
        }
        if (readerRepository.findByReaderNumber("2025/6").isEmpty()) {
            readerRepository.save(new Reader("2025/6", "Luís Montenegro"));
        }
        if (readerRepository.findByReaderNumber("2025/7").isEmpty()) {
            readerRepository.save(new Reader("2025/7", "António Costa"));
        }
        if (readerRepository.findByReaderNumber("2025/8").isEmpty()) {
            readerRepository.save(new Reader("2025/8", "André Ventura"));
        }
    }
    

    private void createBooks() {
        if (bookRepository.findByIsbn("9789720706386").isEmpty()) {
            bookRepository.save(new Book("9789720706386", "O País das Pessoas de Pernas Para o Ar", "Infantil"));
        }
        if (bookRepository.findByIsbn("9789723716160").isEmpty()) {
            bookRepository.save(new Book("9789723716160", "Como se Desenha Uma Casa", "Infantil"));
        }
        if (bookRepository.findByIsbn("9789895612864").isEmpty()) {
            bookRepository.save(new Book("9789895612864", "C e Algoritmos", "Informação"));
        }
        if (bookRepository.findByIsbn("9782722203402").isEmpty()) {
            bookRepository.save(new Book("9782722203402", "Introdução ao Desenvolvimento Moderno para a Web", "Informação"));
        }
        if (bookRepository.findByIsbn("9789722328296").isEmpty()) {
            bookRepository.save(new Book("9789722328296", "O Principezinho", "Infantil"));
        }
        if (bookRepository.findByIsbn("9789895702756").isEmpty()) {
            bookRepository.save(new Book("9789895702756", "A Criada Está a Ver", "Thriller"));
        }
        if (bookRepository.findByIsbn("9789897776090").isEmpty()) {
            bookRepository.save(new Book("9789897776090", "O Hobbit", "Fantasia"));
        }
        if (bookRepository.findByIsbn("9789896379636").isEmpty()) {
            bookRepository.save(new Book("9789896379636", "Histórias de Vigaristas e Canalhas", "Fantasia"));
        }
        if (bookRepository.findByIsbn("9789896378905").isEmpty()) {
            bookRepository.save(new Book("9789896378905", "Histórias de Aventureiros e Patifes", "Fantasia"));
        }
        if (bookRepository.findByIsbn("9789896375225").isEmpty()) {
            bookRepository.save(new Book("9789896375225", "Windhaven", "Fantasia"));
        }
    }

    private void createLendings() {
        int i;
        int seq = 0;
    
        List<Book> books = bookRepository.findAll();
        List<Reader> readers = (List<Reader>) readerRepository.findAll();
    
        if (books.size() < 10 || readers.size() < 6) {
            throw new IllegalStateException("Not enough books or readers to create lendings. Found " + books.size() + " books and " + readers.size() + " readers.");
        }
    
        LocalDate startDate;
        LocalDate returnedDate;
        Lending lending;

        //Lendings 1 through 3 (late, returned)
        for(i = 0; i < 3; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()){
                startDate = LocalDate.of(2024, 1,31-i);
                returnedDate = LocalDate.of(2024,2,15+i);
                lending = Lending.newBootstrappingLending(books.get(i), readers.get(i*2), 2024, seq, startDate, returnedDate, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }

        //Lendings 4 through 6 (overdue, not returned)
        for(i = 0; i < 3; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()){
                startDate = LocalDate.of(2024, 3,25+i);
                lending = Lending.newBootstrappingLending(books.get(1+i), readers.get(1+i*2), 2024, seq, startDate, null, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }
        //Lendings 7 through 9 (late, overdue, not returned)
        for(i = 0; i < 3; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()){
                startDate = LocalDate.of(2024, 4,(1+2*i));
                lending = Lending.newBootstrappingLending(books.get(3/(i+1)), readers.get(i*2), 2024, seq, startDate, null, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }

        //Lendings 10 through 12 (returned)
        for(i = 0; i < 3; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()){
                startDate = LocalDate.of(2024, 5,(i+1));
                returnedDate = LocalDate.of(2024,5,(i+2));
                lending = Lending.newBootstrappingLending(books.get(3-i), readers.get(1+i*2), 2024, seq, startDate, returnedDate, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }

        //Lendings 13 through 18 (returned)
        for(i = 0; i < 6; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()){
                startDate = LocalDate.of(2024, 5,(i+2));
                returnedDate = LocalDate.of(2024,5,(i+2*2));
                lending = Lending.newBootstrappingLending(books.get(i), readers.get(i), 2024, seq, startDate, returnedDate, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }

        //Lendings 19 through 23 (returned)
        for(i = 0; i < 6; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()){
                startDate = LocalDate.of(2024, 5,(i+8));
                returnedDate = LocalDate.of(2024,5,(2*i+8));
                lending = Lending.newBootstrappingLending(books.get(i), readers.get(1+i%4), 2024, seq, startDate, returnedDate, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }

        //Lendings 24 through 29 (returned)
        for(i = 0; i < 6; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()){
                startDate = LocalDate.of(2024, 5,(i+18));
                returnedDate = LocalDate.of(2024,5,(2*i+18));
                lending = Lending.newBootstrappingLending(books.get(i), readers.get(i%2+2), 2024, seq, startDate, returnedDate, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }

        //Lendings 30 through 35 (not returned, not overdue)
        for(i = 0; i < 6; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()){
                startDate = LocalDate.of(2024, 6,(i/3+1));
                lending = Lending.newBootstrappingLending(books.get(i), readers.get(i%2+3), 2024, seq, startDate, null, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }

        //Lendings 36 through 45 (not returned, not overdue)
        for(i = 0; i < 10; i++){
            ++seq;
            if(lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()){
                startDate = LocalDate.of(2024, 6,(2+i/4));
                lending = Lending.newBootstrappingLending(books.get(i), readers.get(4-i%4), 2024, seq, startDate, null, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }
    }

}

