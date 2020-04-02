/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.UniqueMarkerSet;

/**
 *
 * @author boris.heithecker
 */
public abstract class ImportItem {

    public static final String PROP_DELETE_DATE = "delete-date";
    public static final String PROP_IMPORT_TARGET = "import-target";
    protected String sourceNode;
    protected final UniqueMarkerSet uniqueMarkers = new UniqueMarkerSet();
    private LocalDate deleteDate;
    private final Map<String, Object> userProperties = new HashMap<>();
    protected final VetoableChangeSupport vSupport = new VetoableChangeSupport(this);

    protected ImportItem(String sourceNode) {
        this.sourceNode = sourceNode;
    }

    public abstract boolean isFragment();

    public Marker[] allMarkers() {
        return uniqueMarkers.stream()
                .toArray(Marker[]::new);
    }

    public String getSourceNodeLabel() {
        return sourceNode;
    }

    public UniqueMarkerSet getUniqueMarkerSet() {
        return uniqueMarkers;
    }

    public LocalDate getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(LocalDate deleteDate) {
        LocalDate old = this.deleteDate;
        this.deleteDate = deleteDate;
        try {
            vSupport.fireVetoableChange(PROP_DELETE_DATE, old, deleteDate);
        } catch (PropertyVetoException ex) {
            this.deleteDate = old;
        }
    }

    public abstract boolean isValid();

    public Object getClientProperty(String key) {
        Object ret;
        synchronized (userProperties) {
            ret = userProperties.get(key);
        }
        return ret;
    }

    public void setClientProperty(String key, Object value) throws PropertyVetoException {
        Object before;
        synchronized (userProperties) {
            before = userProperties.put(key, value);
        }
        vSupport.fireVetoableChange(key, before, value);
    }

    public void addVetoableChangeListener(VetoableChangeListener listener) {
        vSupport.addVetoableChangeListener(listener);
    }

    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        vSupport.removeVetoableChangeListener(listener);
    }

    public interface CloneableImport {

        public int id();
    }
}
