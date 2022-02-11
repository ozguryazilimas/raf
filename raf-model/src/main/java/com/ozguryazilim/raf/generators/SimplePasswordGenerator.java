package com.ozguryazilim.raf.generators;

import org.hibernate.Session;
import org.hibernate.tuple.ValueGenerator;

import java.util.Random;

/**
 * 6 haneli yalnızca rakamlardan oluşan basit parola üretir.
 */
public class SimplePasswordGenerator implements ValueGenerator<String> {
    @Override
    public String generateValue(Session session, Object o) {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }
}
