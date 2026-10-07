package pt.psoft.g1.psoftg1.shared.repositories;

import com.fasterxml.jackson.core.type.TypeReference;

public interface CacheRepository {
  void save(String key, Object value);

  <T> T find(String key, TypeReference<T> typeRef);

  void delete(String key);
}
