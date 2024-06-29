package salon;

import java.util.Comparator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

abstract class AbstarctTree<K extends Comparable<? super K>, S> {
  private Map<K, S> primaryStore;

  AbstarctTree(Map<K, S> primaryStore) {
    this.primaryStore = primaryStore;
  }

  public Map<K, S> getPrimaryStore() {
    return primaryStore;
  }

  public <KK extends Comparable<? super KK>> IndexTree<K, KK, S> sort(
      Function<Map.Entry<K, S>, KK> keyExtractor) {
    return MapUtilities.sort(primaryStore, keyExtractor, Comparator.naturalOrder());
  }

  public <KK extends Comparable<? super KK>> IndexTree<K, KK, S> sort(
      Function<Map.Entry<K, S>, KK> keyExtractor, Comparator<? super String> comparator) {
    return MapUtilities.sort(primaryStore, keyExtractor, comparator);
  }

  public <GK extends Comparable<? super GK>, R> Aggregate<GK, S, R> aggregate(
      R startValue, BiFunction<S, Integer, GK> getGroupByKey, BiFunction<R, S, R> action) {
    return MapUtilities.aggregate(
        primaryStore, startValue, getGroupByKey, (a, b, c) -> action.apply(a, b));
  }

  public <GK extends Comparable<? super GK>, R> Aggregate<GK, S, R> aggregate(
      R startValue,
      BiFunction<S, Integer, GK> getGroupByKey,
      TriFunction<R, S, Aggregate<GK, S, R>, R> action) {
    return MapUtilities.aggregate(primaryStore, startValue, getGroupByKey, action);
  }

  @Override
  public String toString() {
    // Print each entry
    String result = "Data [\n";
    for (Map.Entry<K, S> entry : primaryStore.entrySet()) {
      K key = entry.getKey();
      S value = entry.getValue();
      result += key + ": " + value + " ";
    }
    return result + "\n ]";
  }
}
