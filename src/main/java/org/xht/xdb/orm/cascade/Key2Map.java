package org.xht.xdb.orm.cascade;

import java.util.HashMap;

public class Key2Map<K1, K2, V> {
    private HashMap<K1, HashMap<K2, V>> map = new HashMap<>();

    public Key2Map<K1, K2, V> put(K1 k1, K2 k2, V v) {
        map.computeIfAbsent(k1, k -> new HashMap<>()).put(k2, v);
        return this;
    }

    public V get(K1 k1, K2 k2) {
        HashMap<K2, V> iv = map.get(k1);
        if (iv == null)
            return null;
        return iv.get(k2);
    }

}
