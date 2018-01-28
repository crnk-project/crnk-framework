package io.crnk.core.mock.repository;

public class MockRepositoryUtil {

	public static void clear() {
		TaskRepository.clear();
		ProjectRepository.clear();
		TaskToProjectRepository.clear();
		HierarchicalTaskRepository.clear();
		ScheduleRepositoryImpl.clear();
		RelationIdTestRepository.clear();

		UserRepository.clear();
		UserToProjectRepository.clear();
		ProjectToTaskRepository.clear();
	}


}
