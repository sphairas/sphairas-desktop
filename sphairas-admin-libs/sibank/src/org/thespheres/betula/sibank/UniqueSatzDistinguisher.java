/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank;

import java.text.Collator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.sibank.DatenExportXml.File;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(value = XmlAccessType.FIELD)
public final class UniqueSatzDistinguisher {

    static final Collator KLASSEN_COLLATOR = Collator.getInstance(Locale.getDefault());
    @XmlElement(name = "STUFE")
    private int stufe = -1;
//    @XmlElement(name = "KLASSE")
//    private String klasse = null;
    @XmlElement(name = "FACH")
    private String fach = null;
    @XmlElement(name = "FACHART")
    private String fachart = null;
    @XmlElement(name = "KURSNR")
    private String kursnr = null;
    @XmlElement(name = "LEHRERK_RZEL")
    private String lkuerzel = null;
    @XmlElement(name = "LEHRERNAME")
    private String lname = null;
    @XmlList
    @XmlElement(name = "Klassen")
    private final Set<String> klassen = new HashSet<>();

    //JAXB
    public UniqueSatzDistinguisher() {
    }

    public UniqueSatzDistinguisher(DatenExportXml.Satz s) {
        try {
            if (s.stufe != null) {
                this.stufe = Integer.valueOf(s.stufe);
            }
        } catch (NumberFormatException nex) {
            this.stufe = -1;
        }
        this.fach = StringUtils.trimToNull(s.fach);
        this.fachart = StringUtils.trimToNull(s.fachart);
        this.kursnr = StringUtils.trimToNull(s.kursnr);
        this.lkuerzel = StringUtils.trimToNull(s.lkuerzel);
        this.lname = StringUtils.trimToNull(s.lname);
    }

    void addKlasse(final String v) {
        final String add = StringUtils.deleteWhitespace(v);
        klassen.add(add);
    }

    String klassen() {
        return klassen.stream()
                .sorted(KLASSEN_COLLATOR)
                .collect(Collectors.joining(" "));
    }

    public int getStufe() {
        return stufe;
    }

    public String getFach() {
        return fach;
    }

    public String getFachart() {
        return fachart;
    }

    public String getKursnr() {
        return kursnr;
    }

    public String getLkuerzel() {
        return lkuerzel;
    }

    public String getLname() {
        return lname;
    }

    public String sourceNodeLabel(DatenExportXml.File forType) {
        if (forType.equals(File.KURSE)) {
            StringJoiner sj = new StringJoiner(" ");
            if (getFach() != null) {
                sj.add(getFach());
            }
            if (getStufe() != -1) {
                sj.add(Integer.toString(getStufe()));
            }
            if (getFachart() != null) {
                sj.add(getFachart());
            }
            if (getKursnr() != null) {
                sj.add(getKursnr());
            }
            if (!klassen.isEmpty()) {
                final String kl = klassen.stream()
                        .sorted(KLASSEN_COLLATOR)
                        .collect(Collectors.joining(","));
                sj.add(kl);
            }
            return sj.toString();
        } else if (forType.equals(File.AGS)) {
            return getFach();
        }
        return toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + this.stufe;
        hash = 17 * hash + Objects.hashCode(this.fach);
        hash = 17 * hash + Objects.hashCode(this.fachart);
        hash = 17 * hash + Objects.hashCode(this.kursnr);
        hash = 17 * hash + Objects.hashCode(this.lkuerzel);
        hash = 17 * hash + Objects.hashCode(this.lname);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UniqueSatzDistinguisher other = (UniqueSatzDistinguisher) obj;
        if (this.stufe != other.stufe) {
            return false;
        }
        if (!Objects.equals(this.fach, other.fach)) {
            return false;
        }
        if (!Objects.equals(this.fachart, other.fachart)) {
            return false;
        }
        if (!Objects.equals(this.kursnr, other.kursnr)) {
            return false;
        }
        if (!Objects.equals(this.lkuerzel, other.lkuerzel)) {
            return false;
        }
        return Objects.equals(this.lname, other.lname);
    }

    @Override
    public String toString() {
        return "DatenExportXml.Satz{" + "stufe=" + stufe + ", fach=" + fach + ", fachart=" + fachart + ", kursnr=" + kursnr + ", lkuerzel=" + lkuerzel + ", lname=" + lname + '}';
    }

}
