package pt.psoft.g1.psoftg1.readermanagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderViewAMQP;
import pt.psoft.g1.psoftg1.readermanagement.model.Reader;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReaderServiceImpl implements ReaderService {
    private final ReaderRepository readerRepository;

    @Override
    public void create(ReaderViewAMQP readerViewAMQP) {
        Reader reader = new Reader(readerViewAMQP.getReaderNumber(), readerViewAMQP.getName());
        readerRepository.save(reader);
    }

    @Override
    public void update(ReaderViewAMQP readerViewAMQP) {
        Reader reader = readerRepository.findByReaderNumber(readerViewAMQP.getReaderNumber())
                .orElseThrow(() -> new RuntimeException("Reader not found"));

        reader.setName(readerViewAMQP.getName());
        readerRepository.save(reader);
    }

    @Override
    public Optional<Reader> findByReaderNumber(String readerNumber) {
        return readerRepository.findByReaderNumber(readerNumber);
    }

    @Override
    public Optional<Reader> findById(Long userId) {
        return readerRepository.findById(userId);
    }
}