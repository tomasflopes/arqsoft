package pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import pt.psoft.g1.psoftg1.shared.repositories.CacheRepository;

@Repository("springDataAuthorRedisRepository")
@Profile("redis")
public class SpringDataAuthorRedisRepository implements CacheRepository {

  private final RedisTemplate<String, Object> redisTemplate;

  public SpringDataAuthorRedisRepository(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void save(String key, Object value) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      String json = mapper.writeValueAsString(value);
      redisTemplate.opsForValue().set(key, json, 3, TimeUnit.MINUTES);
    } catch (Exception e) {
      throw new RuntimeException("Error serializing object to JSON", e);
    }
  }

  @Override
  public <T> T find(String key, TypeReference<T> typeRef) {
    Object obj = redisTemplate.opsForValue().get(key);
    if (obj == null)
      return null;
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue((String) obj, typeRef);
    } catch (Exception e) {
      throw new RuntimeException("Error deserializing JSON from Redis", e);
    }
  }

  @Override
  public void delete(String key) {
    redisTemplate.delete(key);
  }
}
