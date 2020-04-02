/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.model;

import java.beans.PropertyVetoException;
import java.text.Collator;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.xmlimport.ImportItem;

public abstract class ImportSigneeItem extends ImportItem {

    private final static Collator COLLATOR = Collator.getInstance(Locale.getDefault());
    public static final String PROP_SELECTED = "selected";
    public static final String PROP_SIGNEE = "signee";
    public static final String PROP_SOURCE_STATUS = "source.status";
    public static final String PROP_STATUS = "status";
    protected Marker status;
    protected String sourceStatus;
    private boolean selected;

    public ImportSigneeItem(String name) {
        super(name);
    }

    public void setSourceStatus(String sourceStatus) throws PropertyVetoException {
        String old = this.sourceStatus;
        this.sourceStatus = sourceStatus;
        try {
            vSupport.fireVetoableChange(PROP_SOURCE_STATUS, old, this.sourceStatus);
        } catch (PropertyVetoException vex) {
            this.sourceStatus = old;
            throw vex;
        }
    }

    public String getName() {
        return getSourceNodeLabel();
    }

    public abstract Signee getSignee();

    public String getSourceStatus() {
        return sourceStatus;
    }

    public Marker getStatus() {
        return status;
    }

    public abstract Marker[] getMarkers();

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) throws PropertyVetoException {
        boolean old = this.selected;
        this.selected = selected;
        try {
            vSupport.fireVetoableChange(PROP_SELECTED, old, this.selected);
        } catch (PropertyVetoException vex) {
            this.selected = old;
            throw vex;
        }
    }

    @Override
    public boolean isFragment() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public int compareTo(ImportSigneeItem o) {
        return COLLATOR.compare(StringUtils.trimToEmpty(getName()), StringUtils.trimToEmpty(o.getName()));
    }

}
