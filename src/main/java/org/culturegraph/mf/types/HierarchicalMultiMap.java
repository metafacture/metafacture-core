package org.culturegraph.mf.types;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public final class HierarchicalMultiMap<Key1, Key2, V> implements Iterable<Map.Entry<Key2, V>> {

  private final Map<Key1, Map<Key2, List<V>>> emitBuffer;
  private final Map<Key1, Map<Key2, List<V>>> valueBuffer;

  public HierarchicalMultiMap() {
    emitBuffer = new LinkedHashMap<>();
    valueBuffer = new LinkedHashMap<>();
  }

  public void addToEmit(final Key1 key1, final Key2 key2, final V value) {
    final Map<Key2, List<V>> emitMap = getFirstOrAddDefault(emitBuffer, key1);
    getSecondOrAddDefault(emitMap, key2).add(value);
  }

  public void addToValue(final Key1 key1, final Key2 key2, final V value) {
    final Map<Key2, List<V>> valueMap = getFirstOrAddDefault(valueBuffer, key1);
    getSecondOrAddDefault(valueMap, key2).add(value);
  }

  public void emitValues(final Key1 key) {
    final Map<Key2, List<V>> valueMap = getFirstOrAddDefault(valueBuffer, key);
    final Map<Key2, List<V>> emitMap = getFirstOrAddDefault(emitBuffer, key);

    for (final Map.Entry<Key2, List<V>> entry : valueMap.entrySet()) {
      getSecondOrAddDefault(emitMap, entry.getKey()).addAll(entry.getValue());
    }
  }

  public void removeValues(final Key1 key) {
    final Map<Key2, List<V>> valueMap = valueBuffer.get(key);
    if (valueMap == null) {
      return;
    }

    final Map<Key2, List<V>> emitMap = emitBuffer.get(key);
    if (emitMap == null) {
      return;
    }

    for (final Map.Entry<Key2, List<V>> entry : emitMap.entrySet()) {
      final List<V> values = valueMap.get(entry.getKey());
      if (values != null) {
        entry.getValue().removeAll(values);
      }
    }
  }

  @Override
  public Iterator<Map.Entry<Key2, V>> iterator() {
    return new Iterator<Map.Entry<Key2, V>>() {

      private final Iterator<Map<Key2, List<V>>> emitIterator =
          emitBuffer.values().iterator();

      private Key2 currentKey2;
      private Iterator<Map.Entry<Key2, List<V>>> currentMap;
      private Iterator<V> currentList;

      private Map.Entry<Key2, V> prefetched;

      @Override
      public boolean hasNext() {
        if (prefetched == null) {
          prefetched = computeNext();
        }
        return prefetched != null;
      }

      @Override
      public Map.Entry<Key2, V> next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        final Map.Entry<Key2, V> entry = prefetched;
        prefetched = null;
        return entry;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

      private Map.Entry<Key2, V> computeNext() {
        // We're at a second level iteration, return next value
        if (currentList != null) {
          if (currentList.hasNext()) {
            return make(currentList.next());
          } else {
            // avoid hasNext checks on subsequent iterations
            currentList = null;
          }
        }

        // We're at a first level iteration, start iteration over the next 2nd level
        if (currentMap != null) {
          if (currentMap.hasNext()) {
            final Map.Entry<Key2, List<V>> next = currentMap.next();
            currentKey2 = next.getKey();
            currentList = next.getValue().iterator();
            return computeNext();
          } else {
            // avoid hasNext checks on subsequent iterations
            currentMap = null;
          }
        }

        // We're at a root level iteration, iterate over the next 1st level
        if (emitIterator.hasNext()) {
          currentMap = emitIterator.next().entrySet().iterator();
          return computeNext();
        }

        // There is nothing left to iterator, null everything for good measure.
        currentList = null;
        currentMap = null;
        return null;
      }

      public Map.Entry<Key2, V> make(final V value) {
        return new AbstractMap.SimpleEntry<>(currentKey2, value);
      }
    };
  }

  public boolean hasEmits() {
    return !emitBuffer.isEmpty();
  }

  public boolean hasValues() {
    return !valueBuffer.isEmpty();
  }

  public void clear() {
    clearEmits();
    clearValues();
  }

  public void clearEmits() {
    emitBuffer.clear();
  }

  public void clearValues() {
    valueBuffer.clear();
  }

  protected Map<Key2, List<V>> defaultFirstLevel() {
    return new LinkedHashMap<>();
  }

  protected List<V> defaultSecondLevel() {
    return new ArrayList<>();
  }

  private Map<Key2, List<V>> getFirstOrAddDefault(final Map<Key1, Map<Key2, List<V>>> buffer, final Key1 key) {
    Map<Key2, List<V>> result;
    if ((result = buffer.get(key)) == null) {
      result = defaultFirstLevel();
      buffer.put(key, result);
    }
    return result;
  }

  private List<V> getSecondOrAddDefault(final Map<Key2, List<V>> map, final Key2 key) {
    List<V> result;
    if ((result = map.get(key)) == null) {
      result = defaultSecondLevel();
      map.put(key, result);
    }
    return result;
  }
}
