package io.crnk.core.mock.models;

public interface Identifiable<T extends Comparable<T>> {

    T getId();
}