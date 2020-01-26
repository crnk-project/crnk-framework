package io.crnk.example.springboot.domain.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.links.Link;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.list.ResourceListBase;
import io.crnk.core.resource.meta.PagedMetaInformation;
import io.crnk.example.springboot.domain.model.Project;

public interface ProjectRepository extends ResourceRepository<Project, Long> {

	@Override
	ProjectList findAll(QuerySpec querySpec);

	class ProjectListMeta implements PagedMetaInformation {

		private Long totalResourceCount;

		@Override
		public Long getTotalResourceCount() {
			return totalResourceCount;
		}

		@Override
		public void setTotalResourceCount(Long totalResourceCount) {
			this.totalResourceCount = totalResourceCount;
		}

	}

	class ProjectListLinks implements PagedLinksInformation {

		private Link first;

		private Link last;

		private Link next;

		private Link prev;

		@Override
		public Link getFirst() {
			return first;
		}

		@Override
		public void setFirst(Link first) {
			this.first = first;
		}

		@Override
		public Link getLast() {
			return last;
		}

		@Override
		public void setLast(Link last) {
			this.last = last;
		}

		@Override
		public Link getNext() {
			return next;
		}

		@Override
		public void setNext(Link next) {
			this.next = next;
		}

		@Override
		public Link getPrev() {
			return prev;
		}

		@Override
		public void setPrev(Link prev) {
			this.prev = prev;
		}

	}

	class ProjectList extends ResourceListBase<Project, ProjectListMeta, ProjectListLinks> {

	}
}
