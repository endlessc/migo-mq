package aaa;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Author  知秋
 * Created by Auser on 2017/2/24.
 */
public class test {
    public <V extends Ordered> List<V> myMethod(List<Thing<V>> things) {
        List<V> results = things.stream()
                                .filter(Thing::hasPropertyOne)
                                .map(Thing::getValuedProperty)
                                .filter(valued -> valued != null && valued.hasPropertyTwo())
                                .map(Valued::getValue)
                                .filter(Objects::nonNull)
                                .sorted((a, b) -> {
                                    return Integer.compare(a.getOrder(), b.getOrder());
                                })
                                .collect(Collectors.toList());
        return results;
    }
}
