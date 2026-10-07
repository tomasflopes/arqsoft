package pt.psoft.g1.psoftg1.bootstrapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.usermanagement.model.Librarian;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@Profile("bootstrap")
@Order(1)
public class UserBootstrapper implements CommandLineRunner {
  private final Logger logger = LogManager.getLogger(UserBootstrapper.class);

  private final UserRepository userRepository;
  private final ReaderRepository readerRepository;
  private final GenreRepository genreRepository;
  private final Environment environment;

  @Autowired(required = false)
  private JdbcTemplate jdbcTemplate;

  @Autowired(required = false)
  private MongoTemplate mongoTemplate;

  private List<String> queriesToExecute = new ArrayList<>();
  private boolean isMongoProfile;

  public UserBootstrapper(UserRepository userRepository,
                          ReaderRepository readerRepository,
                          GenreRepository genreRepository,
                          Environment environment) {
    this.userRepository = userRepository;
    this.readerRepository = readerRepository;
    this.genreRepository = genreRepository;
    this.environment = environment;
  }

  @Override
  public void run(final String... args) {
    isMongoProfile = Arrays.asList(environment.getActiveProfiles()).contains("mongo");

    if (isMongoProfile && mongoTemplate != null) {
      try {
        mongoTemplate.dropCollection(User.class);
      } catch (Exception e) {
      }

      try {
        mongoTemplate.dropCollection(ReaderDetails.class);
      } catch (Exception e) {
        logger.error("Error dropping ReaderDetails collection: {}", e.getMessage());
      }

      try {
        mongoTemplate.dropCollection(Genre.class);
      } catch (Exception e) {
        logger.error("Error dropping Genre collection: {}", e.getMessage());
      }
    }

    createReaders();
    createLibrarian();

    // Só executa queries SQL se não for MongoDB
    if (!isMongoProfile) {
      executeQueries();
    }

  }

