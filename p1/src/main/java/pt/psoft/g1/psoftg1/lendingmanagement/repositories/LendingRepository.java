package pt.psoft.g1.psoftg1.lendingmanagement.repositories;

import java.util.List;
import java.util.Optional;

import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;

public interface LendingRepository extends LendingRepoCustom {
  Optional<Lending> findByLendingNumber(String lendingNumber);

  List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn);

  int getCountFromCurrentYear();

  List<Lending> listOutstandingByReaderNumber(String readerNumber);

  Double getAverageDuration();

  Double getAvgLendingDurationByIsbn(String isbn);

  List<Lending> findAll();

  Lending save(Lending lending);

  void delete(Lending lending);

}
