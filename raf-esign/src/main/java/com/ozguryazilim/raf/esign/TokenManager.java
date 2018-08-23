/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.esign;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author oyas
 */
public class TokenManager {

    private static final long TIMEOUT = 5;

    private Cache<String, Object> tokens;

    private static TokenManager instance;

    protected TokenManager() {
        tokens = CacheBuilder.newBuilder().expireAfterWrite(TIMEOUT, TimeUnit.MINUTES).build();
    }

    /**
     * Geriye auth için kullanılacak olan token döner.
     *
     * @param data token ile birlikte geri alınacak olan oturum bilgisi.
     * @return
     */
    public String getToken(Object data) {
        String token = UUID.randomUUID().toString();
        //FIXME: Buraya değer olarak Idendity konmalı.
        tokens.put(token, data);
        return token;
    }

    /**
     * Geriye auth için kullanılacak olan token döner.
     *
     * @param token kullanılacak olan token
     * @param data token ile birlikte geri alınacak olan oturum bilgisi.
     * @return
     */
    public String getToken(String token, Object data) {
        tokens.put(token, data);
        return token;
    }

    /**
     * Verilen token'a ait bir değer var mı kontrolü yapılır. Eğer varsa true
     * aksi halde false döner.
     *
     * @param token
     * @return
     */
    public Boolean isTokenValid(String token) {
        return tokens.getIfPresent(token) != null;
    }

    /**
     * Geriye verilen token'a atanmış değeri döndürür.
     *
     * Eğer token invalid ise ya da değer yoksa null döner.
     *
     * @param token
     * @return
     */
    public Object getTokenData(String token) {
        return tokens.getIfPresent(token);
    }

    /**
     * Verilen token'ı invalidate eder.
     *
     * @param token
     */
    public void invalidateToken(String token) {
        tokens.invalidate(token);
    }

    /**
     * Geriye Singleton TokenManager döndürür.
     *
     * @return
     */
    public static TokenManager instance() {

        if (instance == null) {
            instance = new TokenManager();
        }

        return instance;
    }

}
