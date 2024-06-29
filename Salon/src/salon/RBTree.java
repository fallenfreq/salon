package salon;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Custom tree map to index and sort map entries based on extracted keys.
 *
 * @param <MK> the type of original map keys
 * @param <K> the type of extracted and sorted keys
 * @param <S> the type of values in the map
 */
class IndexTree<PK extends Comparable<? super PK>, IK extends Comparable<? super IK>, S>
    extends TreeMap<String, S> {
  private static final long serialVersionUID = 1L;
  private final Function<Map.Entry<PK, S>, String> keyExtractor;
  // map of index key to primary key
  // Requires the index value to also be unique
  private Map<String, PK> keyMap = new HashMap<>();

  /**
   * Constructs an IndexTree with a key extractor and a comparator.
   *
   * @param keyExtractor function to extract keys from map entries
   * @param comparator comparator to determine the order of the keys
   */
  public IndexTree(
      Function<Map.Entry<PK, S>, IK> keyExtractor, Comparator<? super String> comparator) {
    super(comparator);
    // the key is the primary key so usually a unique id
    this.keyExtractor = map -> keyExtractor.apply(map) + "-" + map.getKey();
  }

  /**
   * Constructs an IndexTree with a key extractor and natural ordering.
   *
   * @param keyExtractor function to extract keys from map entries
   */
  public IndexTree(Function<Map.Entry<PK, S>, IK> keyExtractor) {
    this(keyExtractor, null);
  }

  /**
   * Gets the key extractor function.
   *
   * @return the key extractor function
   */
  public Function<Map.Entry<PK, S>, String> getKeyExtractor() {
    return keyExtractor;
  }

  public PK getPrimaryKey(IK indexKey) {
    return keyMap.get(indexKey);
  }

  public PK setPrimaryKey(String indexKey, PK primaryKey) {
    return keyMap.put(indexKey, primaryKey);
  }
}

class RBTree<K extends Comparable<? super K>, V extends HasPrimaryKey<K>>
    extends AbstarctTree<K, V> {
  private Map<String, IndexTree<?, ?, V>> indexes = new HashMap<>();

  RBTree() {
    super(new TreeMap<>());
  }

  public V get(K key) {
    return this.getPrimaryStore().get(key);
  }

  // I considered having a get that lets you get an index and pass the id but its pointless because
  // you can just get it from primaryTree. The indexes with non unique keys are just good for
  // listing things in order rather than for lookups
  public <PK extends Comparable<? super PK>, IK extends Comparable<? super IK>> V get(
      IK key, String indexName) {
    IndexTree<PK, IK, V> index = this.getIndex(indexName);
    return index.get(key + "-" + index.getPrimaryKey(key));
  }

  public V add(V value) {
    K key = value.getPrimaryKey();
    V oldValue = this.getPrimaryStore().put(key, value);
    for (String indexName : indexes.keySet()) {
      updateIndex(
          indexName,
          new AbstractMap.SimpleEntry<K, V>(key, oldValue),
          new AbstractMap.SimpleEntry<K, V>(key, value));
    }
    return oldValue;
  }

  @SuppressWarnings("unchecked")
  private <PK extends Comparable<? super PK>, IK extends Comparable<? super IK>> void updateIndex(
      String indexName, Map.Entry<PK, V> oldValueEntry, Map.Entry<PK, V> newValueEntry) {
    IndexTree<PK, IK, V> index = (IndexTree<PK, IK, V>) indexes.get(indexName);
    Function<Map.Entry<PK, V>, String> keyExtractor = index.getKeyExtractor();
    String indexKey = keyExtractor.apply(newValueEntry);
    // The calculated key should not change so index.remove is not really doing anything right now
    // I do have the primary key after the index key to make it unique but thats kind of pointless
    // right now
    if (oldValueEntry.getValue() != null) {
      index.remove(keyExtractor.apply(oldValueEntry));
    } else { // this is the first entry so set the key map
      index.setPrimaryKey(indexKey.replaceAll("-\\d+$", ""), newValueEntry.getKey());
    }
    index.put(indexKey, newValueEntry.getValue());
  }

  @SuppressWarnings("unchecked")
  public <MK extends Comparable<? super MK>, KK extends Comparable<? super KK>>
      IndexTree<MK, KK, V> saveIndex(IndexTree<MK, KK, V> index, String indexName) {
    return (IndexTree<MK, KK, V>) indexes.put(indexName, index);
  }

  // Get index by indexName
  @SuppressWarnings("unchecked")
  public <MK extends Comparable<? super MK>, KK extends Comparable<? super KK>>
      IndexTree<MK, KK, V> getIndex(String indexName) {
    return (IndexTree<MK, KK, V>) indexes.get(indexName);
  }

  public RBTree<K, V> filter(Predicate<V> condition) {
    RBTree<K, V> filteredMap = new RBTree<>();
    for (V value : this.getPrimaryStore().values()) {
      if (condition.test(value)) {
        filteredMap.add(value);
      }
    }
    return filteredMap;
  }
}
