package de.bitshares_munich.utils;

import java.util.HashMap;

/**
 * A custom Map implementation that will return a default value
 * specified in the constructor.
 *
 * Created by nelson on 12/24/16.
 */
public class DefaultHashMap<K,V> extends HashMap<K,V> {
    protected V defaultValue;
    public DefaultHashMap(V defaultValue) {
        this.defaultValue = defaultValue;
    }
    @Override
    public V get(Object k) {
        return containsKey(k) ? super.get(k) : defaultValue;
    }
}