package pt.psoft.g1.psoftg1.readermanagement.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.CacheRepository;
import pt.psoft.g1.psoftg1.shared.repositories.ForbiddenNameRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;

@Service
public class ReaderServiceImpl implements ReaderService {
  private final ReaderRepository readerRepo;
  private final UserRepository userRepo;
  private final ReaderMapper readerMapper;
  private final GenreRepository genreRepo;
  private final ForbiddenNameRepository forbiddenNameRepository;
  private final PhotoRepository photoRepository;

  private final CacheRepository cacheRepository;

  public ReaderServiceImpl(
      ReaderRepository readerRepository,
      UserRepository userRepo,
      ReaderMapper readerMapper,
      GenreRepository genreRepo,
      ForbiddenNameRepository forbiddenNameRepository,
      PhotoRepository photoRepository,
      @Autowired(required = false) @Qualifier("SpringDataReaderRedisRepository") CacheRepository cacheRepository) {
    this.readerRepo = readerRepository;
    this.userRepo = userRepo;
    this.readerMapper = readerMapper;
    this.genreRepo = genreRepo;
    this.forbiddenNameRepository = forbiddenNameRepository;
    this.photoRepository = photoRepository;
    this.cacheRepository = cacheRepository;
  }

  @Override
  public ReaderDetails create(CreateReaderRequest request, String photoURI) {
    if (userRepo.findByUsername(request.getUsername()).isPresent()) {
      throw new ConflictException("Username already exists!");
    }

    Iterable<String> words = List.of(request.getFullName().split("\\s+"));
    for (String word : words) {
      if (!forbiddenNameRepository.findByForbiddenNameIsContained(word).isEmpty()) {
        throw new IllegalArgumentException("Name contains a forbidden word");
      }
    }

    List<String> stringInterestList = request.getInterestList();
    List<Genre> interestList = this.getGenreListFromStringList(stringInterestList);
    /*
     * if(stringInterestList != null && !stringInterestList.isEmpty()) {
     * request.setInterestList(this.getGenreListFromStringList(stringInterestList));
     * }
     */

    /*
     * Since photos can be null (no photo uploaded) that means the URI can be null
     * as well.
     * To avoid the client sending false data, photoURI has to be set to any value /
     * null
     * according to the MultipartFile photo object
     *
     * That means:
     * - photo = null && photoURI = null -> photo is removed
     * - photo = null && photoURI = validString -> ignored
     * - photo = validFile && photoURI = null -> ignored
     * - photo = validFile && photoURI = validString -> photo is set
     */

    MultipartFile photo = request.getPhoto();
    if (photo == null && photoURI != null || photo != null && photoURI == null) {
      request.setPhoto(null);
    }

    int count = readerRepo.getCountFromCurrentYear();
    Reader reader = readerMapper.createReader(request);
    ReaderDetails rd = readerMapper.createReaderDetails(count + 1, reader, request, photoURI, interestList);

    userRepo.save(reader);
    return readerRepo.save(rd);
  }

