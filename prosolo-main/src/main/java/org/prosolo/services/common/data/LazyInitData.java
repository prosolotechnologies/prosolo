package org.prosolo.services.common.data;

import java.util.List;

/**
 * Represents collection of specified type which is supposed to be lazily initialized with flag
 * that indicates if collection is already initialized or not and count which gives the total element count.
 * Note: count is not a number of elements in a collection, but a number of elements in total which is available
 * even before data is initialized.
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

    public long getCount() {
        return count;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public List<T> getData() {
        return data;
    }
}
