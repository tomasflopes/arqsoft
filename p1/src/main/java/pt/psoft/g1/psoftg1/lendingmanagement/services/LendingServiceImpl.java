package pt.psoft.g1.psoftg1.lendingmanagement.services;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.LendingForbiddenException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.FineRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.CacheRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

@Service
@PropertySource({ "classpath:config/library.properties" })
public class LendingServiceImpl implements LendingService {
  private final LendingRepository lendingRepository;
  private final FineRepository fineRepository;
  private final BookRepository bookRepository;
  private final ReaderRepository readerRepository;
  private final CacheRepository cacheRepository;

  public LendingServiceImpl(
      LendingRepository lendingRepository,
      FineRepository fineRepository,
      BookRepository bookRepository,
      ReaderRepository readerRepository,
      @Autowired(required = false) @Qualifier("SpringDataLendingRedisRepository") CacheRepository cacheRepository) {
    this.lendingRepository = lendingRepository;
    this.fineRepository = fineRepository;
    this.bookRepository = bookRepository;
    this.readerRepository = readerRepository;
    this.cacheRepository = cacheRepository;
  }

  @Value("${lendingDurationInDays}")
  private int lendingDurationInDays;
  @Value("${fineValuePerDayInCents}")
  private int fineValuePerDayInCents;

  @Override
  public Optional<Lending> findByLendingNumber(String lendingNumber) {
    String cacheKey = "lending:" + lendingNumber;
    Lending cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<Lending>() {
      });
      if (cached != null)
        return Optional.of(cached);
    }
    Optional<Lending> lending = lendingRepository.findByLendingNumber(lendingNumber);
    if (cacheRepository != null && lending.isPresent()) {
      cacheRepository.save(cacheKey, lending.get());
    }
    return lending;
  }

  @Override
  public List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn, Optional<Boolean> returned) {
    String cacheKey = "lendings:reader:" + readerNumber + ":isbn:" + isbn + ":returned:" + returned.orElse(null);
    List<Lending> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<List<Lending>>() {
      });
      if (cached != null)
        return cached;
    }
    List<Lending> lendings = lendingRepository.listByReaderNumberAndIsbn(readerNumber, isbn);
    if (returned.isPresent()) {
      lendings.removeIf(l -> (l.getReturnedDate() == null) == returned.get());
    }
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, lendings);
    }
    return lendings;
  }

  @Override
  public Lending create(final CreateLendingRequest resource) {
    int count = 0;

    Iterable<Lending> lendingList = lendingRepository.listOutstandingByReaderNumber(resource.getReaderNumber());
    for (Lending lending : lendingList) {
      // Business rule: cannot create a lending if user has late outstanding books to
      // return.
      if (lending.getDaysDelayed() > 0) {
        throw new LendingForbiddenException("Reader has book(s) past their due date");
      }
      count++;
      // Business rule: cannot create a lending if user already has 3 outstanding
      // books to return.
      if (count >= 3) {
        throw new LendingForbiddenException("Reader has three books outstanding already");
      }
    }

    final var b = bookRepository.findByIsbn(resource.getIsbn())
        .orElseThrow(() -> new NotFoundException("Book not found"));
    final var r = readerRepository.findByReaderNumber(resource.getReaderNumber())
        .orElseThrow(() -> new NotFoundException("Reader not found"));
    int seq = lendingRepository.getCountFromCurrentYear() + 1;
    final Lending l = new Lending(b, r, seq, lendingDurationInDays, fineValuePerDayInCents);

    return lendingRepository.save(l);
  }

  @Override
  public Lending setReturned(final String lendingNumber, final SetLendingReturnedRequest resource,
      final long desiredVersion) {

    var lending = lendingRepository.findByLendingNumber(lendingNumber)
        .orElseThrow(() -> new NotFoundException("Cannot update lending with this lending number"));

    lending.setReturned(desiredVersion, resource.getCommentary());

    if (lending.getDaysDelayed() > 0) {
      final var fine = new Fine(lending);
      fineRepository.save(fine);
    }

    return lendingRepository.save(lending);
  }

  @Override
  public Double getAverageDuration() {
    String cacheKey = "lending:avgDuration:all";
    Double cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<Double>() {
      });
      if (cached != null)
        return cached;
    }
    Double avg = lendingRepository.getAverageDuration();
    Double formatted = Double.valueOf(String.format(Locale.US, "%.1f", avg));
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, formatted);
    }
    return formatted;
  }

  @Override
  public List<Lending> getOverdue(Page page) {
    if (page == null) {
      page = new Page(1, 10);
    }
    String cacheKey = "lendings:overdue:" + page.getNumber() + ":" + page.getLimit();
    List<Lending> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<List<Lending>>() {
      });
      if (cached != null)
        return cached;
    }
    List<Lending> lendings = lendingRepository.getOverdue(page);
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, lendings);
    }
    return lendings;
  }

  @Override
  public Double getAvgLendingDurationByIsbn(String isbn) {
    String cacheKey = "lending:avgDuration:isbn:" + isbn;
    Double cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<Double>() {
      });
      if (cached != null)
        return cached;
    }
    Double avg = lendingRepository.getAvgLendingDurationByIsbn(isbn);
    Double formatted = Double.valueOf(String.format(Locale.US, "%.1f", avg));
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, formatted);
    }
    return formatted;
  }

  @Override
  public List<Lending> searchLendings(Page page, SearchLendingQuery query) {
    LocalDate startDate = null;
    LocalDate endDate = null;

    if (page == null) {
      page = new Page(1, 10);
    }
    if (query == null)
      query = new SearchLendingQuery("",
          "",
          null,
          LocalDate.now().minusDays(10L).toString(),
          null);

    try {
      if (query.getStartDate() != null)
        startDate = LocalDate.parse(query.getStartDate());
      if (query.getEndDate() != null)
        endDate = LocalDate.parse(query.getEndDate());
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Expected format is YYYY-MM-DD");
    }

    String cacheKey = "lendings:search:" + page.getNumber() + ":" + page.getLimit() + ":" +
        query.getReaderNumber() + ":" + query.getIsbn() + ":" + query.getReturned() + ":" +
        query.getStartDate() + ":" + query.getEndDate();

    List<Lending> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<List<Lending>>() {
      });
      if (cached != null)
        return cached;
    }

    List<Lending> lendings = lendingRepository.searchLendings(page,
        query.getReaderNumber(),
        query.getIsbn(),
        query.getReturned(),
        startDate,
        endDate);

    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, lendings);
    }
    return lendings;
  }

}