  @Override
  public List<ReaderBookCountDTO> findTopByGenre(String genre, LocalDate startDate, LocalDate endDate) {
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date cannot be after end date");
    }
    String cacheKey = "readers:topByGenre:" + genre + ":" + startDate + ":" + endDate;
    List<ReaderBookCountDTO> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey,
          new com.fasterxml.jackson.core.type.TypeReference<List<ReaderBookCountDTO>>() {
          });
      if (cached != null)
        return cached;
    }
    Pageable pageableRules = PageRequest.of(0, 5);
    List<ReaderBookCountDTO> result = this.readerRepo.findTopByGenre(pageableRules, genre, startDate, endDate)
        .getContent();
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, result);
    }
    return result;
  }

  @Override
  public ReaderDetails update(final String id, final UpdateReaderRequest request, final long desiredVersion,
      String photoURI) {
    final ReaderDetails readerDetails = readerRepo.findByUserId(id)
        .orElseThrow(() -> new NotFoundException("Cannot find reader"));

    List<String> stringInterestList = request.getInterestList();
    List<Genre> interestList = this.getGenreListFromStringList(stringInterestList);

    /*
     * Since photos can be null (no photo uploaded) that means the URI can be null
     * as well.
     * To avoid the client sending false data, photoURI has to be set to any value /
     * null
     * according to the MultipartFile photo object
     *
     * That means:
     * - photo = null && photoURI = null -> photo is removed
     * - photo = null && photoURI = validString -> ignored
     * - photo = validFile && photoURI = null -> ignored
     * - photo = validFile && photoURI = validString -> photo is set
     */

    MultipartFile photo = request.getPhoto();
    if (photo == null && photoURI != null || photo != null && photoURI == null) {
      request.setPhoto(null);
    }

    readerDetails.applyPatch(desiredVersion, request, photoURI, interestList);

    userRepo.save(readerDetails.getReader());
    return readerRepo.save(readerDetails);
  }

  @Override
  public Optional<ReaderDetails> findByReaderNumber(String readerNumber) {
    String cacheKey = "reader:" + readerNumber;
    ReaderDetails cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<ReaderDetails>() {
      });
      if (cached != null)
        return Optional.of(cached);
    }
    Optional<ReaderDetails> reader = readerRepo.findByReaderNumber(readerNumber);
    if (cacheRepository != null && reader.isPresent()) {
      cacheRepository.save(cacheKey, reader.get());
    }
    return reader;
  }

  @Override
  public List<ReaderDetails> findByPhoneNumber(String phoneNumber) {
    String cacheKey = "readers:phone:" + phoneNumber;
    List<ReaderDetails> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<List<ReaderDetails>>() {
      });
      if (cached != null)
        return cached;
    }
    List<ReaderDetails> result = this.readerRepo.findByPhoneNumber(phoneNumber);
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, result);
    }
    return result;
  }

  @Override
  public Optional<ReaderDetails> findByUsername(final String username) {
    String cacheKey = "reader:username:" + username;
    ReaderDetails cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<ReaderDetails>() {
      });
      if (cached != null)
        return Optional.of(cached);
    }
    Optional<ReaderDetails> reader = this.readerRepo.findByUsername(username);
    if (cacheRepository != null && reader.isPresent()) {
      cacheRepository.save(cacheKey, reader.get());
    }
    return reader;
  }

  public Optional<ReaderDetails> findByUserId(String userId) {
    String cacheKey = "reader:userId:" + userId;
    ReaderDetails cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<ReaderDetails>() {
      });
      if (cached != null)
        return Optional.of(cached);
    }
    Optional<ReaderDetails> reader = this.readerRepo.findByUserId(userId);
    if (cacheRepository != null && reader.isPresent()) {
      cacheRepository.save(cacheKey, reader.get());
    }
    return reader;
  }

  @Override
  public Iterable<ReaderDetails> findAll() {
    String cacheKey = "readers:all";
    List<ReaderDetails> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<List<ReaderDetails>>() {
      });
      if (cached != null)
        return cached;
    }
    List<ReaderDetails> readers = new ArrayList<>();
    this.readerRepo.findAll().forEach(readers::add);
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, readers);
    }
    return readers;
  }

  @Override
  public List<ReaderDetails> findTopReaders(int minTop) {
    if (minTop < 1) {
      throw new IllegalArgumentException("Minimum top reader must be greater than 0");
    }
    String cacheKey = "readers:top:" + minTop;
    List<ReaderDetails> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<List<ReaderDetails>>() {
      });
      if (cached != null)
        return cached;
    }
    Pageable pageableRules = PageRequest.of(0, minTop);
    Page<ReaderDetails> page = readerRepo.findTopReaders(pageableRules);
    List<ReaderDetails> result = page.getContent();
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, result);
    }
    return result;
  }

  private List<Genre> getGenreListFromStringList(List<String> interestList) {
    if (interestList == null) {
      return null;
    }

    if (interestList.isEmpty()) {
      return new ArrayList<>();
    }

    List<Genre> genreList = new ArrayList<>();
    for (String interest : interestList) {
      Optional<Genre> optGenre = genreRepo.findByString(interest);
      if (optGenre.isEmpty()) {
        throw new NotFoundException("Could not find genre with name " + interest);
      }

      genreList.add(optGenre.get());
    }

    return genreList;
  }

  @Override
  public Optional<ReaderDetails> removeReaderPhoto(String readerNumber, long desiredVersion) {
    ReaderDetails readerDetails = readerRepo.findByReaderNumber(readerNumber)
        .orElseThrow(() -> new NotFoundException("Cannot find reader"));

    String photoFile = readerDetails.getPhoto().getPhotoFile();
    readerDetails.removePhoto(desiredVersion);
    Optional<ReaderDetails> updatedReader = Optional.of(readerRepo.save(readerDetails));
    photoRepository.deleteByPhotoFile(photoFile);
    return updatedReader;
  }

  @Override
  public List<ReaderDetails> searchReaders(pt.psoft.g1.psoftg1.shared.services.Page page, SearchReadersQuery query) {
    if (page == null)
      page = new pt.psoft.g1.psoftg1.shared.services.Page(1, 10);

    if (query == null)
      query = new SearchReadersQuery("", "", "");

    String cacheKey = "readers:search:" + page.getNumber() + ":" + page.getLimit() + ":" +
        query.getName() + ":" + query.getEmail() + ":" + query.getPhoneNumber();

    List<ReaderDetails> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<List<ReaderDetails>>() {
      });
      if (cached != null)
        return cached;
    }

    final var list = readerRepo.searchReaderDetails(page, query);

    if (list.isEmpty())
      throw new NotFoundException("No results match the search query");

    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, list);
    }
    return list;
  }
}
