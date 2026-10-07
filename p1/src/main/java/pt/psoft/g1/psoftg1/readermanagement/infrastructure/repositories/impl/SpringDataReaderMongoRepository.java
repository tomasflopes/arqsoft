package pt.psoft.g1.psoftg1.readermanagement.infrastructure.repositories.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderDetailsRepoCustom;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO;
import pt.psoft.g1.psoftg1.readermanagement.services.SearchReadersQuery;

@Repository("springDataReaderMongoRepository")
@Profile("mongo")
public interface SpringDataReaderMongoRepository
    extends ReaderRepository, ReaderDetailsRepoCustom, MongoRepository<ReaderDetails, String> {

  @Override
  @org.springframework.data.mongodb.repository.Query("{ 'readerNumber.readerNumber': ?0 }")
  Optional<ReaderDetails> findByReaderNumber(String readerNumber);

  @Override
  @org.springframework.data.mongodb.repository.Query("{ 'phoneNumber.phoneNumber': ?0 }")
  List<ReaderDetails> findByPhoneNumber(String phoneNumber);

  @Override
  @org.springframework.data.mongodb.repository.Query("{ 'reader.user.name': ?0 }")
  Optional<ReaderDetails> findByUsername(String username);

  @Override
  @org.springframework.data.mongodb.repository.Query("{ 'reader.id': ?0 }")
  Optional<ReaderDetails> findByUserId(String userId);

  @Override
  @org.springframework.data.mongodb.repository.Query(value = "{ 'reader.createdAt': { $gte: { $dateFromParts: { year: { $year: new Date() } } } } }", count = true)
  int getCountFromCurrentYear();

  @Override
  @org.springframework.data.mongodb.repository.Query("{}")
  Page<ReaderDetails> findTopReaders(Pageable pageable);

  @Override
  @org.springframework.data.mongodb.repository.Query(value = "{ 'lendings.book.genre.genre': ?0, 'lendings.startDate': { $gte: ?1, $lte: ?2 } }")
  Page<ReaderBookCountDTO> findTopByGenre(Pageable pageable, String genre, LocalDate startDate, LocalDate endDate);
}

// Implementação customizada no mesmo ficheiro

@RequiredArgsConstructor
@Profile("mongo")
class SpringDataReaderMongoRepositoryImpl implements ReaderDetailsRepoCustom {

  private final MongoTemplate mongoTemplate;

  @Override
  public List<ReaderDetails> searchReaderDetails(pt.psoft.g1.psoftg1.shared.services.Page page,
      SearchReadersQuery query) {
    Query mongoQuery = new Query();
    List<Criteria> criteriaList = new ArrayList<>();

    // Nome - busca em 'name.name' (objeto Name dentro de Reader)
    if (query.getName() != null && !query.getName().isBlank()) {
      criteriaList.add(Criteria.where("reader.name.name").regex(query.getName()));
    }

    // Email - busca em 'reader.username'
    if (query.getEmail() != null && !query.getEmail().isBlank()) {
      criteriaList.add(Criteria.where("username").is(query.getEmail()));
    }

    // Número de telefone
    if (query.getPhoneNumber() != null && !query.getPhoneNumber().isBlank()) {
      criteriaList.add(Criteria.where("phoneNumber.phoneNumber").is(query.getPhoneNumber()));
    }

    // OR entre os critérios (tal como no SQL)
    if (!criteriaList.isEmpty()) {
      mongoQuery.addCriteria(new Criteria().orOperator(criteriaList.toArray(new Criteria[0])));
    }

    // Paginação
    mongoQuery.skip((long) (page.getNumber() - 1) * page.getLimit());
    mongoQuery.limit(page.getLimit());

    return mongoTemplate.find(mongoQuery, ReaderDetails.class);
  }
}
