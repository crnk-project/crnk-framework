package io.crnk.example.openliberty.microprofile.endpoints;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.jpa.JpaEntityRepositoryBase;
import io.crnk.example.openliberty.microprofile.dao.ScheduleDao;
import io.crnk.example.openliberty.microprofile.model.ScheduleEntity;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;

public class ScheduleRepository extends JpaEntityRepositoryBase<ScheduleEntity, Long> {

    @Inject
	ScheduleDao scheduleDao;

    @PostConstruct
    public void init() {
        scheduleDao.add(new ScheduleEntity(101L, "Tight"));
		scheduleDao.add(new ScheduleEntity(102L, "Loose"));
		scheduleDao.add(new ScheduleEntity(103L, "Buffered"));
    }

    public ScheduleRepository() {
        super(ScheduleEntity.class);
    }

    @Override
    public ResourceList<ScheduleEntity> findAll(Collection<Long> ids, QuerySpec querySpec) {
        return super.findAll(ids, querySpec);
    }

    @Override
    public ResourceList<ScheduleEntity> findAll(QuerySpec querySpec) {
        return super.findAll(querySpec);
    }
}