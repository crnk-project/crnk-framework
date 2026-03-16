package io.crnk.data.jpa.internal.query.backend.criteria;

import io.crnk.data.jpa.internal.query.AbstractQueryExecutorImpl;
import io.crnk.data.jpa.internal.query.QueryUtil;
import io.crnk.data.jpa.internal.query.backend.querydsl.ObjectArrayTupleImpl;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQueryExecutor;
import io.crnk.meta.model.MetaDataObject;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JpaCriteriaQueryExecutorImpl<T> extends AbstractQueryExecutorImpl<T> implements JpaCriteriaQueryExecutor<T> {

    private CriteriaQuery<T> query;

    public JpaCriteriaQueryExecutorImpl(EntityManager em, MetaDataObject meta, CriteriaQuery<T> criteriaQuery,
                                        int numAutoSelections, Map<String, Integer> selectionBindings) {
        super(em, meta, numAutoSelections, selectionBindings);

        this.query = criteriaQuery;
    }

    public CriteriaQuery<T> getQuery() {
        return query;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypedQuery<T> getTypedQuery() {
        return (TypedQuery<T>) setupQuery(em.createQuery(query));
    }

    @Override
    protected boolean isCompoundSelection() {
        return query.getSelection().isCompoundSelection();
    }

    @Override
    protected boolean isDistinct() {
        return query.isDistinct();
    }

    @Override
    protected boolean hasManyRootsFetchesOrJoins() {
        return QueryUtil.hasManyRootsFetchesOrJoins(query);
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public long getTotalRowCount() {
        Selection<T> selection = query.getSelection();
        List<Order> orderList = query.getOrderList();
        try {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            Expression<Long> countExpr;

            Set<Root<?>> roots = query.getRoots();
            if (roots.size() != 1) {
                throw new IllegalStateException("cannot compute totalRowCount in case of multiple query roots");
            }
            if (!query.getGroupList().isEmpty()) {
                throw new IllegalStateException("cannot compute totalRowCount for grouped queries");
            }

            // transform query to a count query
            Root root = roots.iterator().next();
            countExpr = isDistinct() ? builder.countDistinct(root) : builder.count(root);
            query.multiselect(countExpr);
            query.orderBy(new ArrayList<>());

            TypedQuery countQuery = em.createQuery(query);

            return (Long) countQuery.getSingleResult();
        } finally {
            // transform count query back to regular query
            query.multiselect(selection);
            query.orderBy(orderList);
        }
    }

    @Override
    public List<Tuple> getResultTuples() {
        List<?> results = executeQuery();
        List<Tuple> tuples = new ArrayList<>();
        for (Object result : results) {
            if (result instanceof Object[]) {
                tuples.add(new CriteriaTupleImpl((Object[]) result, selectionBindings));
            } else {
                tuples.add(new ObjectArrayTupleImpl(result, selectionBindings));
            }
        }
        return tuples;
    }
}
