/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 *
 * @author boris.heithecker
 */
public class CollectionUtil {

    public static <T> Collector<T, ?, List<T>> nonNullList() {
        return Collectors.collectingAndThen(Collectors.toList(), l -> l.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    public static <T> Collector<T, ?, T> singleOrNull() {
        return Collectors.collectingAndThen(Collectors.toSet(), set -> set.size() == 1 ? set.iterator().next() : null);
    }

    public static <T> Collector<T, ?, T> requireSingleOrNull() {
        return Collectors.collectingAndThen(Collectors.toSet(), set -> {
            if (set.isEmpty()) {
                return null;
            } else if (set.size() == 1) {
                return set.iterator().next();
            } else {
                final String m = set.stream().map(Object::toString).collect(Collectors.joining(","));
                throw new IllegalStateException("Multiple elements in stream: " + m);
            }
        });
    }

    public static <T> Collector<T, ?, Optional<T>> singleton() {
        return Collectors.collectingAndThen(Collectors.toSet(), set -> set.size() == 1 ? Optional.of(set.iterator().next()) : Optional.empty());
    }

    public static <T> Collector<T, ?, Optional<T>> requireSingleton() {
        return Collectors.collectingAndThen(Collectors.toSet(), set -> {
            if (set.isEmpty()) {
                return Optional.empty();
            } else if (set.size() == 1) {
                return Optional.of(set.iterator().next());
            } else {
                throw new IllegalStateException();
            }
        });
    }

}
