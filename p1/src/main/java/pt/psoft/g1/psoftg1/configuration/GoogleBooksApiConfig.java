package pt.psoft.g1.psoftg1.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableConfigurationProperties
@RequiredArgsConstructor
public class GoogleBooksApiConfig {
  private final String VERSION = "/v1/";

  @Value("${google.books.api.base-url}")
  private String baseUrl;

  @Bean("googleBooksWebClient")
  public WebClient webClient() {
    return WebClient.builder()
        .baseUrl(baseUrl + VERSION)
        .build();
  }
}
