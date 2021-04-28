package com.ozguryazilim.raf.index.manager;

import com.ozguryazilim.telve.messagebus.command.AbstractStorableCommand;

public class IndexRemoverCommand extends AbstractStorableCommand {

    private String willRemoveIndexes;

    public IndexRemoverCommand() {
    }

    public IndexRemoverCommand(String willRemoveIndexes) {
        this.willRemoveIndexes = willRemoveIndexes;
    }

    public String getWillRemoveIndexes() {
        return willRemoveIndexes;
    }

    public void setWillRemoveIndexes(String willRemoveIndexes) {
        this.willRemoveIndexes = willRemoveIndexes;
    }

}
