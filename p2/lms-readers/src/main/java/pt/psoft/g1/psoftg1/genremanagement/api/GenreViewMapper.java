package pt.psoft.g1.psoftg1.genremanagement.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.shared.api.MapperInterface;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class GenreViewMapper extends MapperInterface {

    // mapeia do campo 'genre' da entidade para o campo 'genre' do DTO
    @Mapping(target = "genre", source = "genre")
    public abstract GenreView toGenreView(Genre genre);

    public abstract List<GenreView> toGenreView(List<Genre> genres);

    // útil quando já tens só uma String (nome do genre)
    public abstract GenreView mapStringToGenreView(String genre);
}