package io.crnk.jpa.internal.facet;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.repository.decorate.ResourceRepositoryDecoratorBase;
import io.crnk.core.utils.Prioritizable;
import io.crnk.data.facet.FacetValue;
import io.crnk.data.facet.config.BasicFacetInformation;
import io.crnk.data.facet.config.FacetInformation;
import io.crnk.data.facet.provider.FacetProviderBase;
import io.crnk.jpa.JpaEntityRepository;
import io.crnk.jpa.internal.JpaRepositoryUtils;
import io.crnk.jpa.internal.JpaRequestContext;
import io.crnk.jpa.internal.query.QueryBuilder;
import io.crnk.jpa.internal.query.backend.criteria.JpaCriteriaQueryBackend;
import io.crnk.jpa.internal.query.backend.criteria.JpaCriteriaQueryImpl;
import io.crnk.jpa.query.JpaQueryFactory;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpaFacetProvider extends FacetProviderBase implements Prioritizable {


	@Override
	public boolean accepts(RegistryEntry entry) {
		Object resourceRepository = entry.getResourceRepository().getResourceRepository();
		return unwrap(resourceRepository) instanceof JpaEntityRepository;
	}

	private Object unwrap(Object resourceRepository) {
		// TODO ResourceRepositoryDecorator support
		while (resourceRepository instanceof ResourceRepositoryDecoratorBase) {
			resourceRepository = ((ResourceRepositoryDecoratorBase) resourceRepository).getDecoratedObject();
		}
		return resourceRepository;
	}

	@Override
	public List<FacetValue> findValues(FacetInformation facetInformation, QuerySpec querySpec) {
		if (facetInformation instanceof BasicFacetInformation) {
			String resourceType = facetInformation.getResource().getType();
			ResourceRepositoryV2 repository = (ResourceRepositoryV2) context.getEntry(resourceType).getResourceRepository().getResourceRepository();
			JpaEntityRepository entityRepository = (JpaEntityRepository) unwrap(repository);
			BasicFacetInformation basicFacetInformation = (BasicFacetInformation) facetInformation;

			PathSpec path = basicFacetInformation.getPath();

			TypeParser typeParser = context.getTypeParser();
			Map<Object, FacetValue> facetValueMap = new HashMap<>();

			// setup query
			JpaQueryFactory queryFactory = entityRepository.getQueryFactory();
			Class entityClass = entityRepository.getEntityClass();
			JpaCriteriaQueryImpl query = (JpaCriteriaQueryImpl) queryFactory.query(entityClass);
			query.setPrivateData(new JpaRequestContext(entityRepository, querySpec));
			QuerySpec filteredQuerySpec = entityRepository.filterQuerySpec(querySpec);
			JpaRepositoryUtils.prepareQuery(query, filteredQuerySpec, Collections.emptySet());


			// create query
			JpaCriteriaQueryBackend backend = query.newBackend();
			CriteriaQuery criteriaQuery = backend.getCriteriaQuery();
			QueryBuilder executorFactory = new QueryBuilder(query, backend);
			executorFactory.applyFilterSpec();

			// perform grouping
			Expression expression = (Expression) executorFactory.getExpression(path);
			criteriaQuery.groupBy(expression);

			// perform selection
			CriteriaBuilder criteriaBuilder = backend.getCriteriaBuilder();
			Expression<Long> countExpr = criteriaBuilder.count(expression);
			criteriaQuery.multiselect(expression, countExpr);


			TypedQuery typedQuery = queryFactory.getEntityManager().createQuery(criteriaQuery);
			List<Object[]> resultList = typedQuery.getResultList();
			for (Object[] result : resultList) {
				Object value = result[0];
				Long count = (Long) result[1];
				String label = typeParser.toString(value);

				FacetValue facetValue = new FacetValue();
				facetValue.setValue(value);
				facetValue.setLabel(label);
				facetValue.setCount(count);
				facetValue.setFilterSpec(path.filter(FilterOperator.EQ, value));

				facetValueMap.put(value, facetValue);
			}
			return toList(facetValueMap);
		} else {
			throw new UnsupportedOperationException("unknown facet type: " + facetInformation);
		}

	}

	@Override
	public int getPriority() {
		return 500;
	}
}