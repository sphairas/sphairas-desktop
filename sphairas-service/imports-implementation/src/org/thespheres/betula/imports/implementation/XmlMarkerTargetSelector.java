/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.imports.implementation;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.util.MarkerAdapter.XmlMarkerAdapter;
import org.thespheres.betula.ui.util.JAXBUtil;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.model.XmlTargetSelector;

/**
 *
 * @author boris.heithecker
 */
@JAXBUtil.JAXBRegistrations({
    @JAXBUtil.JAXBRegistration(target = "XmlTargetImportSettings"),
    @JAXBUtil.JAXBRegistration(target = "XmlTargetProcessorHintsSettings")})
@XmlRootElement(name = "marker-target-selector") //, namespace = "http://www.thespheres.org/xsd/betula/target-settings-defaults.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlMarkerTargetSelector extends XmlTargetSelector {

    public enum Type {
        PRESENCE, ABSENCE;
    }
    @XmlJavaTypeAdapter(XmlMarkerAdapter.class)
    @XmlElement(name = "marker")
    private Marker[] markers;
    @XmlElement(name = "marker-convention")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String[] conventions;
    @XmlAttribute(name = "type")
    private Type type;

    public XmlMarkerTargetSelector() {
        super();
    }

    public XmlMarkerTargetSelector(String id) {
        super(id);
    }

    @Override
    public boolean applies(ImportTargetsItem item) {
        final Set<Marker> ums = Arrays.stream(item.allMarkers()).collect(Collectors.toSet());
        boolean contained = markers == null || Arrays.stream(markers).allMatch(ums::contains);
        contained = contained && 
                (conventions == null || Arrays.stream(conventions).allMatch(c -> ums.stream().anyMatch(m -> m.getConvention().equals(c))));
        final Type t = type == null ? Type.PRESENCE : type;
        return t.equals(Type.PRESENCE) ? contained : !contained;
    }

    public Marker[] getMarkers() {
        return markers;
    }

    public void setMarkers(Marker[] marker) {
        this.markers = marker;
    }

    public String[] getConventions() {
        return conventions;
    }

    public void setConventions(String[] conventions) {
        this.conventions = conventions;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

}
