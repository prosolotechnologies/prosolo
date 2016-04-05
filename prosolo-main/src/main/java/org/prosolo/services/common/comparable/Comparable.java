package org.prosolo.services.common.comparable;

@FunctionalInterface
public interface Comparable<T> {
	public boolean equals(T t1, T t2);
}
