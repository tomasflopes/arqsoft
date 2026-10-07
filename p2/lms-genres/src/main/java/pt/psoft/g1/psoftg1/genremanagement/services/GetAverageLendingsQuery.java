package pt.psoft.g1.psoftg1.genremanagement.services;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAverageLendingsQuery {
  @Getter
  private int year;

  @Min(value = 1)
  @Max(value = 12)
  @Getter
  private int month;
}
