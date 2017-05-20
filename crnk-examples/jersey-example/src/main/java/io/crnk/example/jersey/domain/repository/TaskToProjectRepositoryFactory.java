package io.crnk.example.jersey.domain.repository;

import org.glassfish.hk2.api.Factory;

public class TaskToProjectRepositoryFactory implements Factory<TaskToProjectRepository> {

	@Override
	public TaskToProjectRepository provide() {
		return new TaskToProjectRepository();
	}

	@Override
	public void dispose(TaskToProjectRepository instance) {

	}
}