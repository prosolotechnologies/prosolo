package org.prosolo.services.common.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-11-21
 * @since 1.2.0
 */
public class SortOrder<T extends Enum<T>> {

    private final List<SimpleSortOrder<T>> sortOrders;

    private SortOrder(List<SimpleSortOrder<T>> sortOrders) {
        this.sortOrders = Collections.unmodifiableList(sortOrders);
    }

    public static <T extends Enum<T>> Builder<T> builder() {
        return new Builder<>();
    }

    public List<SimpleSortOrder<T>> getSortOrders() {
        return sortOrders;
    }

    public boolean isSortPresent() {
        return !sortOrders.isEmpty();
    }

    public static class Builder<T extends Enum<T>> {

        private List<SimpleSortOrder<T>> sortOrders = new ArrayList<>();

        public Builder addOrder(T sortField, SortingOption sortOption) {
            this.sortOrders.add(new SimpleSortOrder<>(sortField, sortOption));
            return this;
        }

        public SortOrder<T> build() {
            return new SortOrder<>(this.sortOrders);
        }
    }

    public static class SimpleSortOrder<T extends Enum<T>> {

        private T sortField;
        private SortingOption sortOption;

        public SimpleSortOrder(T sortField, SortingOption sortOption) {
            this.sortField = sortField;
            this.sortOption = sortOption;
        }

        public T getSortField() {
            return sortField;
        }

        public SortingOption getSortOption() {
            return sortOption;
        }

    }
}
