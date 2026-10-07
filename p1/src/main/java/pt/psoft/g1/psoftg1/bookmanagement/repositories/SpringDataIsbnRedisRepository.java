package pt.psoft.g1.psoftg1.bookmanagement.repositories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.shared.repositories.CacheRepository;

import java.util.concurrent.TimeUnit;

@Repository("springDataIsbnRedisRepository")
@Profile("redis")
public class SpringDataIsbnRedisRepository implements CacheRepository {
  private final RedisTemplate<String, Object> redisTemplate;

  public SpringDataIsbnRedisRepository(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void save(String key, Object value) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    try {
      String json = mapper.writeValueAsString(value);
      redisTemplate.opsForValue().set(key, json, 3, TimeUnit.MINUTES);
    } catch (Exception e) {
      throw new RuntimeException("Error serializing object to JSON", e);
    }
  }

  @Override
  public <T> T find(String key, TypeReference<T> typeRef) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    try {
      String json = (String) redisTemplate.opsForValue().get(key);
      if (json == null)
        return null;
      return mapper.readValue(json, typeRef);
    } catch (Exception e) {
      throw new RuntimeException("Error deserializing JSON from Redis", e);
    }
  }

  @Override
  public void delete(String key) {
    redisTemplate.delete(key);
  }
}
