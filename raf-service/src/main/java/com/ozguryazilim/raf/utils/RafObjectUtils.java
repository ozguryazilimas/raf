package com.ozguryazilim.raf.utils;

import com.ozguryazilim.raf.models.RafObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class RafObjectUtils {
    public static Predicate<RafObject> distinctRafObject() {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(t.getId(), Boolean.TRUE) == null;
    };

}
