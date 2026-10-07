package pt.psoft.g1.psoftg1.genremanagement.services;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.shared.repositories.CacheRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

@Service
public class GenreServiceImpl implements GenreService {
  private final GenreRepository genreRepository;
  private final CacheRepository cacheRepository;

  public GenreServiceImpl(
      GenreRepository genreRepository,
      @Autowired(required = false) @Qualifier("SpringDataGenreRedisRepository") CacheRepository cacheRepository) {
    this.genreRepository = genreRepository;
    this.cacheRepository = cacheRepository;
  }

  @Override
  public Optional<Genre> findByString(String name) {
    String cacheKey = "genre:" + name;
    Genre cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<Genre>() {
      });
      if (cached != null)
        return Optional.of(cached);
    }
    Optional<Genre> genre = genreRepository.findByString(name);
    if (cacheRepository != null && genre.isPresent()) {
      cacheRepository.save(cacheKey, genre.get());
    }
    return genre;
  }

  @Override
  public Iterable<Genre> findAll() {
    String cacheKey = "genres:all";
    List<Genre> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<List<Genre>>() {
      });
      if (cached != null)
        return cached;
    }
    List<Genre> genres = StreamSupport
        .stream(genreRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, genres);
    }
    return genres;
  }

  @Override
  public List<GenreBookCountDTO> findTopGenreByBooks() {
    String cacheKey = "genres:top:books";
    List<GenreBookCountDTO> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey,
          new com.fasterxml.jackson.core.type.TypeReference<List<GenreBookCountDTO>>() {
          });
      if (cached != null)
        return cached;
    }
    Pageable pageableRules = PageRequest.of(0, 5);
    List<GenreBookCountDTO> result = this.genreRepository.findTop5GenreByBookCount(pageableRules).getContent();
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, result);
    }
    return result;
  }

  public Optional<Genre> findByStringDirect(String name) {
    String cacheKey = "genre:direct:" + name;
    Genre cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<Genre>() {
      });
      if (cached != null)
        return Optional.of(cached);
    }
    Optional<Genre> genre = genreRepository.findByString(name);
    if (cacheRepository != null && genre.isPresent()) {
      cacheRepository.save(cacheKey, genre.get());
    }
    return genre;
  }

  public Iterable<Genre> findAllDirect() {
    String cacheKey = "genres:all:direct";
    List<Genre> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<List<Genre>>() {
      });
      if (cached != null)
        return cached;
    }
    List<Genre> genres = StreamSupport
        .stream(genreRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, genres);
    }
    return genres;
  }

  public List<GenreBookCountDTO> findTopGenreByBooksDirect() {
    String cacheKey = "genres:top:books:direct";
    List<GenreBookCountDTO> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey,
          new com.fasterxml.jackson.core.type.TypeReference<List<GenreBookCountDTO>>() {
          });
      if (cached != null)
        return cached;
    }
    Pageable pageableRules = PageRequest.of(0, 5);
    List<GenreBookCountDTO> result = this.genreRepository.findTop5GenreByBookCount(pageableRules).getContent();
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, result);
    }
    return result;
  }

  @Override
  public Genre save(Genre genre) {
    return this.genreRepository.save(genre);
  }

  @Override
  public List<GenreLendingsPerMonthDTO> getLendingsPerMonthLastYearByGenre() {
    String cacheKey = "genres:lendings:lastYear";
    List<GenreLendingsPerMonthDTO> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey,
          new com.fasterxml.jackson.core.type.TypeReference<List<GenreLendingsPerMonthDTO>>() {
          });
      if (cached != null)
        return cached;
    }
    List<GenreLendingsPerMonthDTO> result = genreRepository.getLendingsPerMonthLastYearByGenre();
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, result);
    }
    return result;
  }

  @Override
  public List<GenreLendingsDTO> getAverageLendings(GetAverageLendingsQuery query, Page page) {
    if (page == null)
      page = new Page(1, 10);

    final var month = LocalDate.of(query.getYear(), query.getMonth(), 1);
    String cacheKey = "genres:averageLendings:" + month + ":" + page.getNumber() + ":" + page.getLimit();
    List<GenreLendingsDTO> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey,
          new com.fasterxml.jackson.core.type.TypeReference<List<GenreLendingsDTO>>() {
          });
      if (cached != null)
        return cached;
    }
    List<GenreLendingsDTO> result = genreRepository.getAverageLendingsInMonth(month, page);
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, result);
    }
    return result;
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

    String cacheKey = "genres:avgDuration:" + start + ":" + end;
    List<GenreLendingsPerMonthDTO> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey,
          new com.fasterxml.jackson.core.type.TypeReference<List<GenreLendingsPerMonthDTO>>() {
          });
      if (cached != null)
        return cached;
    }

    final var list = genreRepository.getLendingsAverageDurationPerMonth(startDate, endDate);

    if (list.isEmpty())
      throw new NotFoundException("No objects match the provided criteria");

    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, list);
    }
    return list;
  }
}
