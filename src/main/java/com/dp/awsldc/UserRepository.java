package com.dp.awsldc;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  @Cacheable(value = "users", key = "#cognitoSub")
  Optional<UserEntity> findByCognitoSub(String cognitoSub);
}
