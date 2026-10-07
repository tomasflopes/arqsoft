package pt.psoft.g1.psoftg1.lendingmanagement.repositories;

import java.util.List;
import java.util.Optional;

import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;

public interface LendingRepository {
  Optional<Lending> findByLendingNumber(String lendingNumber);

  Optional<Lending> findByBook(Book book);

  List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn);

  int getCountFromCurrentYear();

  Double getAverageDuration();

  Double getAvgLendingDurationByIsbn(String isbn);

  Lending save(Lending lending);

  void delete(Lending lending);
}
