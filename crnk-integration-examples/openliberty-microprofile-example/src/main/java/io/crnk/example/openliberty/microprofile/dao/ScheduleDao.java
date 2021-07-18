package io.crnk.example.openliberty.microprofile.dao;

import io.crnk.example.openliberty.microprofile.model.ScheduleEntity;

import javax.ejb.Startup;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@RequestScoped
@Startup
public class ScheduleDao {

    @Inject
    private EntityManager em;

    public void add(ScheduleEntity scheduleEntity) {
        em.getTransaction().begin();
        em.persist(scheduleEntity);
        em.getTransaction().commit();
    }
}
