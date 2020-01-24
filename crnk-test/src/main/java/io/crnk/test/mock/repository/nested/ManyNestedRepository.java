package io.crnk.test.mock.repository.nested;

import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.test.mock.models.nested.PostComment;
import io.crnk.test.mock.models.nested.PostCommentId;

public class ManyNestedRepository extends InMemoryResourceRepository<PostComment, PostCommentId> {

	public ManyNestedRepository() {
		super(PostComment.class);
	}
}