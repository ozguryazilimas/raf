package com.ozguryazilim.raf.generators;

import org.hibernate.Session;
import org.hibernate.tuple.ValueGenerator;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * 6 haneli yalnızca rakamlardan oluşan basit parola üretir.
 */
public class SimplePasswordGenerator implements ValueGenerator<String> {

    private final Random rnd;

    public SimplePasswordGenerator() throws NoSuchAlgorithmException {
        rnd = SecureRandom.getInstanceStrong();
    }

    @Override
    public String generateValue(Session session, Object o) {
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }
}