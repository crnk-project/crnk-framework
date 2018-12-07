package io.crnk.example.springboot.domain.repository;

import io.crnk.example.springboot.domain.model.ScheduleEntity;
import io.crnk.jpa.JpaEntityRepositoryBase;
import org.springframework.stereotype.Component;

@Component
public class ScheduleRepositoryImpl extends JpaEntityRepositoryBase<ScheduleEntity, Long> {

    public ScheduleRepositoryImpl() {
        super(ScheduleEntity.class);
    }
}