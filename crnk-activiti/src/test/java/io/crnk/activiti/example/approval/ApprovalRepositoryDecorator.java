package io.crnk.activiti.example.approval;

import java.io.Serializable;
import java.util.UUID;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactoryBase;
import io.crnk.core.repository.decorate.ResourceRepositoryDecorator;
import io.crnk.core.repository.decorate.ResourceRepositoryDecoratorBase;
import io.crnk.test.mock.models.Schedule;

public class ApprovalRepositoryDecorator<T> extends ResourceRepositoryDecoratorBase<T, UUID> {


	private final ApprovalManager approvalManager;

	public static final RepositoryDecoratorFactory createFactory(ApprovalManager approvalManager) {
		return new RepositoryDecoratorFactoryBase() {

			@Override
			public <T, I extends Serializable> ResourceRepositoryDecorator<T, I> decorateRepository(
					ResourceRepositoryV2<T, I> repository) {
				if (repository.getResourceClass() == Schedule.class) {
					return (ResourceRepositoryDecorator<T, I>) new ApprovalRepositoryDecorator(approvalManager);
				}
				return null;
			}
		};
	}

	public ApprovalRepositoryDecorator(ApprovalManager approvalManager) {
		this.approvalManager = approvalManager;
	}

	@Override
	public <S extends T> S save(S entity) {
		if (approvalManager.needsApproval(entity, HttpMethod.PATCH)) {
			return approvalManager.requestApproval(entity, HttpMethod.PATCH);
		}
		else {
			return super.save(entity);
		}
	}

	@Override
	public <S extends T> S create(S entity) {
		if (approvalManager.needsApproval(entity, HttpMethod.POST)) {
			return approvalManager.requestApproval(entity, HttpMethod.POST);
		}
		else {
			return super.create(entity);
		}
	}

	@Override
	public void delete(UUID id) {
		T entity = super.findOne(id, new QuerySpec(getResourceClass()));
		if (approvalManager.needsApproval(entity, HttpMethod.DELETE)) {
			approvalManager.requestApproval(entity, HttpMethod.DELETE);
		}
		else {
			super.delete(id);
		}
	}

}
