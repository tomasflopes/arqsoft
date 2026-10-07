package pt.psoft.g1.psoftg1.readermanagement.repositories;

import org.springframework.data.repository.CrudRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.Reader;

import java.util.Optional;

public interface ReaderRepository extends CrudRepository<Reader, Long> {
    Optional<Reader> findByReaderNumber(String readerNumber);
}