package pt.psoft.g1.psoftg1.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenLibraryApiConfig {
    @Bean("openLibraryRestTemplate")
    public RestTemplate openLibraryRestTemplate() {
        return new RestTemplate();
    }
}