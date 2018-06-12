/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import java.io.Serializable;

/**
 *
 * @author oyas
 */
public class SequencerConfig implements Serializable {

    private String name;
    private String classname;
    private String expression;

    public SequencerConfig(String name, String classname, String expression) {
        this.name = name;
        this.classname = classname;
        this.expression = expression;
    }

    public String getName() {
        return name;
    }

    public String getClassname() {
        return classname;
    }

    public String getExpression() {
        return expression;
    }
}
