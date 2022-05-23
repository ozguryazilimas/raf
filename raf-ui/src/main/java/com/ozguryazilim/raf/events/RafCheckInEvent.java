package com.ozguryazilim.raf.events;

import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafVersion;

/**
 *
 * @author oyas
 */
public class RafCheckInEvent {
    RafObject checkedInObject;
    RafVersion newVersion;

    public RafCheckInEvent(RafObject checkedInObject, RafVersion newVersion) {
        this.checkedInObject = checkedInObject;
        this.newVersion = newVersion;
    }

    public RafCheckInEvent(RafObject checkedInDoc) {
        this.checkedInObject = checkedInDoc;
    }

    public RafCheckInEvent() {
    }

    public RafObject getCheckedInObject() {
        return checkedInObject;
    }

    public void setCheckedInObject(RafObject checkedInObject) {
        this.checkedInObject = checkedInObject;
    }

    public RafVersion getNewVersion() {
        return newVersion;
    }

    public void setNewVersion(RafVersion newVersion) {
        this.newVersion = newVersion;
    }
}
