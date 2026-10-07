package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.FineRepository;

import java.util.Optional;

@Repository("springDataFineMongoRepository")
@Profile("mongo")
public interface SpringDataFineMongoRepository
        extends FineRepository, MongoRepository<Fine, String> {

    @Override
    @Query("{ 'lending.lendingNumber.lendingNumber': ?0 }")
    Optional<Fine> findByLendingNumber(String lendingNumber);
}
