package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.ComplexPojo;
import io.crnk.core.mock.models.ContainedPojo;
import io.crnk.core.repository.InMemoryResourceRepository;

public class ComplexPojoRepository extends InMemoryResourceRepository<ComplexPojo, Long> {

    public ComplexPojoRepository() {
        super(ComplexPojo.class);

        ComplexPojo complexPojo = new ComplexPojo();
        complexPojo.setContainedPojo(new ContainedPojo());
        complexPojo.getContainedPojo().setUpdateableProperty1("value from repository mock");
        complexPojo.getContainedPojo().setUpdateableProperty2("value from repository mock");
        complexPojo.setId(1l);
        resources.put(complexPojo.getId(), complexPojo);
    }

    @Override
    public <S extends ComplexPojo> S save(S entity) {
        if (entity.getId() == null) {
            entity.setId((long) (resources.size() + 1));
        }
        return super.save(entity);
    }
}
