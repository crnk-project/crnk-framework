package io.crnk.data.jpa.internal.query.backend.querydsl;

import com.querydsl.core.JoinExpression;
import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;

import jakarta.persistence.criteria.JoinType;
import java.lang.reflect.Field;
import java.util.List;

class QuerydslUtils {

	private QuerydslUtils() {
	}

	@SuppressWarnings("unchecked")
	public static <T> EntityPath<T> getEntityPath(Class<T> entityClass) {
		Class<?> queryClass = getQueryClass(entityClass);
		try {
			String fieldName = firstToLower(entityClass.getSimpleName());
			Field field = queryClass.getField(fieldName);
			return (EntityPath<T>) field.get(entityClass);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalStateException("failed to access query class " + queryClass.getName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> Expression<T> get(Expression<?> path, String name) {
		try {
			Class<?> clazz = path.getClass();
			Field field = clazz.getField(name);
			return (Expression<T>) field.get(path);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalStateException("failed get field " + path + "." + name, e);
		}
	}

	public static com.querydsl.core.JoinType convertJoinType(JoinType joinType) {
		switch (joinType) {
			case INNER:
				return com.querydsl.core.JoinType.JOIN;
			case LEFT:
				return com.querydsl.core.JoinType.LEFTJOIN;
			case RIGHT:
				return com.querydsl.core.JoinType.RIGHTJOIN;
			default:
				throw new IllegalStateException(joinType.toString() + " unknown");
		}
	}

	private static String firstToLower(String name) {
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	public static boolean hasManyRootsFetchesOrJoins(JPAQuery<?> query) {
		List<JoinExpression> joins = query.getMetadata().getJoins();
		for (JoinExpression join : joins) {
			if (join.getTarget() instanceof CollectionExpression) {
				return true;
			}
		}
		return false;
	}

	public static Class<?> getQueryClass(Class<?> entityClass) {
		String queryClassName = entityClass.getPackage().getName() + ".Q" + entityClass.getSimpleName();
		try {
			return entityClass.getClassLoader().loadClass(queryClassName);
		} catch (ClassNotFoundException | SecurityException | IllegalArgumentException e) {
			throw new IllegalStateException("unable to find query class " + queryClassName, e);
		}
	}
}
