package pt.psoft.g1.psoftg1.external.adapter.isbn;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.bookmanagement.model.Isbn;
import pt.psoft.g1.psoftg1.shared.repositories.CacheRepository;

import java.util.List;

@Component
@Primary
public class IsbnFetcherFacade implements IsbnFetcher {
  private static final Logger LOGGER = LogManager.getLogger();
  private static final String CACHE_KEY_PREFIX = "isbn::";

  private final CacheRepository isbnCacheRepository;
  private final List<IsbnFetcher> fetchers;

  public IsbnFetcherFacade(List<IsbnFetcher> fetchers,
                           @Autowired(required = false) @Qualifier("springDataBookRedisRepository") CacheRepository isbnCacheRepository) {
    this.fetchers = fetchers;
    this.isbnCacheRepository = isbnCacheRepository;
  }

  public Isbn fetchIsbnByTitle(String title) {
    if (isbnCacheRepository != null) {
      String cachedIsbn = isbnCacheRepository.find(CACHE_KEY_PREFIX + title, new TypeReference<String>() {
      });
      if (cachedIsbn != null) {
        LOGGER.info("operation=fetchIsbnByTitle msg='Cache hit for title={}' isbn='{}'", title, cachedIsbn);
        return new Isbn(cachedIsbn);
      }
      LOGGER.debug("operation=fetchIsbnByTitle msg='Cache miss for title={}'", title);
    }

    for (IsbnFetcher fetcher : fetchers) {
      String fetcherName = fetcher.getClass().getSimpleName();

      Isbn isbn = fetcher.fetchIsbnByTitle(title);
      LOGGER.info("operation=fetchIsbnByTitle msg='Tentativa de fetch com {}' title='{}' isbn='{}'",
        fetcherName, title, isbn);
      if (isbn != null) {
        LOGGER.info("operation=fetchIsbnByTitle msg='Fetch bem-sucedido com {}' title='{}' isbn='{}'",
          fetcherName, title, isbn);

        if (isbnCacheRepository != null) {
          isbnCacheRepository.save(CACHE_KEY_PREFIX + title, isbn.toString());
        }

        return isbn;
      }
    }
    return null;
  }
}
