package pt.psoft.g1.psoftg1.lendingmanagement.api;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.shared.api.MapperInterface;

@Mapper(componentModel = "spring")
public abstract class LendingViewAMQPMapper extends MapperInterface {
  @Mapping(target = "lendingNumber", source = "lending.lendingNumber")
  @Mapping(target = "readerNumber", source = "readerNumber")
  @Mapping(target = "bookId", source = "book.isbn")
  @Mapping(target = "startDate", source = "startDate")
  @Mapping(target = "limitDate", source = "limitDate")
  @Mapping(target = "returnedDate", source = "returnedDate")

  public abstract LendingViewAMQP toLendingViewAMQP(Lending lending);

  public abstract List<LendingViewAMQP> toLendingViewAMQP(List<Lending> lendingList);
}
