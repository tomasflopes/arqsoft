package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.FineRepository;

@Repository
@Profile("sql | test")
public interface SpringDataFineRepository extends FineRepository, CrudRepository<Fine, Long> {

  @Override
  @Query("SELECT f " +
      "FROM Fine f " +
      "JOIN Lending l ON f.lending.pk = l.pk " +
      "WHERE l.lendingNumber.lendingNumber = :lendingNumber")
  Optional<Fine> findByLendingNumber(String lendingNumber);

}
