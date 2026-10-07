package pt.psoft.g1.psoftg1.bookmanagement.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookViewAMQP {
  private String isbn;
  private String title;
  private String description;
  private String genre;
  private List<Long> authorIds;
  private Long version;
}
