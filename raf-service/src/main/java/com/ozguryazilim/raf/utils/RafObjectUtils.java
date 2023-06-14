package com.ozguryazilim.raf.utils;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RafObjectUtils {

    public static Predicate<RafObject> distinctRafObject() {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(t.getId(), Boolean.TRUE) == null;
    };

    /**
     * Checks if rafObject is record and has no document.
     * @param obj RafObject
     * @return returns true if record and empty
     */
    public static boolean isEmptyRecord(RafObject obj) {
        if (obj instanceof RafRecord) {
            RafRecord record = (RafRecord) obj;
            return record.getDocuments().isEmpty();
        }

        return false;
    }

}
