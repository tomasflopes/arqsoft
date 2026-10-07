package pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepoCustom;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;
import pt.psoft.g1.psoftg1.bookmanagement.services.SearchBooksQuery;

@Repository("springDataBookRepository")
@Profile("mongo")
public interface SpringDataBookMongoRepository extends BookRepository, MongoRepository<Book, String> {

    @org.springframework.data.mongodb.repository.Query("{ 'isbn.isbn': ?0 }")
    Optional<Book> findByIsbn(String isbn);

    @Aggregation(pipeline = {
        "{ $lookup: { from: 'lending', localField: 'isbn.isbn', foreignField: 'bookIsbn', as: 'lendings' } }",
        "{ $match: { 'lendings.returnedDate': { $gte: ?0 } } }",
        "{ $addFields: { lendingCount: { $size: '$lendings' } } }",
        "{ $project: { " +
        "  book: '$$ROOT', " +
        "  lendingCount: 1 " +
        "} }",
        "{ $sort: { lendingCount: -1 } }",
        "{ $skip: ?#{#pageable.offset} }",
        "{ $limit: ?#{#pageable.pageSize} }"
    })
    Slice<BookCountDTO> findTop5BooksLentSlice(LocalDate startDate, Pageable pageable);

    @Override
    default Page<BookCountDTO> findTop5BooksLent(LocalDate startDate, Pageable pageable) {
        Slice<BookCountDTO> slice = findTop5BooksLentSlice(startDate, pageable);
        return new PageImpl<>(
            slice.getContent(),
            pageable,
            slice.hasNext() ? pageable.getOffset() + slice.getNumberOfElements() + 1 
                            : pageable.getOffset() + slice.getNumberOfElements()
        );
    }

    @org.springframework.data.mongodb.repository.Query("{ 'genre.genre': { $regex: ?0, $options: 'i' } }")
    List<Book> findByGenre(String genre);

    @org.springframework.data.mongodb.repository.Query("{ 'title.title': { $regex: ?0, $options: 'i' } }")
    List<Book> findByTitle(String title);

    @org.springframework.data.mongodb.repository.Query("{ 'authors.name.name': { $regex: ?0, $options: 'i' } }")
    List<Book> findByAuthorName(String name);

    @org.springframework.data.mongodb.repository.Query("{ 'authors.authorNumber': ?0 }")
    List<Book> findBooksByAuthorNumber(String authorNumber);
}


@Component
@RequiredArgsConstructor
@Profile("mongo")
class SpringDataBookMongoRepositoryImpl implements BookRepoCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Book> searchBooks(pt.psoft.g1.psoftg1.shared.services.Page page, SearchBooksQuery query) {
        final Query mongoQuery = new Query();
        final List<Criteria> criteria = new ArrayList<>();

        // Filtro por título
        if (StringUtils.hasText(query.getTitle())) {
            criteria.add(Criteria.where("title.title").regex("^" + query.getTitle(), "i"));
        }

        // Filtro por género
        if (StringUtils.hasText(query.getGenre())) {
            criteria.add(Criteria.where("genre.genre").regex("^" + query.getGenre(), "i"));
        }

        // Filtro por nome do autor - precisa fazer lookup manualmente
        if (StringUtils.hasText(query.getAuthorName())) {
            // Busca authors primeiro
            Query authorQuery = new Query(Criteria.where("name.name").regex("^" + query.getAuthorName(), "i"));
            List<pt.psoft.g1.psoftg1.authormanagement.model.Author> authors = 
                mongoTemplate.find(authorQuery, pt.psoft.g1.psoftg1.authormanagement.model.Author.class);
            
            if (authors.isEmpty()) {
                // Se não encontrou autores, retorna lista vazia
                return new ArrayList<>();
            }
            
            // Busca por authorNumber em vez de tentar acessar o nome através do DBRef
            List<String> authorNumbers = authors.stream()
                .map(a -> a.getAuthorNumber())
                .toList();
            
            criteria.add(Criteria.where("authors.authorNumber").in(authorNumbers));
        }

        // Combinar critérios com AND (busca mais restritiva)
        if (!criteria.isEmpty()) {
            mongoQuery.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }

        // Ordenação por título
        mongoQuery.with(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.ASC, "title.title"
        ));

        // Paginação
        mongoQuery.skip((long) (page.getNumber() - 1) * page.getLimit());
        mongoQuery.limit(page.getLimit());

        return mongoTemplate.find(mongoQuery, Book.class);
    }
}