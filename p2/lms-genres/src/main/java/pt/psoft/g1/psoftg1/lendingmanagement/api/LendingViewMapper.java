package pt.psoft.g1.psoftg1.lendingmanagement.api;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.shared.api.MapperInterface;

@Mapper(componentModel = "spring")
public abstract class LendingViewMapper extends MapperInterface {
  @Mapping(target = "lendingNumber", source = "lendingNumber")
  @Mapping(target = "bookTitle", source = "book.title")
  @Mapping(target = "fineValueInCents", expression = "java(lending.getFineValueInCents().orElse(null))")
  @Mapping(target = "returnedDate", source = "returnedDate")

  public abstract LendingView toLendingView(Lending lending);

  public abstract List<LendingView> toLendingView(List<Lending> lendings);

  public abstract LendingsAverageDurationView toLendingsAverageDurationView(Double lendingsAverageDuration);
}
