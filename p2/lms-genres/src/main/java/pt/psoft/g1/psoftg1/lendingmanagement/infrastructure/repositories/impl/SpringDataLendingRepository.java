package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;

public interface SpringDataLendingRepository
    extends LendingRepository, CrudRepository<Lending, Long> {
  @Override
  @Query("SELECT l FROM Lending l WHERE l.lendingNumber = :lendingNumber")
  Optional<Lending> findByLendingNumber(String lendingNumber);

  @Override
  @Query("SELECT l FROM Lending l JOIN l.book b WHERE b.isbn = :isbn AND l.readerNumber = :readerNumber")
  List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn);

  @Override
  @Query("SELECT COUNT(l) FROM Lending l WHERE YEAR(l.startDate) = YEAR(CURRENT_DATE)")
  int getCountFromCurrentYear();

  @Override
  @Query(value = "SELECT AVG(DATEDIFF(day, l.start_date, l.returned_date)) FROM Lending l", nativeQuery = true)
  Double getAverageDuration();

  @Override
  @Query(value = "SELECT AVG(DATEDIFF(day, l.start_date, l.returned_date)) FROM Lending l JOIN BOOK b ON l.BOOK_PK = b.PK WHERE b.ISBN = :isbn", nativeQuery = true)
  Double getAvgLendingDurationByIsbn(@Param("isbn") String isbn);
}
