package pt.psoft.g1.psoftg1.authormanagement.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;

import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.shared.repositories.CacheRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;

@Service
public class AuthorServiceImpl implements AuthorService {
  private final AuthorRepository authorRepository;
  private final BookRepository bookRepository;
  private final AuthorMapper mapper;
  private final PhotoRepository photoRepository;
  private final CacheRepository cacheRepository;

  public AuthorServiceImpl(
      AuthorRepository authorRepository,
      BookRepository bookRepository,
      AuthorMapper mapper,
      PhotoRepository photoRepository,
      @Autowired(required = false) @Qualifier("springDataAuthorRedisRepository") CacheRepository cacheRepository) {
    this.authorRepository = authorRepository;
    this.bookRepository = bookRepository;
    this.mapper = mapper;
    this.photoRepository = photoRepository;
    this.cacheRepository = cacheRepository;
  }

  @Override
  public Iterable<Author> findAll() {
    String cacheKey = "authors:all";
    List<Author> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new TypeReference<List<Author>>() {
      });
      if (cached != null)
        return cached;
    }
    List<Author> authors = (List<Author>) authorRepository.findAll();
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, authors);
    }
    return authors;
  }

  @Override
  public Optional<Author> findByAuthorNumber(final String authorNumber) {
    String cacheKey = "author:" + authorNumber;
    if (cacheRepository != null) {
      Optional<Author> cached = Optional.ofNullable(cacheRepository.find(cacheKey, new TypeReference<Author>() {
      }));
      if (cached.isPresent())
        return cached;
    }
    Optional<Author> author = authorRepository.findByAuthorNumber(authorNumber);
    if (cacheRepository != null) {
      author.ifPresent(a -> cacheRepository.save(cacheKey, a));
    }
    return author;
  }

  @Override
  public List<Author> findByName(String name) {
    String cacheKey = "authors:name:" + name;
    List<Author> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new TypeReference<List<Author>>() {
      });
      if (cached != null)
        return cached;
    }
    List<Author> authors = authorRepository.searchByNameNameStartsWith(name);
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, authors);
    }
    return authors;
  }

  @Override
  public Author create(final CreateAuthorRequest resource) {
    final Author author = mapper.create(resource);
    return authorRepository.save(author);
  }

  @Override
  public Author partialUpdate(final String authorNumber, final UpdateAuthorRequest request, final long desiredVersion) {
    // first let's check if the object exists so we don't create a new object with
    // save
    final var author = findByAuthorNumber(authorNumber)
        .orElseThrow(() -> new NotFoundException("Cannot update an object that does not yet exist"));
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
    String photoURI = request.getPhotoURI();
    if (photo == null && photoURI != null || photo != null && photoURI == null) {
      request.setPhoto(null);
      request.setPhotoURI(null);
    }
    // since we got the object from the database we can check the version in memory
    // and apply the patch
    author.applyPatch(desiredVersion, request);

    // in the meantime some other user might have changed this object on the
    // database, so concurrency control will still be applied when we try to save
    // this updated object
    return authorRepository.save(author);
  }

  @Override
  public List<AuthorLendingView> findTopAuthorByLendings() {
    String cacheKey = "authors:top:lending";
    List<AuthorLendingView> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new TypeReference<List<AuthorLendingView>>() {
      });
      if (cached != null)
        return cached;
    }
    Pageable pageableRules = PageRequest.of(0, 5);
    List<AuthorLendingView> result = authorRepository.findTopAuthorByLendings(pageableRules).getContent();
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, result);
    }
    return result;
  }

  @Override
  public List<Book> findBooksByAuthorNumber(String authorNumber) {
    String cacheKey = "author:" + authorNumber + ":books";
    List<Book> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new TypeReference<List<Book>>() {
      });
      if (cached != null)
        return cached;
    }
    List<Book> books = bookRepository.findBooksByAuthorNumber(authorNumber);
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, books);
    }
    return books;
  }

  @Override
  public List<Author> findCoAuthorsByAuthorNumber(String authorNumber) {
    String cacheKey = "author:" + authorNumber + ":coauthors";
    List<Author> cached = null;
    if (cacheRepository != null) {
      cached = cacheRepository.find(cacheKey, new TypeReference<List<Author>>() {
      });
      if (cached != null)
        return cached;
    }
    List<Author> coauthors = authorRepository.findCoAuthorsByAuthorNumber(authorNumber);
    if (cacheRepository != null) {
      cacheRepository.save(cacheKey, coauthors);
    }
    return coauthors;
  }

  @Override
  public Optional<Author> removeAuthorPhoto(String authorNumber, long desiredVersion) {
    Author author = authorRepository.findByAuthorNumber(authorNumber)
        .orElseThrow(() -> new NotFoundException("Cannot find reader"));

    String photoFile = author.getPhoto().getPhotoFile();
    author.removePhoto(desiredVersion);
    Optional<Author> updatedAuthor = Optional.of(authorRepository.save(author));
    photoRepository.deleteByPhotoFile(photoFile);
    return updatedAuthor;
  }

}
