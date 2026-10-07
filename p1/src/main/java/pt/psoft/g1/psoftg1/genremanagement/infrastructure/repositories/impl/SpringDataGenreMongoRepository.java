package pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.impl;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepoCustom;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsDTO;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsPerMonthDTO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Repository("springDataGenreMongoRepository")
@Profile("mongo")
public interface SpringDataGenreMongoRepository
  extends GenreRepository, GenreRepoCustom, MongoRepository<Genre, Integer> {
  @Query("{}")
  List<Genre> findAllGenres();

  @Override
  @Query("{ 'genre': ?0 }")
  Optional<Genre> findByString(@Param("genreName") @NotNull String genreName);

  @org.springframework.data.mongodb.repository.Aggregation(pipeline = {
    "{ $lookup: { from: 'book', localField: 'genre', foreignField: 'genre.genre', as: 'books' } }",
    "{ $addFields: { bookCount: { $size: '$books' } } }",
    "{ $sort: { bookCount: -1 } }",
    "{ $skip: ?#{#pageable.offset} }",
    "{ $limit: ?#{#pageable.pageSize} }"
  })
  List<Genre> findTop5GenreByBookCountInternal(Pageable pageable);

  @Override
  default Page findTop5GenreByBookCount(Pageable pageable) {
    List<Genre> genres = findTop5GenreByBookCountInternal(pageable);
    return new PageImpl<>(genres, pageable, genres.size());
  }
}

@RequiredArgsConstructor
@Profile("mongo")
class SpringDataGenreMongoRepositoryImpl implements GenreRepoCustom {

  private final MongoTemplate mongoTemplate;
  private static final String START_DATE_FIELD = "startDate";

  @Override
  public List<GenreLendingsPerMonthDTO> getLendingsPerMonthLastYearByGenre() {
    LocalDate now = LocalDate.now();
    LocalDate twelveMonthsAgo = now.minusMonths(12);

    Date startDate = Date.from(twelveMonthsAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date endDate = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());

    MatchOperation matchStage = Aggregation.match(
      Criteria.where(START_DATE_FIELD).gte(startDate).lte(endDate));

    ProjectionOperation projectStage = Aggregation.project()
      .and("book.genre.genre").as("genre")
      .and(START_DATE_FIELD).extractYear().as("year")
      .and(START_DATE_FIELD).extractMonth().as("month");

    GroupOperation groupStage = Aggregation.group("genre", "year", "month")
      .count().as("count");

    SortOperation sortStage = Aggregation.sort(
      org.springframework.data.domain.Sort.by("_id.year", "_id.month", "_id.genre").ascending());

    Aggregation aggregation = Aggregation.newAggregation(
      matchStage, projectStage, groupStage, sortStage);

    AggregationResults<Map> results = mongoTemplate.aggregate(
      aggregation, "lending", Map.class);

    return processGroupedResults(results.getMappedResults());
  }

  @Override
  public List<GenreLendingsDTO> getAverageLendingsInMonth(LocalDate month,
                                                          pt.psoft.g1.psoftg1.shared.services.Page page) {
    int days = month.lengthOfMonth();
    LocalDate firstOfMonth = LocalDate.of(month.getYear(), month.getMonth(), 1);
    LocalDate lastOfMonth = LocalDate.of(month.getYear(), month.getMonth(), days);

    Date startDate = Date.from(firstOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date endDate = Date.from(lastOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());

    MatchOperation matchStage = Aggregation.match(
      Criteria.where(START_DATE_FIELD).gte(startDate).lte(endDate));

    GroupOperation groupStage = Aggregation.group("book.genre.genre")
      .count().as("loanCount");

    ProjectionOperation projectStage = Aggregation.project()
      .and("_id").as("genre")
      .andExpression("loanCount / " + days).as("dailyAverage");

    Aggregation aggregation = Aggregation.newAggregation(
      matchStage, groupStage, projectStage,
      Aggregation.skip((long) (page.getNumber() - 1) * page.getLimit()),
      Aggregation.limit(page.getLimit()));

    AggregationResults<GenreLendingsDTO> results = mongoTemplate.aggregate(
      aggregation, "lending", GenreLendingsDTO.class);

    return results.getMappedResults();
  }

  @Override
  public List<GenreLendingsPerMonthDTO> getLendingsAverageDurationPerMonth(LocalDate startDate, LocalDate endDate) {
    Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

    MatchOperation matchStage = Aggregation.match(
      Criteria.where(START_DATE_FIELD).gte(start).lte(end)
        .and("returnedDate").ne(null));

    ProjectionOperation projectStage = Aggregation.project()
      .and("book.genre.genre").as("genre")
      .and(START_DATE_FIELD).extractYear().as("year")
      .and(START_DATE_FIELD).extractMonth().as("month")
      .andExpression("(returnedDate - startDate) / (1000 * 60 * 60 * 24)").as("durationInDays");

    GroupOperation groupStage = Aggregation.group("genre", "year", "month")
      .avg("durationInDays").as("averageDuration");

    SortOperation sortStage = Aggregation.sort(
      org.springframework.data.domain.Sort.by("_id.year", "_id.month", "_id.genre").ascending());

    Aggregation aggregation = Aggregation.newAggregation(
      matchStage, projectStage, groupStage, sortStage);

    AggregationResults<Map> results = mongoTemplate.aggregate(
      aggregation, "lending", Map.class);

    return processGroupedResults(results.getMappedResults());
  }

  private List<GenreLendingsPerMonthDTO> processGroupedResults(List<Map> results) {
    Map<Integer, Map<Integer, List<GenreLendingsDTO>>> groupedResults = new HashMap<>();

    for (Map result : results) {
      Map<String, Object> id = (Map<String, Object>) result.get("_id");
      String genre = (String) id.get("genre");
      int year = ((Number) id.get("year")).intValue();
      int month = ((Number) id.get("month")).intValue();

      Number value = (Number) result.getOrDefault("count", result.get("averageDuration"));
      double count = value != null ? value.doubleValue() : 0.0;

      GenreLendingsDTO dto = new GenreLendingsDTO(genre, count);

      groupedResults
        .computeIfAbsent(year, k -> new HashMap<>())
        .computeIfAbsent(month, k -> new ArrayList<>())
        .add(dto);
    }

    List<GenreLendingsPerMonthDTO> lendingsPerMonth = new ArrayList<>();
    for (Map.Entry<Integer, Map<Integer, List<GenreLendingsDTO>>> yearEntry : groupedResults.entrySet()) {
      for (Map.Entry<Integer, List<GenreLendingsDTO>> monthEntry : yearEntry.getValue().entrySet()) {
        lendingsPerMonth.add(new GenreLendingsPerMonthDTO(
          yearEntry.getKey(),
          monthEntry.getKey(),
          monthEntry.getValue()));
      }
    }

    return lendingsPerMonth;
  }
}
