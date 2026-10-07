package pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepoCustom;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;
import pt.psoft.g1.psoftg1.usermanagement.services.SearchUsersQuery;

@Repository("springMongoDataUserRepository")
@CacheConfig(cacheNames = "users", cacheManager = "concurrentMapCacheManager")
@Profile("mongo")
public interface SpringDataUserMongoRepository extends UserRepository, MongoRepository<User, Long> {

  @Override
  @CacheEvict(allEntries = true)
  <S extends User> List<S> saveAll(Iterable<S> entities);

  @Override
  @Caching(evict = {
      @CacheEvict(key = "#p0.id", condition = "#p0.id != null"),
      @CacheEvict(key = "#p0.username", condition = "#p0.username != null")
  })
  <S extends User> S save(S entity);

  @Override
  @Cacheable
  Optional<User> findById(Long objectId);

  @Cacheable
  Optional<User> findByUsername(String username);

  @Cacheable
  List<User> findByNameName(String name);

  List<User> findByNameNameContains(String name);
}

@RequiredArgsConstructor
@Profile("mongo")
class SpringDataUserMongoRepositoryImpl implements UserRepoCustom {

  private final MongoTemplate mongoTemplate;

  @Override
  public List<User> searchUsers(final Page page, final SearchUsersQuery query) {
    final Query mongoQuery = new Query();

    if (StringUtils.hasText(query.getUsername()) || StringUtils.hasText(query.getFullName())) {
      final Criteria criteria = new Criteria();

      if (StringUtils.hasText(query.getUsername()) && StringUtils.hasText(query.getFullName())) {
        criteria.orOperator(
            Criteria.where("username").is(query.getUsername()),
            Criteria.where("name.name").regex(query.getFullName(), "i"));
      } else if (StringUtils.hasText(query.getUsername())) {
        criteria.and("username").is(query.getUsername());
      } else {
        criteria.and("name.name").regex(query.getFullName(), "i");
      }

      mongoQuery.addCriteria(criteria);
    }

    mongoQuery.skip((long) (page.getNumber() - 1) * page.getLimit());
    mongoQuery.limit(page.getLimit());

    return mongoTemplate.find(mongoQuery, User.class);
  }
}
