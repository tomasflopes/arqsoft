package pt.psoft.g1.psoftg1.readermanagement.api;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.shared.api.MapperInterface;

@Mapper(componentModel = "spring")
public abstract class ReaderViewAMQPMapper extends MapperInterface {

  @Mapping(target = "readerNumber", expression = "java(readerDetails.getReaderNumber().toString())")
  @Mapping(target = "name", expression = "java(readerDetails.getReader().getName())")
  @Mapping(target = "email", expression = "java(readerDetails.getReader().getUsername())")
  @Mapping(target = "version", ignore = true) // definido pelo caller
  @Mapping(target = "_links", ignore = true)
  public abstract ReaderViewAMQP toReaderViewAMQP(ReaderDetails readerDetails);

  public abstract List<ReaderViewAMQP> toReaderViewAMQP(List<ReaderDetails> readerDetailsList);
}
