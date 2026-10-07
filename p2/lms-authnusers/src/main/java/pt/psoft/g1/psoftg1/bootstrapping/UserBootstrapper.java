package pt.psoft.g1.psoftg1.bootstrapping;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.usermanagement.model.Librarian;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;

@Component
@RequiredArgsConstructor
@Profile("bootstrap")
@Order(1)
public class UserBootstrapper implements CommandLineRunner {

  private final UserRepository userRepository;
  private final JdbcTemplate jdbcTemplate;
  private List<String> queriesToExecute = new ArrayList<>();

  @Override
  @Transactional
  public void run(final String... args) {
    createReaders();
    createLibrarian();
    executeQueries();
  }

  private void createReaders() {
    // Reader1 - Manuel
    if (userRepository.findByUsername("manuel@gmail.com").isEmpty()) {
      final Reader manuel = Reader.newReader("manuel@gmail.com", "Manuelino123!", "Manuel Sarapinto das Coives");
      userRepository.save(manuel);
    }

    // Reader2 - João
    if (userRepository.findByUsername("joao@gmail.com").isEmpty()) {
      final Reader joao = Reader.newReader("joao@gmail.com", "Joaoratao!123", "João Ratao");
      userRepository.save(joao);
    }

    // Reader3 - Pedro
    if (userRepository.findByUsername("pedro@gmail.com").isEmpty()) {
      final Reader pedro = Reader.newReader("pedro@gmail.com", "Pedrodascenas!123", "Pedro Das Cenas");
      userRepository.save(pedro);
    }

    // Reader4 - Catarina
    if (userRepository.findByUsername("catarina@gmail.com").isEmpty()) {
      final Reader catarina = Reader.newReader("catarina@gmail.com", "Catarinamartins!123", "Catarina Martins");
      userRepository.save(catarina);
    }

    // Reader5 - Marcelo
    if (userRepository.findByUsername("marcelo@gmail.com").isEmpty()) {
      final Reader marcelo = Reader.newReader("marcelo@gmail.com", "Marcelosousa!123", "Marcelo Rebelo de Sousa");
      userRepository.save(marcelo);
    }

    // Reader6 - Luís
    if (userRepository.findByUsername("luis@gmail.com").isEmpty()) {
      final Reader luis = Reader.newReader("luis@gmail.com", "Luismontenegro!123", "Luís Montenegro");
      userRepository.save(luis);
    }

    // Reader7 - António
    if (userRepository.findByUsername("antonio@gmail.com").isEmpty()) {
      final Reader antonio = Reader.newReader("antonio@gmail.com", "Antoniocosta!123", "António Costa");
      userRepository.save(antonio);
    }

    // Reader8 - André
    if (userRepository.findByUsername("andre@gmail.com").isEmpty()) {
      final Reader andre = Reader.newReader("andre@gmail.com", "Andreventura!123", "André Ventura");
      userRepository.save(andre);
    }
  }

  private void createLibrarian() {
    // Maria
    if (userRepository.findByUsername("maria@gmail.com").isEmpty()) {
      final User maria = Librarian.newLibrarian("maria@gmail.com", "Mariaroberta!123", "Maria Roberta");
      userRepository.save(maria);
    }
  }

  private void executeQueries() {
    for (String query : queriesToExecute) {
      jdbcTemplate.update(query);
    }
  }
}
