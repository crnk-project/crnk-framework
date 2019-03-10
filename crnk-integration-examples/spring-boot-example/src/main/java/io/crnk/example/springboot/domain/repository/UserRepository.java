package io.crnk.example.springboot.domain.repository;

import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.example.springboot.domain.model.UserEntity;

public interface UserRepository extends ResourceRepositoryV2<UserEntity, String> {

}
