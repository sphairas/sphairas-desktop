/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author boris.heithecker
 */
//@XmlRootElement(name = "zeugnis-sekundarstufe-niedersachsen", namespace = "http://www.thespheres.org/xsd/betula/zeugnis-sekundarstufe-niedersachsen/v0-0-1")
@XmlRootElement(name = "zeugnis-sekundarstufe-niedersachsen")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZeugnisData {

    public static String PFLICHTUNTERRICHT = "pu";
    public static String LEHRGAENGE = "lehrgaenge";
    public static String WAHLPFLICHTUNTERRICHT = "wpu";
    public static String PROFILUNTERRICHT = "profilu";
    public static String BERICHTE = "berichte";

    @XmlElement(name = "Kopf")
    private final Header kopf = new Header();
    @XmlElement(name = "Bereich")
    private List<Area> areas = new ArrayList<>();
    @XmlElement(name = "Fußnote")
    private List<Footnote> footnotes = new ArrayList<>();
    @XmlElement(name = "Zusatz")
    private List<Note> notes = new ArrayList<>();
    @XmlElement(name = "Kopf2")
    private String secondPageHeader;
    @XmlElement(name = "Ort-Datum")
    private String placeDate;
    @XmlElement(name = "Zweitausfertigung")
    private Duplicate duplicate;
    @XmlAttribute(name = "addSpace")
    private Integer addSpace;
    private transient String sortString;

    public String getSortString() {
        return sortString;
    }

    public void setSortString(String sort) {
        this.sortString = sort;
    }

    public Header getKopf() {
        return kopf;
    }

    public List<Area> getAreas() {
        return areas;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public List<Footnote> getFootnotes() {
        return footnotes;
    }

    public String getPlaceDate() {
        return placeDate;
    }

    public void setPlaceDate(String placeDate) {
        this.placeDate = placeDate;
    }

    public String getSecondPageHeader() {
        return secondPageHeader;
    }

    public void setSecondPageHeader(String secondPageHeader) {
        this.secondPageHeader = secondPageHeader;
    }

    public Duplicate getDuplicate() {
        return duplicate;
    }

    public void setDuplicate(Duplicate duplicate) {
        this.duplicate = duplicate;
    }

    public Area addArea(String u) {
        Area toAdd = new Area(u);
        areas.add(toAdd);
        return toAdd;
    }

    public Note addNote(String u, int position) {
        for (Note n : getNotes()) {
            if (n.getPosition() == position) {
                n.noteHeader = u;
                return n;
            }
        }
        Note toAdd = new Note(u, position);
        notes.add(toAdd);
        return toAdd;
    }

    public Footnote addFootnote(String index, String text) {
        for (Footnote n : getFootnotes()) {
            if (n.getIndex().equals(index)) {
                n.value = text;
                return n;
            }
        }
        Footnote toAdd = new Footnote(index, text);
        footnotes.add(toAdd);
        return toAdd;
    }

    public final void beforeMarshal(Marshaller m) {
        int addmm = 0;
        if (kopf.finalTitle != null) {
            addmm += 8;
        }
        if (areas.size() > 2) {
            addmm += (9 * (areas.size() - 2));
        }
        final long ll = areas.stream()
                .map(a -> a.lines)
                .collect(Collectors.summarizingInt(l -> l.size()))
                .getSum();
        if (ll > 6l) {
            addmm += (5 * (ll - 6));
        }
        if (kopf.leaving != null) {
            addmm += 5;
        }
        if (addmm > 30) {
            addSpace = 12;
        } else if (addmm > 16) {
            addSpace = 8;
        } else {
            addSpace = null;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Header {

        @XmlElement(name = "Schulname")
        private String schoolname;
        @XmlElement(name = "Schulname2")
        private String schoolname2;
        @XmlElement(name = "LogoLinks")
        private String imageLeft;
        @XmlElement(name = "LogoRechts")
        private String imageRight;
        @XmlElement(name = "Schulzweig")
        private String divison;
        @XmlElement(name = "Zeugnisart")
        private String reportTitle;
        @XmlElement(name = "Halbjahrsangaben")
        private HeaderDataLine termData = null;
        @XmlElement(name = "Vorname-Nachname")
        private String studentName;
        @XmlElement(name = "Herkunftsangaben")
        private HeaderDataLine dataPlaceOfBirth = new HeaderDataLine();
        @XmlElement(name = "Abgangsdaten")
        private SchoolLeaving leaving;
        @XmlElement(name = "Schulabschluss")
        private String finalTitle = null;
        @XmlElement(name = "Versaeumnisse")
        private HeaderDataLine daysAbsent = new HeaderDataLine();

        public String getSchoolname() {
            return schoolname;
        }

        public void setSchoolname(String schoolname) {
            this.schoolname = schoolname;
        }

        public String getSchoolname2() {
            return schoolname2;
        }

        public void setSchoolname2(String schoolname2) {
            this.schoolname2 = schoolname2;
        }

        public String getImageLeft() {
            return imageLeft;
        }

        public void setImageLeft(String imageLeft) {
            this.imageLeft = imageLeft;
        }

        public String getImageRight() {
            return imageRight;
        }

        public void setImageRight(String imageRight) {
            this.imageRight = imageRight;
        }

        public String getDivison() {
            return divison;
        }

        public void setDivison(String divison) {
            this.divison = divison;
        }

        public String getReportTitle() {
            return reportTitle;
        }

        public void setReportTitle(String reportTitle) {
            this.reportTitle = reportTitle;
        }

        public HeaderDataLine getTermData() {
            if (termData == null) {
                termData = new HeaderDataLine();
            }
            return termData;
        }

        public void setTermData(String left, String center, String right) {
            getTermData().setLeft(left);
            getTermData().setCenter(center);
            getTermData().setRight(right);
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }

        public HeaderDataLine getDatePlaceOfBirth() {
            return dataPlaceOfBirth;
        }

        public void setDatePlaceOfBirth(String date, String place) {
            getDatePlaceOfBirth().setLeft(date);
            getDatePlaceOfBirth().setRight(place);
        }

        public SchoolLeaving getLeaving() {
            return leaving;
        }

        public void setSchoolLeaving(String date, String year) {
            leaving = new SchoolLeaving(date, year);
        }

        public String getFinalTitle() {
            return finalTitle;
        }

        public void setFinalTitle(String finalTitle) {
            this.finalTitle = finalTitle;
        }

        public HeaderDataLine getDaysAbsent() {
            return daysAbsent;
        }

        public void setDaysAbsent(String absent, String unex) {
            getDaysAbsent().setLeft(absent);
            getDaysAbsent().setRight(unex);
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SchoolLeaving {

        @XmlElement(name = "Datum")
        private String date;
        @XmlElement(name = "Jahrgang")
        private String year;

        public SchoolLeaving() {
        }

        SchoolLeaving(String date, String year) {
            this.date = date;
            this.year = year;
        }

        public String getDate() {
            return date;
        }

        public String getYear() {
            return year;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class HeaderDataLine {

        @XmlElement(name = "Links")
        private String left;
        @XmlElement(name = "Mitte")
        private String center;
        @XmlElement(name = "Rechts")
        private String right;

        public String getLeft() {
            return left;
        }

        public void setLeft(String left) {
            this.left = left;
        }

        public String getCenter() {
            return center;
        }

        public void setCenter(String center) {
            this.center = center;
        }

        public String getRight() {
            return right;
        }

        public void setRight(String right) {
            this.right = right;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Area {

        @XmlElement(name = "Unterricht", required = true)
        private String header;
        @XmlElement(name = "Zeile")
        private final List<Line> lines = new ArrayList<>();
        @XmlElement(name = "Bericht")
        private List<Report> reports = new ArrayList<>();
        @XmlElement(name = "Anmerkung")
        private String note;

        //JAXBOnly
        public Area() {
        }

        public Area(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }

        public void addEmptyLine() {
            lines.add(new Line());
        }

        public Field[] addLine(LineFieldCallback fachLinks, LineFieldCallback fachRechts) {
            Line toAdd = new Line();
            setFields(toAdd, fachLinks, 0);
            setFields(toAdd, fachRechts, 1);
            lines.add(toAdd);
            return toAdd.fields;
        }

        private void setFields(Line toAdd, LineFieldCallback fach, int field) {
            if (fach != null) {
                toAdd.fields[field].subject = fach.getSubject();
                toAdd.fields[field].value = fach.getValue();
                toAdd.fields[field].type = fach.getType();
                toAdd.fields[field].footnoteRef = fach.getFootnoteRef();
            }
        }

        public List<Report> getReports() {
            return reports;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Line {

        @XmlElement(name = "Feld")
        private Field[] fields = new Field[]{new Field("Links"), new Field("Rechts")};

        public Field[] getFields() {
            return fields;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Field {

        @XmlAttribute(name = "Position", required = true)
        private String position;
        @XmlElement(name = "Fach", required = true)
        private String subject = "---";
        @XmlElement(name = "Noteneintrag", required = true)
        private String value = "---";
        @XmlElement(name = "Fachleistungskurs")
        private String type;
        @XmlAttribute(name = "Anmerkungsziffer")
        private String footnoteRef;

        //JAXBOnly
        public Field() {
        }

        public Field(String position) {
            this.position = position;
        }

        public String getPosition() {
            return position;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getFootnoteRef() {
            return footnoteRef;
        }

        public void setFootnoteRef(String footnoteRef) {
            this.footnoteRef = footnoteRef;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Footnote {

        @XmlValue
        private String value;
        @XmlAttribute(name = "Ziffer")
        private String index;

        Footnote(String index, String value) {
            this.value = value;
            this.index = index;
        }

        public static String index(int index) {
            return Integer.toString(index + 1) + ".)";
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getIndex() {
            return index;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Note {

        @XmlAttribute(name = "Titel", required = true)
        private String noteHeader;
        @XmlAttribute(name = "Position", required = true)
        private int position;
        @XmlValue
        //TODO: adapted CollapsedStringAdapter which leaves \n = 0xA untouched 
        private String noteValue;

        //JAXB only
        public Note() {
        }

        public Note(String noteHeader, int position) {
            this.noteHeader = noteHeader;
            this.position = position;
        }

        public String getNoteHeader() {
            return noteHeader;
        }

        public String getNoteValue() {
            return noteValue;
        }

        public void setValue(String noteValue) {
            this.noteValue = noteValue;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Report extends Note {

        public Report() {
        }

        public Report(String noteHeader, int position) {
            super(noteHeader, position);
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Duplicate {

        @XmlElement(name = "SiegelSchule")
        private String siegelSchuleOderBehoerde;
        @XmlElement(name = "Schulleiter")
        private String schulleiter;
        @XmlElement(name = "Klassenlehrer")
        private String klassenlehrer;
        @XmlElement(name = "Zusatz")
        private String zusatz;
        @XmlElement(name = "Datum")
        private String datum;

        public Duplicate() {
        }

        public Duplicate(String siegelSchuleOderBehoerde, String gezeichnet, String datumZweitausfertigung) {
            this.siegelSchuleOderBehoerde = siegelSchuleOderBehoerde;
            this.schulleiter = gezeichnet;
            this.zusatz = datumZweitausfertigung;
        }

        public String getSiegelSchuleOderBehoerde() {
            return siegelSchuleOderBehoerde;
        }

        public void setSiegelSchuleOderBehoerde(String siegelSchuleOderBehoerde) {
            this.siegelSchuleOderBehoerde = siegelSchuleOderBehoerde;
        }

        public String getSchulleiter() {
            return schulleiter;
        }

        public void setSchulleiter(String schulleiter) {
            this.schulleiter = schulleiter;
        }

        public String getKlassenlehrer() {
            return klassenlehrer;
        }

        public void setKlassenlehrer(String klassenlehrer) {
            this.klassenlehrer = klassenlehrer;
        }

        public String getDatum() {
            return datum;
        }

        public void setDatum(String datum) {
            this.datum = datum;
        }

        public String getZusatz() {
            return zusatz;
        }

        public void setZusatz(String zusatz) {
            this.zusatz = zusatz;
        }

    }

    @XmlRootElement(name = "zeugnis-sekundarstufe-niedersachsen-mappe")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ZeugnisCollection {

        @XmlTransient
        private final Collator collator = Collator.getInstance(Locale.GERMANY);
        @XmlElement(name = "zeugnis-sekundarstufe-niedersachsen")
        public final List<ZeugnisData> reports = new ArrayList<>();

        public void sort() {
            Collections.sort(reports, new Comparator<ZeugnisData>() {

                @Override
                public int compare(ZeugnisData zd1, ZeugnisData zd2) {
                    return collator.compare(zd1.getSortString(), zd2.getSortString());
                }
            });
        }
    }

    public interface LineFieldCallback {

        public String getSubject();

        public String getValue();

        default public String getType() {
            return null;
        }

        default public String getFootnoteRef() {
            return null;
        }
    }
}
