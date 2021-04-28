package com.ozguryazilim.raf.imports.email;

import com.ozguryazilim.telve.messagebus.command.AbstractCommand;

/**
 * Tek bir e-posta taşır içerisinde.
 * <p>
 * Böylece importer kuyruktan çıkan her bir e-posta ile tek tek ilgilenir.
 *
 * @author Hakan Uygun
 */
public class EMailImportCommand extends AbstractCommand {

    private String eml;
    private String rafPath;
    private String jexlExp;

    public String getEml() {
        return eml;
    }

    public String getRafPath() {
        return rafPath;
    }

    public void setEml(String eml) {
        this.eml = eml;
    }

    public void setRafPath(String rafPath) {
        this.rafPath = rafPath;
    }

    public String getJexlExp() {
        return jexlExp;
    }

    public void setJexlExp(String jexlExp) {
        this.jexlExp = jexlExp;
    }

}
