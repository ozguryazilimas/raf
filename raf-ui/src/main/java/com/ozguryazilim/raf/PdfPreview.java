/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 *
 * @author oyas
 */
@RequestScoped
@Named
public class PdfPreview implements RafDocumentPreview{

    @Override
    public String getFragment() {
        return "/fragments/pdfPreview.xhtml";
    }
    
}
