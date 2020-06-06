/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig;

/**
 *
 * @author boris
 */
public abstract class AppResourcesProperty {

    protected enum UpdateStatus {
        MODIFIED, TEMPLATE, REMOVAL
    };
    private final String key;
    protected final String value;
    protected String valueOverride;
    protected UpdateStatus status;

    public AppResourcesProperty(final String key, final String value) {
        this.key = key;
        this.value = value;
        this.valueOverride = this.value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return valueOverride;
    }

    public boolean setKey(final String key) {
        throw new UnsupportedOperationException("Keys are fixed.");
    }

    public abstract boolean setValue(String value);

    public boolean isModified() {
        return UpdateStatus.MODIFIED.equals(this.status);
    }

    public boolean isTemplate() {
        return UpdateStatus.TEMPLATE.equals(this.status);
    }

    public boolean isForRemoval() {
        return UpdateStatus.REMOVAL.equals(this.status);
    }

    protected void setStatus(final UpdateStatus status) {
        this.status = status;
    }

}
