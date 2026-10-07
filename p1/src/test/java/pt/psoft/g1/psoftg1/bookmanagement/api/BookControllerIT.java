package pt.psoft.g1.psoftg1.bookmanagement.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Isbn;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.external.adapter.isbn.IsbnFetcher;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerIT {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private AuthorRepository authorRepository;

  @Autowired
  private BookRepository bookRepository;

  @MockBean
  IsbnFetcher isbnFetcher;

  @Autowired
  private ObjectMapper objectMapper;

  private String loginAndGetToken() throws Exception {
    String loginJson = """
      {
          "username": "maria@gmail.com",
          "password": "Mariaroberta!123"
      }
      """;

    MvcResult result = mockMvc.perform(post("/api/public/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(loginJson))
      .andExpect(status().isOk())
      .andReturn();

    // Se o token vem no header:
    String token = result.getResponse().getHeader("Authorization");
    if (token != null)
      return "Bearer " + token;

    // Se o token vem no corpo (JSON):
    String responseBody = result.getResponse().getContentAsString();
    JsonNode json = objectMapper.readTree(responseBody);
    token = json.has("token") ? json.get("token").asText() : null;
    return "Bearer " + token;
  }

  @Test
  void testGetAllBooksWithFilters() throws Exception {
    String token = loginAndGetToken();

    String searchJson = """
      {
      "page": {
          "number": 1,
          "limit": 10
      },
      "query": {
          "title": "O Hobbit",
          "genre": "Fantasia",
          "authorName": "J R R Tolkien"
      }
      }
      """;

    mockMvc.perform(post("/api/books/search")
        .contentType(MediaType.APPLICATION_JSON)
        .content(searchJson)
        .header("Authorization", token))
      .andExpect(status().isOk());
  }

  @Test
  void testGetBooksByTitle() throws Exception {
    String token = loginAndGetToken();
    mockMvc.perform(get("/api/books")
        .param("title", "O Hobbit")
        .header("Authorization", token))
      .andExpect(status().isOk());
  }

  @Test
  void testGetBooksByGenre() throws Exception {
    String token = loginAndGetToken();
    mockMvc.perform(get("/api/books")
        .param("genre", "Fantasia")
        .header("Authorization", token))
      .andExpect(status().isOk());
  }

  @Test
  void testGetBooksByAuthorName() throws Exception {
    String token = loginAndGetToken();
    mockMvc.perform(get("/api/books")
        .param("authorName", "J R R Tolkien")
        .header("Authorization", token))
      .andExpect(status().isOk());
  }

  @Test
  void testGetBookByIsbn() throws Exception {
    String token = loginAndGetToken();
    mockMvc.perform(get("/api/books/{isbn}", "9789897776090")
        .header("Authorization", token))
      .andExpect(status().isOk());
  }


  @Test
  void shouldPersistBookWithoutIsbn() throws Exception {
    String token = loginAndGetToken();

    if (bookRepository.findByIsbn("9780132350884").isPresent()) {
      bookRepository.delete(bookRepository.findByIsbn("9780132350884").get());
    }

    Author author = authorRepository.findAll().iterator().next();
    String content = String.format("""
        {"description": "Livro clássico sobre desenvolvimento de software.",
          "title": "Clean Code",
          "genre": "Informação",
          "authors": ["%s"]}
      """, author.getId());

    when(isbnFetcher.fetchIsbnByTitle("Clean Code")).thenReturn(new Isbn("9780132350884"));

    mockMvc.perform(post("/api/books")
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(content))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.isbn").value("9780132350884"));
  }

  @Test
  void shouldFailIfNoIsbnIsFoundForBook() throws Exception {
    String token = loginAndGetToken();
    when(isbnFetcher.fetchIsbnByTitle("Unknown Book Title")).thenReturn(null);

    Author author = authorRepository.findAll().iterator().next();
    String content = String.format("""
        {"description": "Livro desconhecido.",
          "title": "Unknown Book Title",
          "genre": "Mistério",
          "authors": ["%s"]}
      """, author.getId());

    mockMvc.perform(post("/api/books")
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(content))
      .andExpect(status().isBadRequest());
  }
}
