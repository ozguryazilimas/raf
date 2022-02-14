package com.ozguryazilim.raf.generators;

import org.hibernate.Session;
import org.hibernate.tuple.ValueGenerator;

import java.util.UUID;

public class UUIDGenerator implements ValueGenerator<String> {
    @Override
    public String generateValue(Session session, Object o) {
        return UUID.randomUUID().toString();
    }
}
