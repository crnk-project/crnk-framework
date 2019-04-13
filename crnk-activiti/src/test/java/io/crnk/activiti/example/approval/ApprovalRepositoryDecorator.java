package io.crnk.activiti.example.approval;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;
import io.crnk.core.repository.decorate.WrappedResourceRepository;
import io.crnk.test.mock.models.Schedule;

import java.util.UUID;

public class ApprovalRepositoryDecorator<T> extends WrappedResourceRepository<T, UUID> {


    private final ApprovalManager approvalManager;

    // tag::docs_decorator[]
    public static final RepositoryDecoratorFactory createFactory(ApprovalManager approvalManager) {
        return repository -> {
            if (repository instanceof ResourceRepository && ((ResourceRepository) repository).getResourceClass() == Schedule.class) {
                return new ApprovalRepositoryDecorator(approvalManager, (ResourceRepository) repository);
            }
            return repository;
        };
    }
    // end::docs_decorator[]

    public ApprovalRepositoryDecorator(ApprovalManager approvalManager, ResourceRepository repository) {
        super(repository);
        this.approvalManager = approvalManager;
    }

    // tag::docs_save[]
    @Override
    public <S extends T> S save(S entity) {
        if (approvalManager.needsApproval(entity, HttpMethod.PATCH)) {
            return approvalManager.requestApproval(entity, HttpMethod.PATCH);
        } else {
            return super.save(entity);
        }
    }
    // end::docs_save[]

    @Override
    public <S extends T> S create(S entity) {
        if (approvalManager.needsApproval(entity, HttpMethod.POST)) {
            return approvalManager.requestApproval(entity, HttpMethod.POST);
        } else {
            return super.create(entity);
        }
    }

    @Override
    public void delete(UUID id) {
        T entity = super.findOne(id, new QuerySpec(getResourceClass()));
        if (approvalManager.needsApproval(entity, HttpMethod.DELETE)) {
            approvalManager.requestApproval(entity, HttpMethod.DELETE);
        } else {
            super.delete(id);
        }
    }

}
