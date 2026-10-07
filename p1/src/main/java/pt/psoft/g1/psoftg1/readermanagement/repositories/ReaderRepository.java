package pt.psoft.g1.psoftg1.readermanagement.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import jakarta.validation.constraints.NotNull;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO;

public interface ReaderRepository extends ReaderDetailsRepoCustom {
  Optional<ReaderDetails> findByReaderNumber(@Param("readerNumber") @NotNull String readerNumber);

  List<ReaderDetails> findByPhoneNumber(@Param("phoneNumber") @NotNull String phoneNumber);

  Optional<ReaderDetails> findByUsername(@Param("username") @NotNull String username);

  Optional<ReaderDetails> findByUserId(@Param("userId") @NotNull String userId);

  int getCountFromCurrentYear();

  ReaderDetails save(ReaderDetails readerDetails);

  Iterable<ReaderDetails> findAll();

  Page<ReaderDetails> findTopReaders(Pageable pageable);

  Page<ReaderBookCountDTO> findTopByGenre(Pageable pageable, String genre, LocalDate startDate, LocalDate endDate);

  void delete(ReaderDetails readerDetails);
}
