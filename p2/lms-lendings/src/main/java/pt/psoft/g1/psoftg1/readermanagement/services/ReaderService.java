package pt.psoft.g1.psoftg1.readermanagement.services;

import pt.psoft.g1.psoftg1.readermanagement.api.ReaderViewAMQP;
import pt.psoft.g1.psoftg1.readermanagement.model.Reader;

import java.util.Optional;

public interface ReaderService {
    void create(ReaderViewAMQP readerViewAMQP);
    void update(ReaderViewAMQP readerViewAMQP);
    Optional<Reader> findByReaderNumber(String readerNumber);
    Optional<Reader> findById(Long userId);
}