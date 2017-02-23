package aaa;

/**
 * Author  知秋
 * Created by Auser on 2017/2/24.
 */
public interface Thing <V extends Ordered> {
    boolean hasPropertyOne();
    Valued<V> getValuedProperty();
}
