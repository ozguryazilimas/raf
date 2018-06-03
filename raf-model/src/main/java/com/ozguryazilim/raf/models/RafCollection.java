/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Raf sorgu sonuçlarının toparlandığı bir özel bir collection yapısı.
 * 
 * Arayüz bileşenleri bu model ile toplu işlem taşır.
 * 
 * @author Hakan Uygun
 */
public class RafCollection implements Serializable{
    
    private String mimeType;
    private String path;
    private String title;
    private String id;
    private String name;
    
    private List<RafObject> items = new ArrayList<>();

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RafObject> getItems() {
        return items;
    }

    public void setItems(List<RafObject> items) {
        this.items = items;
    }
    
}
