/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Image tipinde içerikler için preview widget controller.
 * 
 * mimeType'ı image/* olan tipler için kullanılır. Tarayıcıların sunum yeteneği ile çalışır.
 * 
 * FIXME: Kalite v.s. konusunda aslında bişiler yapılmalı. Belgenin tamamını clienta sırf preview için göndermek anlamlı değil.
 * 
 * @author Hakan Uygun
 */
@RequestScoped
@Named
public class ImagePreview implements RafDocumentPreview{

    @Inject
    private RafContext context;
    
    @Inject
    private RafService rafService;
    
    @Override
    public String getFragment() {
        return "/fragments/imagePreview.xhtml";
    }
    
    
    
    
    
}
