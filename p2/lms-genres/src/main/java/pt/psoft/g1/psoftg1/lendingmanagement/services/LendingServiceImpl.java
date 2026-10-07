package pt.psoft.g1.psoftg1.lendingmanagement.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingViewAMQP;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;

@Service
@RequiredArgsConstructor
@PropertySource({ "classpath:config/library.properties" })
public class LendingServiceImpl implements LendingService {
  private final LendingRepository lendingRepository;
  private final BookRepository bookRepository;

  @Value("${lendingDurationInDays}")
  private int lendingDurationInDays;
  @Value("${fineValuePerDayInCents}")
  private int fineValuePerDayInCents;

  @Override
  public Optional<Lending> findByLendingNumber(String lendingNumber) {
    return lendingRepository.findByLendingNumber(lendingNumber);
  }

  @Override
  public Lending create(final CreateLendingRequest resource) {
    Book book = bookRepository.findByIsbn(resource.getIsbn())
        .orElseThrow(() -> new NotFoundException("Book not found"));

    String reader = resource.getReaderNumber();
    String lendingNumber = String.valueOf(lendingRepository.getCountFromCurrentYear() + 1);

    return new Lending(book, reader, lendingNumber, lendingDurationInDays, fineValuePerDayInCents);
  }

  @Override
  public List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn, Optional<Boolean> returned) {
    List<Lending> lendings = lendingRepository.listByReaderNumberAndIsbn(readerNumber, isbn);
    if (returned.isEmpty()) {
      return lendings;
    } else {
      for (int i = 0; i < lendings.size(); i++) {
        if ((lendings.get(i).getReturnedDate() == null) == returned.get()) {
          lendings.remove(i);
          i--;
        }
      }
    }
    return lendings;
  }

  @Override
  public Optional<Lending> findLendingByBookIsbn(String isbn) {
    Optional<Book> book = bookRepository.findByIsbn(isbn);
    if (book.isEmpty()) {
      return Optional.empty();
    }
    return lendingRepository.findByBook(book.get());
  }

  @Override
  public Double getAverageDuration() {
    Double avg = lendingRepository.getAverageDuration();
    return Double.valueOf(String.format(Locale.US, "%.1f", avg));
  }

  @Override
  public void update(LendingViewAMQP lendingViewAMQP) {
    Lending lending = lendingRepository.findByLendingNumber(lendingViewAMQP.getLendingNumber())
        .orElseThrow(
            () -> new NotFoundException("Lending not found with number: " + lendingViewAMQP.getLendingNumber()));

    if (lendingViewAMQP.getReturnedDate() != null) {
      lending.setReturned(LocalDate.parse(lendingViewAMQP.getReturnedDate()));
    }

    if (lendingViewAMQP.getCommentary() != null) {
      lending.setCommentary(lendingViewAMQP.getCommentary());
    }

    lendingRepository.save(lending);
  }

  @Override
  public Double getAvgLendingDurationByIsbn(String isbn) {
    Double avg = lendingRepository.getAvgLendingDurationByIsbn(isbn);
    return Double.valueOf(String.format(Locale.US, "%.1f", avg));
  }
}
