/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.xml;

import org.thespheres.betula.document.ValueElement;
import java.util.Arrays;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Marker;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "zeugnis-sekundarstufe-niedersachsen")
@XmlType(name = "zeugnis-angaben",
        propOrder = {"fehltage", "unentschuldigt", "arbeitsverhalten", "sozialverhalten", "markers", "text", "custom"})
@XmlAccessorType(XmlAccessType.FIELD)
public class NdsZeugnisAngaben {

    @XmlElement(name = "fehltage")
    private ValueElement<Integer> fehltage;
    @XmlElement(name = "unentschuldigt")
    private ValueElement<Integer> unentschuldigt;
    @XmlElement(name = "arbeitsverhalten")
    private ValueElement<Grade> arbeitsverhalten;
    @XmlElement(name = "sozialverhalten")
    private ValueElement<Grade> sozialverhalten;
    @XmlElement(name = "standardisierte-bemerkung-markierung")
    private ValueElement<Marker>[] markers;
    @XmlElement(name = "freie-bemerkung")
    private FreieBemerkung[] custom;
    @XmlElement(name = "text")
    private Text[] text;

    public NdsZeugnisAngaben() {
    }

    public NdsZeugnisAngaben(Integer fehltage, Integer unentschuldigt, Grade av, Grade sv, Marker[] markers, FreieBemerkung[] custom) {
        this.fehltage = fehltage == null ? null : new ValueElement<>(fehltage, null);
        this.unentschuldigt = unentschuldigt == null ? null : new ValueElement<>(unentschuldigt, null);
        this.arbeitsverhalten = av == null ? null : new ValueElement<>(av, null);
        this.sozialverhalten = sv == null ? null : new ValueElement<>(sv, null);
        this.markers = markers == null ? null : Arrays.stream(markers).map(m -> new ValueElement<>(m, null)).toArray(ValueElement[]::new);
        this.custom = custom;
    }

    public ValueElement<Integer> getFehltage() {
        return fehltage;
    }

    public void setFehltage(ValueElement<Integer> fehltage) {
        this.fehltage = fehltage;
    }

    public ValueElement<Integer> getUnentschuldigt() {
        return unentschuldigt;
    }

    public void setUnentschuldigt(ValueElement<Integer> unentschuldigt) {
        this.unentschuldigt = unentschuldigt;
    }

    public ValueElement<Grade> getArbeitsverhalten() {
        return arbeitsverhalten;
    }

    public void setArbeitsverhalten(ValueElement<Grade> arbeitsverhalten) {
        this.arbeitsverhalten = arbeitsverhalten;
    }

    public ValueElement<Grade> getSozialverhalten() {
        return sozialverhalten;
    }

    public void setSozialverhalten(ValueElement<Grade> sozialverhalten) {
        this.sozialverhalten = sozialverhalten;
    }

    public Marker[] getMarkers() {
        return markers == null ? null : Arrays.stream(markers)
                .map(ValueElement::getValue)
                .filter(Objects::nonNull)
                .toArray(Marker[]::new);
    }

    public ValueElement<Marker>[] getMarkerElements() {
        return markers;
    }

    public void setMarkerElements(ValueElement<Marker>[] markers) {
        this.markers = markers;
    }

    public Text[] getText() {
        return text;
    }

    public void setText(Text[] text) {
        this.text = text;
    }

    public FreieBemerkung[] getCustom() {
        return custom;
    }

    public void setCustom(FreieBemerkung[] custom) {
        this.custom = custom;
    }

    @Deprecated //Use Text
    public static class FreieBemerkung extends ValueElement<String> {

        @XmlAttribute
        private int position;

        public FreieBemerkung() {
        }

        public FreieBemerkung(String text, int position, Action action) {
            super(text, action);
            this.position = position;
        }

        public int getPosition() {
            return position;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Text extends ValueElement<String> {

        @XmlAttribute
        private String key;

        public Text() {
        }

        public Text(String key, String value, Action action) {
            super(value, action);
            this.key = key;
        }

        public String getKey() {
            return key;
        }

    }
}
