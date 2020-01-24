package io.crnk.test.mock.repository.nested;

import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.test.mock.models.nested.Post;

public class ParentRepository extends InMemoryResourceRepository<Post, String> {

	public ParentRepository() {
		super(Post.class);
	}
}



