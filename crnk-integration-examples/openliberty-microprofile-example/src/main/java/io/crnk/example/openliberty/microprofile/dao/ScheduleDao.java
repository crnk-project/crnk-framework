package io.crnk.example.openliberty.microprofile.dao;

import io.crnk.example.openliberty.microprofile.model.ScheduleEntity;

import jakarta.ejb.Startup;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

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
