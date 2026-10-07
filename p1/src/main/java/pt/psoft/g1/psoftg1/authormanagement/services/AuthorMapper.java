package pt.psoft.g1.psoftg1.authormanagement.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.shared.api.MapperInterface;

@Mapper(componentModel = "spring")
public abstract class AuthorMapper extends MapperInterface {
  
  public Author create(CreateAuthorRequest request) {
    return new Author(request.getName(), request.getBio(), request.getPhotoURI());
  }

  @Mapping(target = "name", ignore = true)
  @Mapping(target = "bio", ignore = true)
  public abstract void update(UpdateAuthorRequest request, @MappingTarget Author author);
}