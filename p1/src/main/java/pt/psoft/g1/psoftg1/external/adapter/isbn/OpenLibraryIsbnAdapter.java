package pt.psoft.g1.psoftg1.external.adapter.isbn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import pt.psoft.g1.psoftg1.bookmanagement.model.Isbn;
import pt.psoft.g1.psoftg1.external.schemas.OpenLibraryResponse;

@Service
@Profile("openlibrary")
public class OpenLibraryIsbnAdapter implements IsbnFetcher {
  private static final Logger LOGGER = LogManager.getLogger();

  private final RestTemplate restTemplate;

  @Autowired
  public OpenLibraryIsbnAdapter(@Qualifier("openLibraryRestTemplate") RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public Isbn fetchIsbnByTitle(String title) {
    String url = "https://openlibrary.org/search.json?title=" + title.replace(" ", "+");
    OpenLibraryResponse response = restTemplate.getForObject(url, OpenLibraryResponse.class);

    if (response != null && response.docs != null) {
      for (OpenLibraryResponse.Doc doc : response.docs) {
        // Procura no campo "isbn"
        if (doc.isbn != null) {
          for (String isbn : doc.isbn) {
            String cleanIsbn = isbn.replaceAll("[^\\d]", "");
            if (cleanIsbn.length() == 13) {
              LOGGER.info("operation=fetchIsbnByTitle msg='ISBN encontrado em isbn: {}'", cleanIsbn);
              return new Isbn(cleanIsbn);
            }
          }
        }
        // Procura no campo "ia"
        if (doc.ia != null) {
          for (String ia : doc.ia) {
            if (ia.startsWith("isbn_")) {
              String cleanIsbn = ia.replaceAll("[^\\d]", "");
              if (cleanIsbn.length() == 13) {
                LOGGER.info("operation=fetchIsbnByTitle msg='ISBN encontrado em ia: {}'", cleanIsbn);
                return new Isbn(cleanIsbn);
              }
            }
          }
        }
      }
    }
    return null;
  }
}
