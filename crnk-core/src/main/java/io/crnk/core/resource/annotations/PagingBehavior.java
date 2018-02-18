package io.crnk.core.resource.annotations;

import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpecDeserializer;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpecSerializer;
import io.crnk.core.queryspec.pagingspec.PagingSpecDeserializer;
import io.crnk.core.queryspec.pagingspec.PagingSpecSerializer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines paging behavior of the {@link JsonApiRelation} resource
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PagingBehavior {

	/**
	 * {@link io.crnk.core.queryspec.pagingspec.PagingSpec} serializer. Default is {@link io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpecSerializer}
	 * @return Instance of {@link PagingSpecSerializer}
	 */
	Class<? extends PagingSpecSerializer> serializer() default OffsetLimitPagingSpecSerializer.class;

	/**
	 * {@link io.crnk.core.queryspec.pagingspec.PagingSpec} deserializer. Default is {@link io.crnk.core.queryspec.paging.OffsetLimitPagingSpecDeserializer}
	 * @return Instance of {@link PagingSpecDeserializer}
	 */
	Class<? extends PagingSpecDeserializer> deserializer() default OffsetLimitPagingSpecDeserializer.class;
}
