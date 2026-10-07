package pt.psoft.g1.psoftg1.authormanagement.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;

/**
 * Based on https://www.baeldung.com/spring-boot-testing
 * <p>
 * Adaptations to Junit 5 with ChatGPT
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class AuthorServiceImplIT {
  @Autowired
  private AuthorService authorService;
  @MockBean
  private AuthorRepository authorRepository;

  @BeforeEach
  public void setUp() {
    Author alex = new Author("1", "Alex", "O Alex escreveu livros", null);
    List<Author> list = new ArrayList<>();
    list.add(alex);

    Mockito.when(authorRepository.findByAuthorNumber("1"))
      .thenReturn(Optional.of(alex));
    Mockito.when(authorRepository.searchByNameNameStartsWith(alex.getName()))
      .thenReturn(list);
  }

  @Test
  public void whenValidId_thenAuthorShouldBeFound() {
    String id = "1";
    Optional<Author> found = authorService.findByAuthorNumber(id);
    Assertions.assertTrue(found.isPresent());
    found.ifPresent(author -> assertThat(author.getId())
      .isEqualTo(id));
  }

  @Test
  public void whenSearchByName_thenReturnAuthor() {
    String name = "Alex";
    List<Author> found = authorService.findByName(name);
    Assertions.assertNotNull(found);
    Assertions.assertEquals(1, found.size());
    Assertions.assertEquals(name, found.get(0).getName());
  }
}
