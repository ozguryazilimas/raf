package com.ozguryazilim.raf.jbpm.ui;

import java.io.Serializable;

/**
 * Task Result Action'ları için model sınıf.
 * 
 * APPROVE, REJECT v.b. için resgister edilecekler. Bu sayede arayüzde bilinen düğmeler için daha düzgün sunum sağlanacak.
 * 
 * Süreçten gelenler için sorun var. İsimlerini geldiği gibi gösterelim, Dolayısı ile fix olanları düzeltmek gerek
 * 
 * @author Hakan Uygun
 */
public class TaskAction implements Serializable{
    
    private String action;
    private String title;
    private String icon;
    private String style = "btn-default";

    public TaskAction(String action, String title, String icon, String style) {
        this.action = action;
        this.title = title;
        this.icon = icon;
        this.style = style;
    }

    public TaskAction(String action, String icon, String style) {
        this.action = action;
        this.icon = icon;
        this.style = style;
        this.title = action;
    }

    public TaskAction(String action, String icon) {
        this.action = action;
        this.icon = icon;
        this.title = action;
    }

    public TaskAction(String action) {
        this.action = action;
        this.title = action;
    }

    
    
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
    
    
    
}
