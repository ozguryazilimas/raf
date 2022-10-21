package com.ozguryazilim.raf.command.reindexmanagercommand;

public class EsIndexStatus {
    private String name;
    private boolean enabled;
    private long count;

    public EsIndexStatus(String name, boolean enabled, long count) {
        this.name = name;
        this.enabled = enabled;
        this.count = count;
    }

    public EsIndexStatus() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
