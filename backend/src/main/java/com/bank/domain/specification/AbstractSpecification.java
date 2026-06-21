package com.bank.domain.specification;

/**
 * Abstract base providing and/or/not composition for specifications.
 */
public abstract class AbstractSpecification<T> implements Specification<T> {

    @Override
    public Specification<T> and(Specification<T> other) {
        return new AndSpecification<>(this, other);
    }

    @Override
    public Specification<T> or(Specification<T> other) {
        return new OrSpecification<>(this, other);
    }

    @Override
    public Specification<T> not() {
        return new NotSpecification<>(this);
    }

    private static class AndSpecification<T> extends AbstractSpecification<T> {
        private final Specification<T> left;
        private final Specification<T> right;

        AndSpecification(Specification<T> left, Specification<T> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean isSatisfiedBy(T candidate) {
            return left.isSatisfiedBy(candidate) && right.isSatisfiedBy(candidate);
        }
    }

    private static class OrSpecification<T> extends AbstractSpecification<T> {
        private final Specification<T> left;
        private final Specification<T> right;

        OrSpecification(Specification<T> left, Specification<T> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean isSatisfiedBy(T candidate) {
            return left.isSatisfiedBy(candidate) || right.isSatisfiedBy(candidate);
        }
    }

    private static class NotSpecification<T> extends AbstractSpecification<T> {
        private final Specification<T> spec;

        NotSpecification(Specification<T> spec) {
            this.spec = spec;
        }

        @Override
        public boolean isSatisfiedBy(T candidate) {
            return !spec.isSatisfiedBy(candidate);
        }
    }
}
