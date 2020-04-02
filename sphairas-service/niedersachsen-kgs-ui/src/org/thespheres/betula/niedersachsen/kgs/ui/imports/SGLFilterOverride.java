/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.imports;

import java.util.Arrays;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.document.util.MarkerAdapter.XmlMarkerAdapter;
import org.thespheres.betula.xmlimport.utilities.ColumnProperty;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(value = XmlAccessType.FIELD)
public abstract class SGLFilterOverride extends ColumnProperty {

    @Deprecated
    @XmlAttribute(name = "convention", required = true)
    private String convention;
    @Deprecated
    @XmlAttribute(name = "id", required = true)
    private String id;
    @Deprecated
    @XmlAttribute(name = "subset")
    private String subset = null;
    @XmlJavaTypeAdapter(XmlMarkerAdapter.class)
    private Marker[] filter = new Marker[]{};
    @XmlAttribute(name = "includes-sgl-absent-filter")
    private Boolean includeAbsentSGL = null;

    public SGLFilterOverride() {
    }

    protected SGLFilterOverride(final Marker[] marker) {
        if (marker == null) {
            this.filter = null;
        } else {
            this.includeAbsentSGL = Arrays.stream(marker).anyMatch(SGLFilter.NO_SGL::equals) ? true : null;
            this.filter = Arrays.stream(marker)
                    .filter(m -> !SGLFilter.NO_SGL.equals(m))
                    .toArray(Marker[]::new);
        }
    }

    @Override
    public String getColumnId() {
        return "sglfilter";
    }

    public boolean isIncludeAbsentSGL() {
        return this.includeAbsentSGL != null && this.includeAbsentSGL;
    }

    public void setIncludeAbsentSGL(boolean includeAbsentSGL) {
        this.includeAbsentSGL = includeAbsentSGL ? true : null;
    }

    public Marker[] getFilters() {
        //legacy case
        if (id != null && convention != null) {
            final Marker legCase = MarkerFactory.find(convention, id, subset);
            final Marker[] ret = Arrays.copyOf(filter, filter.length + 1);
            ret[filter.length] = legCase;
            return ret;
        }
        if (filter != null) {
            final Marker[] ret = Arrays.copyOf(filter, isIncludeAbsentSGL() ? filter.length + 1 : filter.length);
            if (isIncludeAbsentSGL()) {
                ret[ret.length - 1] = SGLFilter.NO_SGL;
            }
            return ret;
        }
        return null;
    }
}
