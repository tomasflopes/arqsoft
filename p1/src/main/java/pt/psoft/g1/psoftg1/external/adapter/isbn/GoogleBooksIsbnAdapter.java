package pt.psoft.g1.psoftg1.external.adapter.isbn;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import pt.psoft.g1.psoftg1.bookmanagement.model.Isbn;
import pt.psoft.g1.psoftg1.configuration.GoogleBooksApiConfig;
import pt.psoft.g1.psoftg1.external.schemas.GoogleBooksResponse;
import pt.psoft.g1.psoftg1.external.schemas.GoogleBooksResponse.IndustryIdentifier;

@Service
@Profile("googlebooks")
public class GoogleBooksIsbnAdapter implements IsbnFetcher {
  @Autowired
  private GoogleBooksApiConfig fetcher;

  @Override
  public Isbn fetchIsbnByTitle(String title) {
    return fetcher.webClient().get()
        .uri(uriBuilder -> uriBuilder
            .path("volumes")
            .queryParam("q", "intitle:" + title)
            .build())
        .retrieve()
        .bodyToMono(GoogleBooksResponse.class)
        .map(response -> {
          List<IndustryIdentifier> identifiers = Arrays.asList(response.items[0].volumeInfo.industryIdentifiers);
          if (response.items.length == 0)
            return null;

          String isbn = identifiers.stream()
              .filter(id -> "ISBN_13".equals(id.type))
              .findFirst()
              .map(id -> id.identifier)
              .orElse(null);
          return new Isbn(isbn);
        })
        .block();
  }
}
