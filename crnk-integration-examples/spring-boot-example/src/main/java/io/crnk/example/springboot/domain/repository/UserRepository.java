package io.crnk.example.springboot.domain.repository;

import io.crnk.core.repository.ResourceRepository;
import io.crnk.example.springboot.domain.model.UserEntity;

public interface UserRepository extends ResourceRepository<UserEntity, String> {

}
