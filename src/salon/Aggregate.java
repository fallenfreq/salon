package salon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;

class Result<V, R> {
  private R accumulator;
  private ArrayList<V> values = new ArrayList<>();

  Result(R startValue) {
    this.accumulator = startValue;
  }

  public R getAccumulator() {
    return accumulator;
  }

  public V getValue(Integer i) {
    return values.get(i);
  }

  public ArrayList<V> getValues() {
    return values;
  }

  public boolean addValue(V value) {
    return values.add(value);
  }

  public void setAccumulator(R accumulator) {
    this.accumulator = accumulator;
  }

  @Override
  public String toString() {
    return "Result( " + accumulator + " )";
  }
}


class Aggregate<GK extends Comparable<? super GK>, V, R> extends AbstarctTree<GK, Result<V, R>> {
  private R startValue;
  private BiFunction<V, Integer, GK> getGroupByKey;
  private TriFunction<R, V, Aggregate<GK, V, R>, R> action;

  Aggregate(
    R startValue,
    BiFunction<V, Integer, GK> getGroupByKey,
    TriFunction<R, V, Aggregate<GK, V, R>, R> action) {
    super(new HashMap<GK, Result<V, R>>());
    this.getGroupByKey = getGroupByKey;
    this.action = action;
    this.startValue = startValue;
  }

  public GK getGroupByKey(V value) {
    return getGroupByKey.apply(value, null);
  }

  public TriFunction<R, V, Aggregate<GK, V, R>, R> getAction() {
    return action;
  }

  public Result<V, R> get(GK key) {
    Result<V, R> result = getPrimaryStore().get(key);
    return result;
  }

  public Result<V, R> get(V value) {
    return get(value, null);
  }

  public Result<V, R> get(V value, Integer i) {
    GK key = getGroupByKey.apply(value, i);
    Result<V, R> result = getPrimaryStore().get(key);
    return result;
  }

  public Result<V, R> put(V value) {
    return put(value, null);
  }

  public Result<V, R> put(V value, Integer i) {
    GK key = getGroupByKey.apply(value, i);
    Result<V, R> result = getPrimaryStore().getOrDefault(key, new Result<V, R>(startValue));
    return put(key, value, action.apply(result.getAccumulator(), value, this));
  }

  public Result<V, R> put(V value, Integer i, R accValue) {
    GK key = getGroupByKey.apply(value, i);
    return put(key, value, accValue);
  }

  public Result<V, R> put(GK key, V value, R accValue) {
    Result<V, R> result = getPrimaryStore().getOrDefault(key, new Result<V, R>(startValue));
    result.setAccumulator(accValue);
    result.addValue(value);
    return getPrimaryStore().put(key, result);
  }
}
