package pt.psoft.g1.psoftg1.genremanagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.genremanagement.api.GenreViewAMQP;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    @Override
    public List<Genre> findAll() {
        return genreRepository.findAll();
    }

    @Override
    public Optional<Genre> findByGenre(String genre) {
        return genreRepository.findByGenre(genre);
    }

    @Override
    public Genre createFromAMQP(GenreViewAMQP genreViewAMQP) {
        // Verifica se já existe
        Optional<Genre> existing = genreRepository.findByGenre(genreViewAMQP.getGenre());
        if (existing.isPresent()) {
            return existing.get();
        }

        // Cria novo genre
        Genre genre = new Genre(genreViewAMQP.getGenre());
        return genreRepository.save(genre);
    }

    @Override
    public Genre updateFromAMQP(GenreViewAMQP genreViewAMQP) {
        Optional<Genre> existing = genreRepository.findByGenre(genreViewAMQP.getGenre());
        if (existing.isEmpty()) {
            // Se não existe, cria
            return createFromAMQP(genreViewAMQP);
        }

        // Genre é imutável na prática (apenas o nome), então retorna o existente
        return existing.get();
    }

    @Override
    public void deleteFromAMQP(GenreViewAMQP genreViewAMQP) {
        Optional<Genre> existing = genreRepository.findByGenre(genreViewAMQP.getGenre());
        existing.ifPresent(genreRepository::delete);
    }
}