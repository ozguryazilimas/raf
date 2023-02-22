package com.ozguryazilim.raf.events;

import com.ozguryazilim.raf.ui.base.AbstractAction;

/**
 *
 * @author oyas
 */
public class RafFolderChangeEvent {
    AbstractAction action;

    public RafFolderChangeEvent() {
    }
    public RafFolderChangeEvent(AbstractAction action) {
        this.action = action;
    }

    public AbstractAction getAction() {
        return action;
    }
}
