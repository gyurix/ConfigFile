package gyurix.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DualMap<K, V> implements Map<K, V> {
    final HashMap<K, V> keys = new HashMap();
    final HashMap<V, K> values = new HashMap();

    public int size() {
        return this.keys.size();
    }

    public boolean isEmpty() {
        return this.keys.isEmpty();
    }

    public boolean containsKey(Object key) {
        return this.keys.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.values.containsKey(value);
    }

    public V get(Object key) {
        return this.keys.get(key);
    }

    public K getKey(Object value) {
        return this.values.get(value);
    }

    public V put(K key, V value) {
        this.keys.remove(this.values.get(value));
        V o = this.keys.put(key, value);
        this.values.put(value, key);
        return o;
    }

    public V remove(Object key) {
        V o = this.keys.remove(key);
        this.values.remove(o);
        return o;
    }

    public K removeValue(Object value) {
        K key = this.values.remove(value);
        this.keys.remove(key);
        return key;
    }

    public void putAll(Map m) {
        this.keys.putAll(m);
        putAllValue(m);
    }

    private void putAllValue(Map<K, V> m) {
        for (Map.Entry<K, V> e : m.entrySet()) {
            this.values.put(e.getValue(), e.getKey());
        }
    }


    public void clear() {
        this.keys.clear();
        this.values.clear();
    }

    public Set<K> keySet() {
        return this.keys.keySet();
    }
    public Collection<V> values() {
        return this.values.keySet();
    }
    public Set<Map.Entry<K, V>> entrySet() {
        return this.keys.entrySet();
    }
}



/* Location:           D:\Szerverek\SpaceCraft\plugins\ConfLangLib.jar

 * Qualified Name:     DualMap

 * JD-Core Version:    0.7.0.1

 */