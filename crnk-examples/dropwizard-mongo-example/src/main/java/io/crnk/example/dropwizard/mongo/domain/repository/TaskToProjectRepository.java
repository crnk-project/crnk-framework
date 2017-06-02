package io.crnk.example.dropwizard.mongo.domain.repository;

import io.crnk.example.dropwizard.mongo.domain.model.Project;
import io.crnk.example.dropwizard.mongo.domain.model.Task;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.RelationshipRepository;
import org.apache.commons.beanutils.PropertyUtils;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TaskToProjectRepository implements RelationshipRepository<Task, ObjectId, Project, ObjectId> {

	private TaskRepository taskRepository;
	private ProjectRepository projectRepository;

	@Inject
	public TaskToProjectRepository(TaskRepository taskRepository, ProjectRepository projectRepository) {
		this.taskRepository = taskRepository;
		this.projectRepository = projectRepository;
	}

	@Override
	public void setRelation(Task task, ObjectId projectId, String fieldName) {
		Project project = projectRepository.findOne(projectId, null);
		try {
			PropertyUtils.setProperty(task, fieldName, project);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		taskRepository.save(task);
	}

	@Override
	public void setRelations(Task task, Iterable<ObjectId> projectIds, String fieldName) {
		Iterable<Project> projects = projectRepository.findAll(projectIds, null);
		try {
			PropertyUtils.setProperty(task, fieldName, projects);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		taskRepository.save(task);
	}

	/**
	 * A simple implementation of the addRelations method which presents the general concept of the method.
	 * It SHOULD NOT be used in production because of possible race condition - production ready code should perform an
	 * atomic operation.
	 *
	 * @param task
	 * @param projectIds
	 * @param fieldName
	 */
	@Override
	public void addRelations(Task task, Iterable<ObjectId> projectIds, String fieldName) {
		List<Project> newProjectList = new LinkedList<>();
		Iterable<Project> projectsToAdd = projectRepository.findAll(projectIds, null);
		for (Project project : projectsToAdd) {
			newProjectList.add(project);
		}
		try {
			if (PropertyUtils.getProperty(task, fieldName) != null) {
				Iterable<Project> projects = (Iterable<Project>) PropertyUtils.getProperty(task, fieldName);
				for (Project project : projects) {
					newProjectList.add(project);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		try {
			PropertyUtils.setProperty(task, fieldName, newProjectList);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		taskRepository.save(task);
	}

	/**
	 * A simple implementation of the removeRelations method which presents the general concept of the method.
	 * It SHOULD NOT be used in production because of possible race condition - production ready code should perform an
	 * atomic operation.
	 *
	 * @param task
	 * @param projectIds
	 * @param fieldName
	 */
	@Override
	public void removeRelations(Task task, Iterable<ObjectId> projectIds, String fieldName) {
		try {
			if (PropertyUtils.getProperty(task, fieldName) != null) {
				Iterable<Project> projects = (Iterable<Project>) PropertyUtils.getProperty(task, fieldName);
				Iterator<Project> iterator = projects.iterator();
				while (iterator.hasNext()) {
					for (ObjectId projectIdToRemove : projectIds) {
						if (iterator.next().getId().equals(projectIdToRemove)) {
							iterator.remove();
							break;
						}
					}
				}
				List<Project> newProjectList = new ArrayList<>();
				for (Project project : projects) {
					newProjectList.add(project);
				}

				PropertyUtils.setProperty(task, fieldName, newProjectList);
				taskRepository.save(task);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public Project findOneTarget(ObjectId objectId, String fieldName, QueryParams requestParams) {
		Task task = taskRepository.findOne(objectId, requestParams);
		try {
			return (Project) PropertyUtils.getProperty(task, fieldName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Iterable<Project> findManyTargets(ObjectId objectId, String fieldName, QueryParams requestParams) {
		Task task = taskRepository.findOne(objectId, requestParams);
		try {
			return (Iterable<Project>) PropertyUtils.getProperty(task, fieldName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
