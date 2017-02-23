package aaa;

/**
 * Author  知秋
 * Created by Auser on 2017/2/24.
 */
public interface Valued <V extends Ordered> {
    boolean hasPropertyTwo();
    V getValue();
}
