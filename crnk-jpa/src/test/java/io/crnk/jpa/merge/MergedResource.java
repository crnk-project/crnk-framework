package io.crnk.jpa.merge;

import io.crnk.jpa.annotations.JpaMergeRelations;
import io.crnk.jpa.annotations.JpaResource;
import io.crnk.jpa.model.TestEntity;

@JpaResource(type = "merged")
@JpaMergeRelations(attributes = {"oneRelatedValue", "manyRelatedValues"})
public class MergedResource extends TestEntity {

}
