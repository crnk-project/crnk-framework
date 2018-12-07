package io.crnk.example.springboot.domain.repository;

import io.crnk.example.springboot.domain.model.UserEntity;
import io.crnk.jpa.JpaEntityRepositoryBase;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryImpl extends JpaEntityRepositoryBase<UserEntity, Long> {

    public UserRepositoryImpl() {
        super(UserEntity.class);
    }
}