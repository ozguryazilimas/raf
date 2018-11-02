/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.record;

import com.ozguryazilim.raf.auth.RafAsset;
import com.ozguryazilim.raf.record.model.RafRecordType;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sistemde tanımlı olan RecordType tanımlarını ve bu tanımla ilişkili nesleri
 * yönetir.
 *
 *
 * FIXME: yüklenen paketin aktif olup olmadığına bakılcak! Bunun için bir yapı
 * gerekli!
 *
 * @author Hakan Uygun
 */
@ApplicationScoped
public class RecordTypeManager implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RecordTypeManager.class);

    private Map<String, RafRecordType> typeMap = new HashMap<>();
    private Map<String, List<RafRecordType>> kjarTypeMap = new HashMap<>();

    @PostConstruct
    public void init() {

    }

    /**
     * Sisteme yeni RecordType'lar register eder.
     *
     * @param recordTypes
     */
    public synchronized void register(String kjarId, List<RafRecordType> recordTypes) {
        //FIXME: çakışma durumları kontrol edilmeli.

        for (RafRecordType r : recordTypes) {
            typeMap.put(r.getName(), r);
            LOG.info("RecordType Registered : {}", r.getName());
            LOG.debug("RecordType Registered : {}", r);

        }
        
        List<RafRecordType> rl = kjarTypeMap.get(kjarId);
        if (rl == null) {
            kjarTypeMap.put(kjarId, recordTypes);
        } else {
            rl.addAll(recordTypes);
        }

    }

    public void deploy(String kjarId, InputStream is) {
        List<RafRecordType> recordTypes = RecordTypeXmlParser.parse(is);
        register(kjarId, recordTypes);
    }

    public void undeploy(String kjarId) {
        List<RafRecordType> rl = kjarTypeMap.get(kjarId);
        if (rl != null) {

            for (RafRecordType r : rl) {
                typeMap.remove(r.getName());
            }

            kjarTypeMap.remove(kjarId);
        }
    }

    public List<RafRecordType> getRecordTypes() {
        return new ArrayList(typeMap.values());
    }

    public RafRecordType getRecordType(String name) {
        return typeMap.get(name);
    }

    public List<RafAsset> getAssests(String kjarId) {
        List<RafAsset> result = new ArrayList<>();

        List<RafRecordType> rl = kjarTypeMap.get(kjarId);
        if (rl != null) {
            for (RafRecordType r : rl) {
                result.add(new RafAsset(r.getName(), r.getTitle(), "RECORD_TYPE"));
            }
        }

        return result;
    }

}
