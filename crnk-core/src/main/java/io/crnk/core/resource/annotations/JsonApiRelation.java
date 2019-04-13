package io.crnk.core.resource.annotations;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.queryspec.QuerySpec;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.crnk.core.resource.annotations.LookupIncludeBehavior.DEFAULT;
import static io.crnk.core.resource.annotations.SerializeType.LAZY;

/**
 * Indicates a relationship to eithe to either a single value or collection which needs to be handled by a separate
 * relationship resource.
 * <p>
 * For detailed information see <a href="http://www.crnk.io/releases/stable/documentation/#_jsonapirelation">here</a>.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonApiRelation {


    /**
     * Name of the idField annotated with @JsonApiRelationId.
     */
    String idField() default "";

    /**
     * Establishes a bi-directional relationship between two fields by specifying the name of the opposite
     * attribute that owns the relationship. The owner is accessed for read and update operations
     * as long as no dedicated relationship repository is implemented or {@link #repositoryBehavior()}
     * specifies differently.
     * <p>
     * For example, for a one-to-many relationship, the owning side is typically the single-valued field, whereas the
     * multi-valued
     * field declares the mappedBy to point to the single-valued field. This is because the single-valued field
     * is typically something like a column in a database table that can be accessed and updated. In contrast,
     * the multi-valued field can only be obtained by issuing a query against the single-valued field.
     *
     * @return whether a field is the owner.
     */
    String mappedBy() default "";

    /**
     * use {@link #mappedBy} but make sure to make the declration on the correct side.
     */
    @Deprecated
    String opposite() default "";

    /**
     * (Optional) Defines whether the data associated to the relationship should be serialized when making a request.
     * <p>
     * LAZY (Default) - is serialize the relationship when requested with an include query legacy.
     * <p>
     * ONLY_ID - is only serialize the ids in the resources relationship section but not the included section.
     * <p>
     * EAGER - is always serialize this relationship.
     */
    SerializeType serialize() default LAZY;

    /**
     * JSON:API allows complex requests with inclusion of related resources through the <i>include</i> parameter.
     * Behind the scenes Crnk can greatly help in implementing this feature by doing the work for the
     * repositories. There are three different behaviors:
     *
     * <p>
     * DEFAULT (Default) - consults the global lookup behavior set by {@link CrnkProperties#INCLUDE_AUTOMATICALLY} and
     * {@link CrnkProperties#INCLUDE_AUTOMATICALLY_OVERWRITE} first, using the value set by these global settings.  If not
     * set globally, this setting will fall back to {@link LookupIncludeBehavior#NONE}.
     * <p>
     * NONE - repository implementation do inclusions manually by honoring {@link QuerySpec#getIncludedRelations()}.
     * <p>
     * AUTOMATICALLY_WHEN_NULL - automatically perform inclusions by querying relationship and other resource repositories
     * if the given field is null. For {@link JsonApiRelationId} the targeted resource repository can be invoked
     * directly. Relationship repository may implement the functionality manullay. And
     * {@link #repositoryBehavior()} and {@link #owner()} help with built-iu default relationship repository
     * implementations.
     * <p>
     * AUTOMATICALLY_ALWAYS - always automatically perform inclusion.
     */
    LookupIncludeBehavior lookUp() default DEFAULT;


    /**
     * @return behavior of the relationship repository. From providing a manual implementation to reusing an implicit
     * implementation that forwards calls to resource repositories. See {@link RelationshipRepositoryBehavior}
     * and {@link #owner()} for more information.
     */
    RelationshipRepositoryBehavior repositoryBehavior() default RelationshipRepositoryBehavior.DEFAULT;
}
