package pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import pt.psoft.g1.psoftg1.shared.model.ForbiddenName;
import pt.psoft.g1.psoftg1.shared.repositories.ForbiddenNameRepository;

@Repository
@Profile("mongo")
public interface SpringDataForbiddenNameMongoRepository 
    extends ForbiddenNameRepository, MongoRepository<ForbiddenName, Long> {
    
    @Query("{ 'forbiddenName': { $regex: ?0, $options: 'i' } }")
    List<ForbiddenName> findByForbiddenNameIsContained(String pat);

    @Override
    Optional<ForbiddenName> findByForbiddenName(String forbiddenName);

    @Override
    default int deleteForbiddenName(String forbiddenName) {
        long count = deleteByForbiddenName(forbiddenName);
        return (int) count;
    }
    
    long deleteByForbiddenName(String forbiddenName);
}