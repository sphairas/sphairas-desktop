/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis.listen;

import java.text.Collator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import org.thespheres.betula.niedersachsen.NdsTerms;
import org.thespheres.betula.niedersachsen.zeugnis.NdsReportBuilderFactory;
import org.thespheres.betula.niedersachsen.zeugnis.NdsReportConstants;
import org.thespheres.betula.niedersachsen.zeugnis.SubjectOrderDefinition;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ColumnKey.MarkerColumnKey;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ColumnKey.TermColumnKey;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ZensurenListeXml.ColumnXml;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ZensurenListeXml.DataLineXml;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ZensurenListeXml.FootnoteXml;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ZensurenListe.Column;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ZensurenListe.Footnote;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ZensurenListe.DataLine;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.util.StudentComparator;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ZensurenListeXml implements ZensurenListe<DataLineXml, FootnoteXml, ColumnXml> {

    private static final String WPK = "WPK";
    private static final String PROFIL_RS = "Profil";
    @XmlElement(name = "list-data")
    private final ListData lData = new ListData();
    @XmlElementWrapper(name = "data-header")
    @XmlElement(name = "subject")
    private List<ColumnXml> columns = new ArrayList<>();
//    private final transient HashMap<Integer, String> map = new HashMap<>();
    @XmlElement(name = "data")
    private final ArrayList<DataLineXml> list = new ArrayList<>();
    @XmlAttribute(name = "first-column-width", required = false)
    public String firstColumnWidth;
    @XmlElement(name = "footnote")
    private final List<FootnoteXml> footnotes = new ArrayList<>();
    @XmlElement(name = "text")
    private List<Text> texts = new ArrayList<>();
    private transient String sortString;
    final static Collator COLLATOR = Collator.getInstance(Locale.GERMANY);
    private final SubjectOrderDefinition ORDER = NdsReportConstants.FACH_COMPARATOR;
//    @XmlTransient
//    private final Map<Integer, Set> colset = new HashMap<>();
    @XmlTransient
    private final Map<ColumnKey, ColumnXml> colset = new HashMap<>();

    public String getSortString() {
        return sortString;
    }

    public void setSortString(String sort) {
        this.sortString = sort;
    }

    @Override
    public String getListName() {
        return lData.name;
    }

    @Override
    public void setListName(String lname) {
        lData.name = lname;
    }

    @Override
    public void setListDate(String ldate) {
        lData.version = ldate;
    }

    @Override
    public String getListDate() {
        return lData.version;
    }

    @Override
    public DataLineXml addLine(String sName) {
        DataLineXml ret = new DataLineXml();
        ret.student = sName;
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

    @Override
    public FootnoteXml addFootnote(String text) {
        FootnoteXml ret = new FootnoteXml(text);
        footnotes.add(ret);
        ret.setIndex(footnotes.indexOf(ret));
        return ret;
    }

    public void beforeMarshal(Marshaller marshaller) {
//        columns.clear();
//        colset.entrySet().stream().forEach(e -> {
//            e.getValue().stream()
//                    .map(value -> mapToColumn(value, e.getKey()))
//                    .forEach(s -> columns.add(s));
//        });
        colset.values().stream()
                .forEach(columns::add);
    }

    private ColumnXml mapToColumn(Object value, int tier) {
        String fName;
        int pos;
//        boolean keep = false;
        if (value instanceof Set) {
            final Set<Marker> ms;
            try {
                ms = (Set<Marker>) value;
            } catch (ClassCastException cce) {
                throw new IllegalArgumentException(cce);
            }
//            fName = ((Marker) value).getLongLabel();
            fName = ms.size() == 1 ? ms.iterator().next().getLongLabel() : ms.stream()
                    .sorted(ORDER)
                    .map(Marker::getLongLabel).collect(NdsReportBuilderFactory.SUBJECT_JOINING_COLLECTOR);
//            pos = ORDER.positionOf((Marker) value);
            final Marker min = ms.stream().min(ORDER).get();
            pos = ORDER.positionOf(min);
        } else if (value instanceof Term) {
//            keep = true;
            Term term = (Term) value;
            int jahr = (Integer) term.getParameter(NdsTerms.JAHR);
            int nJahr = jahr + 1;
            int hj = (Integer) term.getParameter(NdsTerms.HALBJAHR);
            fName = lastTowDigits(jahr) + "/" + lastTowDigits(nJahr) + "\u00A0" + hj + ".\u00A0Hj.";
            pos = term.getScheduledItemId().getId();
        } else {
            throw new IllegalArgumentException();
        }
        if (fName.startsWith("Profil ")) {
            fName = fName.replaceAll("Profil ", "");
        }
        if (fName.length() > 40) {
            fName = fName.substring(0, 37) + "...";
        }
        ColumnXml ret = new ColumnXml(fName, tier, pos);
//        if (keep) {
//            ret.keepTogetherWithinLine = "always";
//        }
        int l = 0;
        if (tier == 1) {
            l += WPK.length();
            ret.setLevel(WPK);
        } else if (tier == 2) {
            ret.setLevel(PROFIL_RS);
            l += PROFIL_RS.length();
        }
        l += fName.length();
        if (l > 25) {
            ret.setFontSize("7pt");//ZGN
        } else if (l > 15) {
            ret.setFontSize("9pt");//ZGN
        } else {
            ret.setFontSize("11pt");
        }
        return ret;
    }

    private String lastTowDigits(int number) {
        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setMinimumIntegerDigits(2);
        String ret = nf.format(number);
        return ret.length() > 2 ? ret.substring(ret.length() - 2) : ret;
    }

    @Override
    public ColumnXml setValue(DataLineXml line, int tier, Set<Marker> fach, Grade g, String ifGradeNull) {
        MarkerColumnKey ck = new MarkerColumnKey(tier, fach);
        if (g == null && line.map.containsKey(ck)) {
            return null;
        }
        final String label = g != null ? g.getShortLabel() : (ifGradeNull != null ? ifGradeNull : null);
        final Marker min = fach.stream().min(ORDER).get();
        final ColumnXml ret = new ColumnXml(label, tier, ORDER.positionOf(min));
//        colset.computeIfAbsent(tier, t -> new HashSet<>()).add(fach);
        colset.computeIfAbsent(ck, k -> mapToColumn(fach, tier));
        line.map.put(ck, ret);
//        line.values.add(ret);
        return ret;
    }

    public ColumnXml setValue(DataLineXml line, Term term, Grade g, String ifGradeNull) {
        return setValue(line, 0, term, g, ifGradeNull);
    }

    public ColumnXml setValue(DataLineXml line, int tier, Term term, Grade g, String ifGradeNull) {
        final TermColumnKey ck = new TermColumnKey(tier, term.getScheduledItemId());
        if (g == null && line.map.containsKey(ck)) {
            return null;
        }
        String label = g != null ? g.getShortLabel() : (ifGradeNull != null ? ifGradeNull : null);
        ColumnXml ret = new ColumnXml(label, 0, term.getScheduledItemId().getId());
//        colset.computeIfAbsent(Integer.MAX_VALUE, t -> new HashSet<>()).add(term);
        colset.computeIfAbsent(ck, k -> mapToColumn(term, 0));
//        colset.compute(Integer.MAX_VALUE, (t, s) -> {
//            if (s == null) {
//                s = new HashSet<>();
//            }
//            s.add(term);
//            return s;
//        });
        line.map.put(ck, ret);
//        line.values.add(ret);
        return ret;
    }

    public void removeEmptyColumns() {
        final int lines = list.size();
        final Iterator<Map.Entry<ColumnKey, ColumnXml>> it = this.colset.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<ColumnKey, ColumnXml> e = it.next();
            final ColumnKey key = e.getKey();
            long nullColCount = list.stream()
                    .map(l -> l.map.get(key))
                    .map(c -> c.value)
                    .filter(Objects::isNull)
                    .count();
            final boolean removeColumn = nullColCount == lines;
            if (removeColumn) {
                it.remove();
                list.stream().forEach(l -> {
                    final Iterator<Map.Entry<ColumnKey, ColumnXml>> itLine = l.map.entrySet().iterator();
                    while (itLine.hasNext()) {
                        Map.Entry<ColumnKey, ColumnXml> eLine = itLine.next();
                        if (eLine.getKey().equals(key)) {
                            itLine.remove();
                        }
                    }
                });
            }
        }
    }

    public void sort() {
        Collections.sort(list);
    }

    private static class ListData {

        @XmlElement(name = "list-name")
        private String name;
        @XmlElement(name = "list-version")
        private String version;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ColumnXml implements Comparable<ColumnXml>, Column<FootnoteXml> {

        @XmlAttribute(name = "tier", required = true)
        private int tier = 0;
        @XmlAttribute(name = "order", required = true)
        private int order = Integer.MAX_VALUE;
        @XmlAttribute(name = "subject-level")
        private String level;
        @XmlAttribute(name = "label")
        private String lbl;
        @XmlValue
        private String value;
        @XmlAttribute(name = "font-size", required = false)
        private String fontSize;
        @XmlAttribute(name = "color", required = false)
        private String color;

        private ColumnXml(String longLabel, int tier, int order) {
            this.value = longLabel;
            this.tier = tier;
            this.order = order;
        }

        @Override
        public int compareTo(ColumnXml o) {
            return this.order - o.order;
        }

        @Override
        public String getLevel() {
            return level;
        }

        @Override
        public void setLevel(String level) {
            this.level = level;
        }

        @Override
        public void setFootnote(FootnoteXml lbl) {
            this.lbl = lbl.getIndex();
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
    public static class DataLineXml implements Comparable<DataLineXml>, DataLine {

        @XmlElement(name = "student-name")
        private String student;
        @XmlAttribute(name = "student-hint")
        private String studentHint;
        @XmlElement(name = "subject-value")
        private ArrayList<ColumnXml> values2 = new ArrayList<>();
        @XmlElement(name = "note")
        private String note;
        @XmlTransient
        private final Map<ColumnKey, ColumnXml> map = new HashMap<>();

        @Override
        public int compareTo(DataLineXml o) {
            return COLLATOR.compare(StudentComparator.sortStringFromDirectoryName(student), StudentComparator.sortStringFromDirectoryName(o.student));
        }

        @Override
        public String getNote() {
            return note;
        }

        @Override
        public void setNote(String note) {
            this.note = note;
        }

        @Override
        public String getStudentHint() {
            return studentHint;
        }

        @Override
        public void setStudentHint(String studentHint) {
            this.studentHint = studentHint;
        }

        public void beforeMarshal(Marshaller marshaller) {
            values2.clear();
            map.values().forEach(values2::add);
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
    public static class FootnoteXml implements Footnote {

        @XmlValue
        private String value;
        @XmlAttribute(name = "index")
        private String index;

        FootnoteXml(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String getIndex() {
            return index;
        }

        void setIndex(int index) {
            this.index = Integer.toString(index + 1) + ".)";
        }

    }
}
