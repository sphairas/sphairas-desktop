/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.config;

import java.util.Objects;
import org.thespheres.betula.document.Marker;

/**
 *
 * @author boris.heithecker@gmx.net
 */
public class HeadTeachersType {

    private final String docIdName;
    private final String remoteSigneeProperty;
    private final Marker addAllUnitsSelector;

    public HeadTeachersType(final String docIdName, final String remoteSigneeProperty, final Marker addAllUnitsSelector) {
        this.docIdName = docIdName;
        this.remoteSigneeProperty = remoteSigneeProperty;
        this.addAllUnitsSelector = addAllUnitsSelector;
    }

    public String getDocIdName() {
        return docIdName;
    }

    public String getRemoteSigneeProperty() {
        return remoteSigneeProperty;
    }

    public Marker getAddAllUnitsSelector() {
        return addAllUnitsSelector;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return 53 * hash + Objects.hashCode(this.docIdName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HeadTeachersType other = (HeadTeachersType) obj;
        return Objects.equals(this.docIdName, other.docIdName);
    }

}
