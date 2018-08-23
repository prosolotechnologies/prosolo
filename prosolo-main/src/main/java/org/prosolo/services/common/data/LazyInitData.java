package org.prosolo.services.common.data;

import java.util.List;

/**
 * Represents a collection of a specified type that is supposed to be lazily initialized.
 *
 * @author stefanvuckovic
 * @date 2018-08-16
 * @since 1.2.0
 */
public class LazyInitData<T> {

    private long count;
    private boolean initialized;
    private List<T> data;

    public LazyInitData(long count) {
        this.count = count;
    }

    public void init(List<T> data) {
        this.data = data;
        initialized = true;
    }

    /**
     * Returns the total element count. Note that this number does not represent a number of elements in the collection,
     * but the total number of elements.
     *
     * @return
     */
    public long getCount() {
        return count;
    }

    /**
     * The flag that indicates whether the collection is already initialized or not.
     *
     * @return true or false
     */
    public boolean isInitialized() {
        return initialized;
    }

    public List<T> getData() {
        return data;
    }
}
