package com.ozguryazilim.raf.model;

import java.util.List;

public class RafUserRoleDetails {
    private String rafName;
    private String role;
    private List<RafPathUserRoleDetails> rafPathUserRoleDetailsList;

    public RafUserRoleDetails(String rafName, String role, List<RafPathUserRoleDetails> rafPathUserRoleDetailsList) {
        this.rafName = rafName;
        this.role = role;
        this.rafPathUserRoleDetailsList = rafPathUserRoleDetailsList;
    }

    public RafUserRoleDetails() {
    }

    public String getRafName() {
        return rafName;
    }

    public void setRafName(String rafName) {
        this.rafName = rafName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<RafPathUserRoleDetails> getRafPathUserRoleDetailsList() {
        return rafPathUserRoleDetailsList;
    }

    public void setRafPathUserRoleDetailsList(List<RafPathUserRoleDetails> rafPathUserRoleDetailsList) {
        this.rafPathUserRoleDetailsList = rafPathUserRoleDetailsList;
    }
}
