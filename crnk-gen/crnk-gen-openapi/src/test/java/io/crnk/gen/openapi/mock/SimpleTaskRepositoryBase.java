package io.crnk.gen.openapi.mock;

import io.crnk.core.repository.ResourceRepositoryBase;

abstract class SimpleTaskRepositoryBase extends ResourceRepositoryBase<SimpleTask, Long> {

  public SimpleTaskRepositoryBase(Class<SimpleTask> resourceClass) {
    super(resourceClass);
  }
}
