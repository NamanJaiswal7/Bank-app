package com.bank.domain.specification;

/**
 * Specification pattern — composable business rules.
 *
 * @param <T> the type of object this specification evaluates
 */
public interface Specification<T> {

    boolean isSatisfiedBy(T candidate);

    Specification<T> and(Specification<T> other);

    Specification<T> or(Specification<T> other);

    Specification<T> not();
}
