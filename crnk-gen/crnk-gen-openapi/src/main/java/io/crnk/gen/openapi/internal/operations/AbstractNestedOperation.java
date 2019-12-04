package io.crnk.gen.openapi.internal.operations;

import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;

abstract class AbstractNestedOperation extends AbstractOperation {

  final MetaResource metaResource;

  final MetaResource relatedMetaResource;

  final MetaResourceField metaResourceField;

  AbstractNestedOperation(MetaResource metaResource, MetaResourceField metaResourceField, MetaResource relatedMetaResource) {
    this.metaResource = metaResource;
    this.metaResourceField = metaResourceField;
    this.relatedMetaResource = relatedMetaResource;
  }
}
