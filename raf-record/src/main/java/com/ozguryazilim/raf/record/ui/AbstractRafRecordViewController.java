/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.record.ui;

import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.ui.base.AbstractRafObjectViewController;

/**
 *
 * @author oyas
 */
public class AbstractRafRecordViewController extends AbstractRafObjectViewController<RafRecord>{
    
    @Override
    public String getViewId() {
        return "/fragments/recordView.xhtml";
    }
}
