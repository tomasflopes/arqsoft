package pt.psoft.g1.psoftg1.authormanagement.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;
import pt.psoft.g1.psoftg1.usermanagement.model.Role;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Based on https://www.baeldung.com/spring-boot-testing
 * <p>
 * Adaptations to Junit 5 with ChatGPT
 */
/*
 * @ExtendWith(SpringExtension.class)
 *
 * @WebMvcTest(AuthorController.class)
 *
 * @AutoConfigureMockMvc
 * public class AuthorControllerIntegrationTest {
 *
 * @BeforeEach
 * public void setUp() {
 * MockitoAnnotations.openMocks(this);
 * }
 * }
 */

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthorControllerIT {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private Authentication authenticationManager;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private AuthorRepository authorRepository;

  @Autowired
  private PhotoRepository photoRepository;

  private Author author;
  private Photo photo;

  @BeforeEach
  @Transactional
  void setUp() {
    author = new Author("Manuel Antonio Pina",
      "Manuel António Pina foi um jornalista e escritor português, premiado em 2011 com o Prémio Camões",
      "uploads-psoft-g1/photoTest.jpg");

    if (authorRepository.findByAuthorNumber(author.getAuthorNumber()).isEmpty())
      authorRepository.save(author);

    photo = author.getPhoto();
    if (photoRepository.findByPhotoFile(photo.getPhotoFile()).isEmpty())
      photoRepository.save(photo);
  }

  @AfterEach
  @Transactional
  void tearDown() {
    if (authorRepository.findByAuthorNumber(author.getAuthorNumber()).isPresent())
      authorRepository.delete(author);

    if (photoRepository.findByPhotoFile(photo.getPhotoFile()).isPresent())
      photoRepository.deleteByPhotoFile(photo.getPhotoFile());
  }

  private String loginAndGetToken(String role) throws Exception {
    String loginJson = "";
    switch (role) {
      case Role.LIBRARIAN:
        loginJson = """
          {
              "username": "maria@gmail.com",
              "password": "Mariaroberta!123"
          }
          """;
        break;
      case Role.READER:
        loginJson = """
          {
              "username": "manuel@gmail.com",
              "password": "Manuelino123!"
          }
          """;
        break;
      default:
        break;
    }

    MvcResult result = mockMvc.perform(post("/api/public/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(loginJson))
      .andExpect(status().isOk())
      .andReturn();

    String token = result.getResponse().getHeader("Authorization");
    if (token != null)
      return "Bearer " + token;

    String responseBody = result.getResponse().getContentAsString();
    JsonNode json = objectMapper.readTree(responseBody);
    token = json.has("token") ? json.get("token").asText() : null;
    return "Bearer " + token;
  }

  @Test
  void testGetAuthorByNumber() throws Exception {
    String token = loginAndGetToken(Role.LIBRARIAN);
    mockMvc.perform(get("/api/authors/{authorNumber}", author.getAuthorNumber())
        .header("Authorization", token))
      .andExpect(status().isOk());
  }

  @Test
  void testSearchAuthorsByName() throws Exception {
    String token = loginAndGetToken(Role.LIBRARIAN);
    mockMvc.perform(get("/api/authors")
        .param("name", "Tolkien")
        .header("Authorization", token))
      .andExpect(status().isOk());
  }

  @Test
  void testCreateAuthor() throws Exception {
    String token = loginAndGetToken(Role.LIBRARIAN);
    String authorJson = """
      {
          "name": "Novo Autor",
          "bio": "Autor de testes",
          "photoURI": "http://example.com/photo2.jpg"
      }
      """;
    mockMvc.perform(post("/api/authors")
        .contentType(MediaType.APPLICATION_JSON)
        .content(authorJson)
        .header("Authorization", token))
      .andExpect(status().isCreated());
  }

  @Test
  void testGetAuthorDetail() throws Exception {
    String token = loginAndGetToken(Role.LIBRARIAN);
    mockMvc.perform(get("/api/authors/{authorNumber}", author.getAuthorNumber())
        .header("Authorization", token))
      .andExpect(status().isOk());
  }

  @Test
  @Transactional
  void testUpdateAuthor() throws Exception {
    String token = loginAndGetToken(Role.LIBRARIAN);
    String updateJson = """
      {
          "name": "Autor Atualizado"
      }
      """;
    mockMvc.perform(patch("/api/authors/{authorNumber}", author.getAuthorNumber())
        .contentType(MediaType.APPLICATION_JSON)
        .content(updateJson)
        .header("Authorization", token))
      .andExpect(status().isOk());
  }

  @Test
  void testGetAuthorPhoto() throws Exception {
    String token = loginAndGetToken(Role.LIBRARIAN);
    mockMvc.perform(get("/api/authors/{authorNumber}/photo", author.getAuthorNumber())
        .header("Authorization", token))
      .andExpect(status().isOk());
  }

  @Test
  void testGetCoAuthors() throws Exception {
    String token = loginAndGetToken(Role.READER);
    mockMvc.perform(get("/api/authors/{authorNumber}/coauthors", author.getAuthorNumber())
        .header("Authorization", token))
      .andExpect(status().isOk());
  }

  @Test
  void testGetAuthorBooks() throws Exception {
    String token = loginAndGetToken(Role.READER);
    mockMvc.perform(get("/api/authors/{authorNumber}/books", author.getAuthorNumber())
        .header("Authorization", token))
      .andExpect(status().isOk());
  }

  @Test
  void testGetTop5Authors() throws Exception {
    String token = loginAndGetToken(Role.LIBRARIAN);
    mockMvc.perform(get("/api/authors/top5")
        .header("Authorization", token))
      .andExpect(status().isOk());
  }
}
