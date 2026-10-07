package pt.psoft.g1.psoftg1.readermanagement.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.g1.psoftg1.readermanagement.model.Reader;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReaderViewAMQPMapper {

    @Mapping(target = "readerNumber", source = "readerNumber")
    @Mapping(target = "id", source = "pk") 
    ReaderViewAMQP toReaderViewAMQP(Reader reader);

    List<ReaderViewAMQP> toReaderViewAMQPList(List<Reader> readers);
}