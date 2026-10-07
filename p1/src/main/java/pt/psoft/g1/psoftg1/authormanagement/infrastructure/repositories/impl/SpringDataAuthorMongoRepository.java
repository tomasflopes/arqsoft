package pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;

import java.util.List;
import java.util.Optional;

@Repository("springDataAuthorMongoRepository")
@Profile("mongo")
public interface SpringDataAuthorMongoRepository extends AuthorRepository, MongoRepository<Author, String> {
    @Override
    Optional<Author> findByAuthorNumber(String authorNumber);

    @Override
    @Query(value = "{ 'name.name': { $regex: ?0, $options: 'i' } }", collation = "{ locale: 'pt', strength: 1 }")
    List<Author> searchByNameNameStartsWith(String name);

    @Override
    @Query(value = "{ 'name.name': ?0 }", collation = "{ locale: 'pt', strength: 1 }")
    List<Author> searchByNameName(String name);


    @Aggregation(pipeline = {
        "{ $lookup: { from: 'lending', localField: 'authorNumber', foreignField: 'authorNumber', as: 'lendings' } }",
        "{ $addFields: { lendingCount: { $size: '$lendings' } } }",
        "{ $project: { " +
        "  author: '$$ROOT', " +
        "  lendingCount: 1 " +
        "} }",
        "{ $sort: { lendingCount: -1 } }",
        "{ $skip: ?#{#pageable.offset} }",
        "{ $limit: ?#{#pageable.pageSize} }"
    })
    Slice<AuthorLendingView> findTopAuthorByLendingsSlice(Pageable pageable);
    
    @Override
    default Page<AuthorLendingView> findTopAuthorByLendings(Pageable pageable) {
        Slice<AuthorLendingView> slice = findTopAuthorByLendingsSlice(pageable);
        return new PageImpl<>(
            slice.getContent(), 
            pageable, 
            slice.hasNext() ? pageable.getOffset() + slice.getNumberOfElements() + 1 
                            : pageable.getOffset() + slice.getNumberOfElements()
        );
    }

    @Override
    @Aggregation(pipeline = {   
        "{ $unwind: '$books' }",
        "{ $lookup: { from: 'author', localField: 'books.authors', foreignField: '_id', as: 'coAuthors' } }",
        "{ $unwind: '$coAuthors' }",
        "{ $match: { 'authorNumber': ?0, 'coAuthors.authorNumber': { $ne: ?0 } } }",
        "{ $group: { _id: '$coAuthors.authorNumber', coAuthor: { $first: '$coAuthors' } } }"
    })
    List<Author> findCoAuthorsByAuthorNumber(String authorNumber);
}