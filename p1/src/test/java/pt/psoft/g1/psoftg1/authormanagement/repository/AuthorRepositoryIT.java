package pt.psoft.g1.psoftg1.authormanagement.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;

/**
 * Based on https://www.baeldung.com/spring-boot-testing
 * <p>
 * Adaptations to Junit 5 with ChatGPT
 */
@Transactional
@SpringBootTest
@ActiveProfiles("test")
public class AuthorRepositoryIT {
  @Autowired
  private AuthorRepository authorRepository;

  @Test
  public void whenFindByName_thenReturnAuthor() {
    // given
    Author alex = new Author("Alex", "O Alex escreveu livros", null);
    authorRepository.save(alex);

    // when
    List<Author> list = authorRepository.searchByNameName(alex.getName());

    // then
    assertThat(list).isNotEmpty();
    assertThat(list.get(0).getName())
        .isEqualTo(alex.getName());
  }
}
