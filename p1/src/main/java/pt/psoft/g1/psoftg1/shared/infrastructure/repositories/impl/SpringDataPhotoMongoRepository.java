package pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;

@Repository
@Profile("mongo")
public interface SpringDataPhotoMongoRepository 
    extends PhotoRepository, MongoRepository<Photo, Long> {
    
    @Override
    void deleteByPhotoFile(String photoFile);
}