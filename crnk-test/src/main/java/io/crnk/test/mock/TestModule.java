package io.crnk.test.mock;

import io.crnk.core.module.Module;
import io.crnk.test.mock.repository.PrimitiveAttributeRepository;
import io.crnk.test.mock.repository.ProjectRepository;
import io.crnk.test.mock.repository.ProjectToTaskRepository;
import io.crnk.test.mock.repository.RelationIdTestRepository;
import io.crnk.test.mock.repository.ScheduleRepositoryImpl;
import io.crnk.test.mock.repository.ScheduleToTaskRepository;
import io.crnk.test.mock.repository.TaskRepository;
import io.crnk.test.mock.repository.TaskSubtypeRepository;
import io.crnk.test.mock.repository.TaskToProjectRepository;
import io.crnk.test.mock.repository.TaskToScheduleRepo;
import io.crnk.test.mock.repository.nested.ManyNestedRepository;
import io.crnk.test.mock.repository.nested.NestedRelationshipRepository;
import io.crnk.test.mock.repository.nested.ParentRepository;
import io.crnk.test.mock.repository.nested.RelatedRepository;

public class TestModule implements Module {

	private static ManyNestedRepository manyNestedRepository = new ManyNestedRepository();

	private static RelatedRepository relatedRepository = new RelatedRepository();

	private static ParentRepository parentRepository = new ParentRepository();

	private static NestedRelationshipRepository nestedRelationshipRepository = new NestedRelationshipRepository();

	@Override
	public String getModuleName() {
		return "test";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addRepository(new TaskRepository());
		context.addRepository(new ProjectRepository());
		context.addRepository(new ScheduleRepositoryImpl());
		context.addRepository(new TaskSubtypeRepository());
		context.addRepository(new ProjectToTaskRepository());
		context.addRepository(new ScheduleToTaskRepository());
		context.addRepository(new TaskToProjectRepository());
		context.addRepository(new TaskToScheduleRepo());
		context.addRepository(new PrimitiveAttributeRepository());
		context.addRepository(new RelationIdTestRepository());

		context.addRepository(manyNestedRepository);
		context.addRepository(relatedRepository);
		context.addRepository(parentRepository);
		context.addRepository(nestedRelationshipRepository);

		context.addExceptionMapper(new TestExceptionMapper());
	}

	public static void clear() {
		TaskRepository.clear();
		ProjectRepository.clear();
		TaskToProjectRepository.clear();
		ProjectToTaskRepository.clear();
		ScheduleRepositoryImpl.clear();
		RelationIdTestRepository.clear();

		manyNestedRepository.clear();
		relatedRepository.clear();
		parentRepository.clear();
	}
}
