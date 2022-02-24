package com.ozguryazilim.raf.command.checkmissingcontentscommand;

import com.ozguryazilim.telve.messagebus.command.AbstractStorableCommand;

public class CheckMissingContentsCommand extends AbstractStorableCommand {
    private String email;

    public CheckMissingContentsCommand() { }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
