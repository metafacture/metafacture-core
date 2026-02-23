package org.metafacture.solr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HashMapList<K,V> {
    private Map<K, List<V>> hashList;

    public HashMapList() {
        hashList = new HashMap<>();
    }

    public void add(K key, V value) {
        if (!hashList.containsKey(key)) {
            hashList.put(key, new ArrayList<>());
        }

        List<V> list = hashList.get(key);
        list.add(value);
    }

    public List<V> get(K key) {
        return hashList.get(key);
    }

    public Map<K,List<V>> asMap() {
        return hashList;
    }

    public boolean containsKey(K key) {
        return hashList.containsKey(key);
    }

    public boolean isEmpty() {
        return hashList.isEmpty();
    }

    @Override
    public String toString() {
        return "HashMapList{" +
                "hashList=" + hashList +
                '}';
    }
}
