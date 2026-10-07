package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepoCustom;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository("springDataLendingMongoRepository")
@Profile("mongo")
public interface SpringDataLendingMongoRepository 
    extends LendingRepository, LendingRepoCustom, MongoRepository<Lending, String> {

    @Override
    @org.springframework.data.mongodb.repository.Query("{ 'lendingNumber.lendingNumber': ?0 }")
    Optional<Lending> findByLendingNumber(String lendingNumber);

    @Override
    @org.springframework.data.mongodb.repository.Query("{ 'book.isbn.isbn': ?1, 'readerDetails.readerNumber.readerNumber': ?0 }")
    List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn);

    @Override
    @org.springframework.data.mongodb.repository.Query(value = "{ 'startDate': { $gte: ISODate(?0) } }", count = true)
    int getCountFromCurrentYear();

    @Override
    @org.springframework.data.mongodb.repository.Query("{ 'readerDetails.readerNumber.readerNumber': ?0, 'returnedDate': null }")
    List<Lending> listOutstandingByReaderNumber(String readerNumber);

    @Override
    @org.springframework.data.mongodb.repository.Query(value = "{ 'returnedDate': { $exists: true } }", 
           fields = "{ 'duration': { $subtract: ['$returnedDate', '$startDate'] } }")
    Double getAverageDuration();

    @Override
    @org.springframework.data.mongodb.repository.Query(value = "{ 'book.isbn.isbn': ?0, 'returnedDate': { $exists: true } }",
           fields = "{ 'duration': { $subtract: ['$returnedDate', '$startDate'] } }")
    Double getAvgLendingDurationByIsbn(String isbn);
}


@RequiredArgsConstructor
@Profile("mongo")
class SpringDataLendingMongoRepositoryImpl implements LendingRepoCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Lending> getOverdue(Page page) {
        LocalDate today = LocalDate.now();
        
        Query query = new Query();
        
        // Lending atrasado: returnedDate é null E limitReturnDate < hoje
        query.addCriteria(
            new Criteria().andOperator(
                Criteria.where("returnedDate").is(null),
                Criteria.where("limitReturnDate").lt(today)
            )
        );
        
        // Ordenar por limitDate (mais antigo primeiro)
        query.with(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Order.asc("limitDate")
        ));
        
        // Paginação
        query.skip((long) (page.getNumber() - 1) * page.getLimit());
        query.limit(page.getLimit());
        
        return mongoTemplate.find(query, Lending.class);
    }

    @Override
    public List<Lending> searchLendings(Page page, String readerNumber, String isbn, 
                                        Boolean returned, LocalDate startDate, LocalDate endDate) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Filtro por número do leitor
        if (readerNumber != null && !readerNumber.isBlank()) {
            criteriaList.add(Criteria.where("readerDetails.readerNumber.readerNumber").regex(readerNumber));
        }

        // Filtro por ISBN
        if (isbn != null && !isbn.isBlank()) {
            criteriaList.add(Criteria.where("book.isbn.isbn").regex(isbn));
        }

        // Filtro por status de devolução
        if (returned != null) {
            if (returned) {
                criteriaList.add(Criteria.where("returnedDate").ne(null));
            } else {
                criteriaList.add(Criteria.where("returnedDate").is(null));
            }
        }

        // Filtro por data de início
        if (startDate != null) {
            criteriaList.add(Criteria.where("startDate").gte(startDate));
        }

        // Filtro por data de fim
        if (endDate != null) {
            criteriaList.add(Criteria.where("startDate").lte(endDate));
        }

        // Aplicar critérios com AND
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        // Ordenar por lendingNumber
        query.with(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Order.asc("lendingNumber.lendingNumber")
        ));

        // Paginação
        query.skip((long) (page.getNumber() - 1) * page.getLimit());
        query.limit(page.getLimit());

        return mongoTemplate.find(query, Lending.class);
    }
}