  private void createReaders() {

    // Reader1 - Manuel
    Optional<User> existingManuelUser = userRepository.findByUsername("manuel@gmail.com");

    if (existingManuelUser.isEmpty()) {
      final Reader manuel = Reader.newReader("manuel@gmail.com", "Manuelino123!", "Manuel Sarapinto das Coives");
      Reader savedManuel = userRepository.save(manuel);

      // Só adiciona query SQL se não for MongoDB
      if (!isMongoProfile) {
        String dateFormat = LocalDateTime.of(2024, 1, 20, 0, 0, 0, 0)
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        String query = String.format("UPDATE T_USER SET CREATED_AT = '%s' WHERE USERNAME = '%s'", dateFormat,
          savedManuel.getUsername());
        queriesToExecute.add(query);
      }

      String readerNumber = LocalDate.now().getYear() + "/1";
      Optional<ReaderDetails> readerDetails1 = readerRepository.findByReaderNumber(readerNumber);

      Optional<Genre> g1 = genreRepository.findByString("Fantasia");
      Optional<Genre> g2 = genreRepository.findByString("Infantil");

      List<Genre> interestList = new ArrayList<>();
      if (g1.isPresent()) {
        interestList.add(g1.get());
      }

      if (g2.isPresent()) {
        interestList.add(g2.get());
      }

      if (readerDetails1.isEmpty()) {
        ReaderDetails r1 = new ReaderDetails(
          1,
          savedManuel,
          "2000-01-01",
          "919191919",
          true,
          true,
          true,
          "readerPhotoTest.jpg",
          interestList);
        readerRepository.save(r1);
      }
    }
    // Reader2 - João
    if (userRepository.findByUsername("joao@gmail.com").isEmpty()) {
      final Reader joao = Reader.newReader("joao@gmail.com", "Joaoratao!123", "João Ratao");
      userRepository.save(joao);

      if (!isMongoProfile) {
        String dateFormat = LocalDateTime.of(LocalDate.of(2024, 3, 20), LocalTime.now())
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        String query = String.format("UPDATE T_USER SET CREATED_AT = '%s' WHERE USERNAME = '%s'", dateFormat,
          joao.getUsername());
        queriesToExecute.add(query);
      }

      Optional<ReaderDetails> readerDetails2 = readerRepository.findByReaderNumber(LocalDate.now().getYear() + "/2");
      if (readerDetails2.isEmpty()) {
        ReaderDetails r2 = new ReaderDetails(
          2,
          joao,
          "1995-06-02",
          "929292929",
          true,
          false,
          false,
          null,
          null);
        readerRepository.save(r2);
      }
    }

    // Reader3 - Pedro
    if (userRepository.findByUsername("pedro@gmail.com").isEmpty()) {
      final Reader pedro = Reader.newReader("pedro@gmail.com", "Pedrodascenas!123", "Pedro Das Cenas");
      userRepository.save(pedro);

      if (!isMongoProfile) {
        String dateFormat = LocalDateTime.of(LocalDate.of(2024, 1, 20), LocalTime.now())
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        String query = String.format("UPDATE T_USER SET CREATED_AT = '%s' WHERE USERNAME = '%s'", dateFormat,
          pedro.getUsername());
        queriesToExecute.add(query);
      }

      Optional<ReaderDetails> readerDetails3 = readerRepository.findByReaderNumber(LocalDate.now().getYear() + "/3");
      if (readerDetails3.isEmpty()) {
        ReaderDetails r3 = new ReaderDetails(
          3,
          pedro,
          "2001-12-03",
          "939393939",
          true,
          false,
          true,
          null,
          null);
        readerRepository.save(r3);
      }
    }

    // Reader4 - Catarina
    if (userRepository.findByUsername("catarina@gmail.com").isEmpty()) {
      final Reader catarina = Reader.newReader("catarina@gmail.com", "Catarinamartins!123", "Catarina Martins");
      userRepository.save(catarina);

      if (!isMongoProfile) {
        String dateFormat = LocalDateTime.of(LocalDate.of(2024, 3, 20), LocalTime.now())
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        String query = String.format("UPDATE T_USER SET CREATED_AT = '%s' WHERE USERNAME = '%s'", dateFormat,
          catarina.getUsername());
        queriesToExecute.add(query);
      }

      Optional<ReaderDetails> readerDetails4 = readerRepository.findByReaderNumber(LocalDate.now().getYear() + "/4");
      if (readerDetails4.isEmpty()) {
        ReaderDetails r4 = new ReaderDetails(
          4,
          catarina,
          "2002-03-20",
          "912345678",
          true,
          false,
          true,
          null,
          null);
        readerRepository.save(r4);
      }
    }

    // Reader5 - Marcelo
    if (userRepository.findByUsername("marcelo@gmail.com").isEmpty()) {
      final Reader marcelo = Reader.newReader("marcelo@gmail.com", "Marcelosousa!123", "Marcelo Rebelo de Sousa");
      userRepository.save(marcelo);

      if (!isMongoProfile) {
        String dateFormat = LocalDateTime.of(LocalDate.of(2024, 1, 20), LocalTime.now())
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        String query = String.format("UPDATE T_USER SET CREATED_AT = '%s' WHERE USERNAME = '%s'", dateFormat,
          marcelo.getUsername());
        queriesToExecute.add(query);
      }

      Optional<ReaderDetails> readerDetails5 = readerRepository.findByReaderNumber(LocalDate.now().getYear() + "/5");
      if (readerDetails5.isEmpty()) {
        ReaderDetails r5 = new ReaderDetails(
          5,
          marcelo,
          "2000-06-03",
          "912355678",
          true,
          false,
          true,
          null,
          null);
        readerRepository.save(r5);
      }
    }

    // Reader6 - Luís
    if (userRepository.findByUsername("luis@gmail.com").isEmpty()) {
      final Reader luis = Reader.newReader("luis@gmail.com", "Luismontenegro!123", "Luís Montenegro");
      userRepository.save(luis);

      if (!isMongoProfile) {
        String dateFormat = LocalDateTime.of(LocalDate.of(2024, 3, 20), LocalTime.now())
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        String query = String.format("UPDATE T_USER SET CREATED_AT = '%s' WHERE USERNAME = '%s'", dateFormat,
          luis.getUsername());
        queriesToExecute.add(query);
      }

      Optional<ReaderDetails> readerDetails5 = readerRepository.findByReaderNumber(LocalDate.now().getYear() + "/6");
      if (readerDetails5.isEmpty()) {
        ReaderDetails r6 = new ReaderDetails(
          6,
          luis,
          "1999-03-03",
          "912355678",
          true,
          false,
          true,
          null,
          null);
        readerRepository.save(r6);
      }
    }

    // Reader7 - António
    if (userRepository.findByUsername("antonio@gmail.com").isEmpty()) {
      final Reader antonio = Reader.newReader("antonio@gmail.com", "Antoniocosta!123", "António Costa");
      userRepository.save(antonio);

      if (!isMongoProfile) {
        String dateFormat = LocalDateTime.of(LocalDate.of(2024, 6, 20), LocalTime.now())
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        String query = String.format("UPDATE T_USER SET CREATED_AT = '%s' WHERE USERNAME = '%s'", dateFormat,
          antonio.getUsername());
        queriesToExecute.add(query);
      }

      Optional<ReaderDetails> readerDetails5 = readerRepository.findByReaderNumber(LocalDate.now().getYear() + "/7");
      if (readerDetails5.isEmpty()) {
        ReaderDetails r7 = new ReaderDetails(
          7,
          antonio,
          "2001-03-03",
          "912355778",
          true,
          false,
          true,
          null,
          null);
        readerRepository.save(r7);
      }
    }

    // Reader8 - André
    if (userRepository.findByUsername("andre@gmail.com").isEmpty()) {
      final Reader andre = Reader.newReader("andre@gmail.com", "Andreventura!123", "André Ventura");
      userRepository.save(andre);

      if (!isMongoProfile) {
        String dateFormat = LocalDateTime.of(LocalDate.of(2024, 5, 20), LocalTime.now())
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        String query = String.format("UPDATE T_USER SET CREATED_AT = '%s' WHERE USERNAME = '%s'", dateFormat,
          andre.getUsername());
        queriesToExecute.add(query);
      }

      Optional<ReaderDetails> readerDetails5 = readerRepository.findByReaderNumber(LocalDate.now().getYear() + "/8");
      if (readerDetails5.isEmpty()) {
        ReaderDetails r5 = new ReaderDetails(
          8,
          andre,
          "2001-03-03",
          "912355888",
          true,
          false,
          true,
          null,
          null);
        readerRepository.save(r5);
      }
    }
  }

  private void createLibrarian() {
    // Maria
    if (userRepository.findByUsername("maria@gmail.com").isEmpty()) {
      final Librarian maria = Librarian.newLibrarian("maria@gmail.com", "Mariaroberta!123", "Maria Roberta");
      userRepository.save(maria);
    }
  }

  private void executeQueries() {
    // Só executa queries SQL se JdbcTemplate estiver disponível (perfil SQL)
    if (jdbcTemplate != null && tableExists("T_USER")) {
      for (String query : queriesToExecute) {
        jdbcTemplate.update(query);
      }
    }
  }

  private boolean tableExists(String tableName) {
    if (jdbcTemplate == null) {
      return false;
    }
    try {
      jdbcTemplate.queryForObject("SELECT 1 FROM " + tableName + " LIMIT 1", Integer.class);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
