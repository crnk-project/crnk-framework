package io.crnk.data.jpa.internal.facet;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;

import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.repository.decorate.Wrapper;
import io.crnk.core.utils.Prioritizable;
import io.crnk.data.facet.FacetValue;
import io.crnk.data.facet.config.BasicFacetInformation;
import io.crnk.data.facet.config.FacetInformation;
import io.crnk.data.facet.provider.FacetProviderBase;
import io.crnk.data.jpa.JpaEntityRepositoryBase;
import io.crnk.data.jpa.internal.JpaRepositoryUtils;
import io.crnk.data.jpa.internal.JpaRequestContext;
import io.crnk.data.jpa.internal.query.QueryBuilder;
import io.crnk.data.jpa.internal.query.backend.criteria.JpaCriteriaQueryBackend;
import io.crnk.data.jpa.internal.query.backend.criteria.JpaCriteriaQueryImpl;
import io.crnk.data.jpa.query.JpaQueryFactory;

public class JpaFacetProvider extends FacetProviderBase implements Prioritizable {


	@Override
	public boolean accepts(RegistryEntry entry) {
		Object resourceRepository = entry.getResourceRepository().getImplementation();
		return unwrap(resourceRepository) instanceof JpaEntityRepositoryBase;
	}

	private Object unwrap(Object resourceRepository) {
		// TODO ResourceRepositoryDecorator support
		while (resourceRepository instanceof Wrapper) {
			resourceRepository = ((Wrapper) resourceRepository).getWrappedObject();
		}
		return resourceRepository;
	}

	@Override
	public List<FacetValue> findValues(FacetInformation facetInformation, QuerySpec querySpec) {
		if (facetInformation instanceof BasicFacetInformation) {
			String resourceType = facetInformation.getResource().getResourceType();
			ResourceRepository repository = (ResourceRepository) context.getEntry(resourceType).getResourceRepository().getImplementation();
			JpaEntityRepositoryBase entityRepository = (JpaEntityRepositoryBase) unwrap(repository);
			BasicFacetInformation basicFacetInformation = (BasicFacetInformation) facetInformation;

			PathSpec path = basicFacetInformation.getPath();

			TypeParser typeParser = context.getTypeParser();
			Map<Object, FacetValue> facetValueMap = new HashMap<>();

			// setup query
			JpaQueryFactory queryFactory = entityRepository.getQueryFactory();
			Class entityClass = entityRepository.getEntityClass();
			JpaCriteriaQueryImpl query = (JpaCriteriaQueryImpl) queryFactory.query(entityClass);
			query.setPrivateData(new JpaRequestContext(entityRepository, querySpec));
			QuerySpec filteredQuerySpec = JpaRepositoryUtils.filterQuerySpec(entityRepository.getRepositoryConfig(), entityRepository, querySpec);
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
		}
		else {
			throw new UnsupportedOperationException("unknown facet type: " + facetInformation);
		}

	}

	@Override
	public int getPriority() {
		return 500;
	}
}