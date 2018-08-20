package org.prosolo.services.common.data;

import java.util.List;

/**
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
