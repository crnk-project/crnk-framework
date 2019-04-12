package io.crnk.core.repository;

import java.io.Serializable;

public abstract class ReadOnlyResourceRepositoryBase<T, I > extends ResourceRepositoryBase<T, I> {

	protected ReadOnlyResourceRepositoryBase(Class<T> resourceClass) {
		super(resourceClass);
	}

	@Override
	public final <S extends T> S save(S resource) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final <S extends T> S create(S resource) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void delete(I id) {
		throw new UnsupportedOperationException();
	}
}
