/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis.listen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.niedersachsen.zeugnis.NdsReportBuilderFactory;
import org.thespheres.betula.niedersachsen.zeugnis.NdsReportConstants;
import org.thespheres.betula.niedersachsen.zeugnis.SubjectOrderDefinition;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class StudentDetailsXml {

    static final String WPK = "WPK";
//    private static final String PROFIL_RS = "Profil";
    @XmlElement(name = "list-data")
    private final ListData listData = new ListData();
    @XmlElementWrapper(name = "subjects")
    @XmlElement(name = "subject")
    private final List<Column> subjects = new ArrayList<>();
    @XmlElement(name = "term-data")
    private final ArrayList<TermDataLine> list = new ArrayList<>();
    @XmlAttribute(name = "term-data-first-column-width", required = false)
    public String firstColumnWidth;
    @XmlElement(name = "footnote")
    private final List<Footnote> footnotes = new ArrayList<>();
    @XmlElement(name = "text")
    private List<Text> texts = new ArrayList<>();
    private transient String sortString;
//    private final static Collator COLLATOR = Collator.getInstance(Locale.GERMANY);
    private final static SubjectOrderDefinition ORDER = NdsReportConstants.FACH_COMPARATOR;
//    @XmlTransient
//    private final Map<ColumnKey, ColumnValue> colmap = new HashMap<>();

    public String getSortString() {
        return sortString;
    }

    public void setSortString(String sort) {
        this.sortString = sort;
    }

    public String getListName() {
        return listData.name;
    }

    public void setListName(String lname) {
        listData.name = lname;
    }

    public void setListDate(String ldate) {
        listData.version = ldate;
    }

    public TermDataLine addLine(int line, String termName) {
        TermDataLine ret = new TermDataLine(line, termName);
        list.add(ret);
        return ret;
    }

    public Text addText(String u, int position) {
        for (Text t : getTexts()) {
            if (t.getPosition() == position) {
                t.noteHeader = u;
                return t;
            }
        }
        Text toAdd = new Text(u, position);
        texts.add(toAdd);
        return toAdd;
    }

    public List<Text> getTexts() {
        return texts;
    }

    public Footnote addFootnote(String text) {
        Footnote ret = new Footnote(text);
        footnotes.add(ret);
        ret.setIndex(footnotes.indexOf(ret));
        return ret;
    }

    public List<Footnote> getFootnotes() {
        return footnotes;
    }

    public void beforeMarshal(Marshaller marshaller) {
        final Set<ColumnKey.MarkerColumnKey> allKeys = new HashSet<>();
        list.stream()
                .flatMap(l -> l.map.keySet().stream())
                .sorted(Comparator.comparing(c -> c.tier))
                .sorted(Comparator.comparing(c -> ORDER.positionOf(c.comparingMarker(ORDER))))
                .distinct()
                .peek(allKeys::add)
                .map(StudentDetailsXml::mapToColumn)
                .forEach(subjects::add);
        list.stream()
                .forEach(l -> l.beforeMarshal(allKeys));
        Collections.sort(list, Comparator.comparing(l -> l.row));
    }

    private static Column mapToColumn(ColumnKey.MarkerColumnKey key) throws IllegalArgumentException {
//        boolean keep = false;
        String fName = key.marker.size() == 1 ? key.marker.iterator().next().getLongLabel() : key.marker.stream()
                .sorted(ORDER)
                .map(Marker::getLongLabel)
                .collect(NdsReportBuilderFactory.SUBJECT_JOINING_COLLECTOR);

//            if (fName.startsWith("Profil ")) {
//                fName = fName.replaceAll("Profil ", "");
//            }
//            int ws = 0;
//            while ((ws = fName.indexOf("\u0020", ws)) != -1) {
//            }
        String[] wsp = fName.split("\u0020");
        StringJoiner wsj = new StringJoiner("\u00A0");
        for (int i = 0; i < wsp.length - 1; i++) {
            wsj.add(wsp[i]);
        }
        fName = wsj.toString() + "\u0020" + wsp[wsp.length - 1];
//            if (fName.indexOf(" ") < 11) {
//                fName = fName.replaceFirst(" ", "\u00A0"); /// \u200B
//            }
        Column ret = new Column(fName, key.tier, ORDER.positionOf(key.comparingMarker(ORDER)));
//        if (keep) {
//            ret.keepTogetherWithinLine = "always";
//        }
        int l = 0;
        if (key.tier == 1) {
            l += WPK.length();
            ret.setLabelLeft(WPK);
        } else if (key.tier == 2) {
//            ret.setLabelLeft(PROFIL_RS);
//            l += PROFIL_RS.length();
        }
        l += fName.length();
        if (l > 13) {
            ret.setFontSize("9pt");//ZGN
        } else {
            ret.setFontSize("11pt");
        }
        return ret;
    }

    public ColumnValue setValue(TermDataLine line, int tier, Set<Marker> fach, Grade g, String ifGradeNull) {
        final ColumnKey.MarkerColumnKey k = new ColumnKey.MarkerColumnKey(tier, fach);
        if (g == null && line.map.containsKey(k)) {
            return null;
        }
        final String text = g != null ? g.getShortLabel() : (ifGradeNull != null ? ifGradeNull : null);
        final ColumnValue ret = new ColumnValue(text);
        line.map.put(k, ret);
        return ret;
    }

    private static class ListData {

        @XmlElement(name = "student-detail-name")
        private String name;
        @XmlElement(name = "student-detail-version")
        private String version;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ColumnValue {

        @XmlAttribute(name = "label-left")
        private String left;
        @XmlAttribute(name = "label-right")
        private String right;
        @XmlValue
        private String value;
        @XmlAttribute(name = "font-size", required = false)
        private String fontSize;
        @XmlAttribute(name = "color", required = false)
        private String color;

        private ColumnValue(String value) {
            this.value = value;
        }

        public String getLabelLeft() {
            return left;
        }

        public void setLabelLeft(String level) {
            this.left = level;
        }

        public String getLabelRight() {
            return this.right;
        }

        public void setLabelRight(String lbl) {
            this.right = lbl;
        }

        public String getFontSize() {
            return fontSize;
        }

        public void setFontSize(String fontSize) {
            this.fontSize = fontSize;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Column extends ColumnValue implements Comparable<Column> {

        @XmlAttribute(name = "tier", required = true)
        private int tier = 0;
        @XmlAttribute(name = "order", required = true)
        private int order = Integer.MAX_VALUE;

        private Column(String longLabel, int tier, int order) {
            super(longLabel);
            this.tier = tier;
            this.order = order;
        }

        @Override
        public int compareTo(Column o) {
            return this.order - o.order;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TermDataLine {

        @XmlElement(name = "term-name")
        private String term;
        @XmlAttribute(name = "term-hint")
        private String hint;
        @XmlElement(name = "column-value")
        private final ArrayList<Column> values = new ArrayList<>();
        @XmlTransient
        private final Map<ColumnKey.MarkerColumnKey, ColumnValue> map = new HashMap<>();
        @XmlElement(name = "note")
        private String note;
        @XmlAttribute(name = "row")
        private final int row;

        TermDataLine(int row, String term) {
            this.row = row;
            this.term = term;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getStudentHint() {
            return hint;
        }

        public void setStudentHint(String studentHint) {
            this.hint = studentHint;
        }

        private void beforeMarshal(final Set<ColumnKey.MarkerColumnKey> allKeys) {
            final Map<ColumnKey.MarkerColumnKey, ColumnValue> all = allKeys.stream()
                    .collect(Collectors.toMap(k -> k, key -> map.computeIfAbsent(key, k -> new ColumnValue(null))));
            all.entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getKey().tier))
                    .sorted(Comparator.comparing(e -> ORDER.positionOf(e.getKey().comparingMarker(ORDER))))
                    .distinct()
                    .map(e -> mapToColumn(e.getKey(), e.getValue()))
                    .forEach(values::add);

        }

        private static Column mapToColumn(ColumnKey.MarkerColumnKey key, ColumnValue value) throws IllegalArgumentException {
            final Column ret = new Column(value.value, key.tier, ORDER.positionOf(key.comparingMarker(ORDER)));
            ret.setLabelLeft(value.getLabelLeft());
            ret.setLabelRight(value.getLabelRight());
            ret.setColor(value.getColor());
            return ret;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Note {

        @XmlAttribute(name = "font-size", required = false)
        private String fontSize;
        @XmlValue
        private String value;

        private Note(String value) {
            this.value = value;
        }

        public String getFontSize() {
            return fontSize;
        }

        public void setFontSize(String fontSize) {
            this.fontSize = fontSize;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Text {

        @XmlAttribute(name = "title", required = true)
        private String noteHeader;
        @XmlAttribute(name = "position", required = true)
        private int position;
        @XmlValue
        //TODO: adapted CollapsedStringAdapter which leaves \n = 0xA untouched 
        private String noteValue;

        //JAXB only
        public Text() {
        }

        public Text(String noteHeader, int position) {
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
    public static class Footnote {

        @XmlValue
        private String value;
        @XmlAttribute(name = "index")
        private String index;
        @XmlAttribute(name = "hint")
        private String hint;

        Footnote(String value) {
            this.value = value;
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

        void setIndex(int index) {
            this.index = Integer.toString(index + 1) + ".)";
        }

        public String getHint() {
            return hint;
        }

        public void setHint(String hint) {
            this.hint = hint;
        }

    }

}
