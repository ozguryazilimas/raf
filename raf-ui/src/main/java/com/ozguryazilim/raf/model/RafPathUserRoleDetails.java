package com.ozguryazilim.raf.model;

public class RafPathUserRoleDetails {
    private String rafPath;
    private String role;

    public RafPathUserRoleDetails(String rafPath, String role) {
        this.rafPath = rafPath;
        this.role = role;
    }

    public RafPathUserRoleDetails() {
    }

    public String getRafPath() {
        return rafPath;
    }

    public void setRafPath(String rafPath) {
        this.rafPath = rafPath;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
