package pt.psoft.g1.psoftg1.genremanagement.services;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.api.GenreViewAMQP;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.publishers.GenreEventsPublisher;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {
  private final GenreRepository genreRepository;
  private final GenreEventsPublisher genreEventsPublisher;
  
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<Genre> findByString(String name) {
    return genreRepository.findByString(name);
  }

  @Override
  public Iterable<Genre> findAll() {
    return StreamSupport
        .stream(genreRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }

  @Override
  public List<GenreBookCountDTO> findTopGenreByBooks() {
    Pageable pageableRules = PageRequest.of(0, 5);
    return this.genreRepository.findTop5GenreByBookCount(pageableRules).getContent();
  }

  public Optional<Genre> findByStringDirect(String name) {
    Optional<Genre> genre = genreRepository.findByString(name);
    return genre;
  }

  public Iterable<Genre> findAllDirect() {
    return StreamSupport
        .stream(genreRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public Genre update(String currentName, String newName) {
    System.out.println(">>> UPDATE: Buscando genre: " + currentName);
    final Genre genre = genreRepository.findByString(currentName)
        .orElseThrow(() -> new NotFoundException("Genre not found: " + currentName));

    System.out.println(">>> UPDATE: Genre encontrado: " + genre.getGenre());

    // Verificar se já existe um genre com o novo nome (exceto o atual)
    Optional<Genre> existing = genreRepository.findByString(newName);
    if (existing.isPresent() && !existing.get().getGenre().equals(currentName)) {
      throw new IllegalArgumentException("Genre with name '" + newName + "' already exists");
    }

    System.out.println(">>> UPDATE: Atualizando de '" + currentName + "' para '" + newName + "'");
    
    // Atualizar
    genre.updateGenre(newName);
    final Genre saved = genreRepository.save(genre);
    
    System.out.println(">>> UPDATE: Após save, valor: " + saved.getGenre());
    entityManager.flush(); // Força o flush para a BD
    System.out.println(">>> UPDATE: Flush executado!");
    
    // Publicar UPDATE com nome antigo E novo (para sincronizar outros serviços)
    genreEventsPublisher.sendGenreUpdated(saved, currentName, 1L);
    
    return saved;
  }

  @Override
  public Genre save(Genre genre) {
    final boolean isNew = !genreRepository.findByString(genre.getGenre()).isPresent();
    final Genre savedGenre = this.genreRepository.save(genre);

    // Publicar evento para RabbitMQ
    if (isNew) {
      genreEventsPublisher.sendGenreCreated(savedGenre);
    }

    return savedGenre;
  }

  @Override
  public Genre save(GenreViewAMQP genreViewAMQP) {
    final String genre = genreViewAMQP.getGenre();

    return save(new Genre(genre));
  }

  @Override
  @Transactional
  public void deleteByName(String genreName) {
    final Genre genre = genreRepository.findByString(genreName)
        .orElseThrow(() -> new NotFoundException("Genre not found: " + genreName));

    genreRepository.delete(genre);
    genreEventsPublisher.sendGenreDeleted(genre, 1L);
  }

  @Override
  public List<GenreLendingsPerMonthDTO> getLendingsPerMonthLastYearByGenre() {
    return genreRepository.getLendingsPerMonthLastYearByGenre();
  }

  @Override
  public List<GenreLendingsDTO> getAverageLendings(GetAverageLendingsQuery query, Page page) {
    if (page == null)
      page = new Page(1, 10);

    final var month = LocalDate.of(query.getYear(), query.getMonth(), 1);
    return genreRepository.getAverageLendingsInMonth(month, page);
  }

  @Override
  public List<GenreLendingsPerMonthDTO> getLendingsAverageDurationPerMonth(String start, String end) {
    LocalDate startDate;
    LocalDate endDate;

    try {
      startDate = LocalDate.parse(start);
      endDate = LocalDate.parse(end);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Expected format is YYYY-MM-DD");
    }

    if (startDate.isAfter(endDate))
      throw new IllegalArgumentException("Start date cannot be after end date");

    final var list = genreRepository.getLendingsAverageDurationPerMonth(startDate, endDate);

    if (list.isEmpty())
      throw new NotFoundException("No objects match the provided criteria");

    return list;
  }
}
