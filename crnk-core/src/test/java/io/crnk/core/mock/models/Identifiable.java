package io.crnk.core.mock.models;

import java.io.Serializable;

public interface Identifiable<T extends Serializable & Comparable<T>> {

	T getId();
}