package org.prosolo.common.util.map;

import java.io.Serializable;
import java.util.*;

/**
 * @author stefanvuckovic
 * @date 2017-09-26
 * @since 1.0.0
 */
public class CountByKeyMap<T> implements Serializable, Map<T, Integer> {

    private static final long serialVersionUID = -3131057610644356104L;

    private Map<T, Integer> map;

    public CountByKeyMap() {
        map = new HashMap<>();
    }

    @Override
    public Integer get(Object key) {
        return map.get(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer put(T t, Integer integer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends T, ? extends Integer> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<T> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Integer> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<T, Integer>> entrySet() {
        return map.entrySet();
    }

    public void put(T key) {
        Integer count = map.get(key);
        if (count == null) {
            count = 0;
        }
        map.put(key, ++count);
    }

}
