package pt.psoft.g1.psoftg1.genremanagement.api;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.shared.api.MapperInterface;

@Mapper(componentModel = "spring")
public abstract class GenreViewAMQPMapper extends MapperInterface {

  @Mapping(target = "genre", source = "genre")
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "_links", ignore = true)

  public abstract GenreViewAMQP toGenreViewAMQP(Genre genre);

  public abstract List<GenreViewAMQP> toGenreViewAMQP(List<Genre> bookList);
}
