/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis.listen;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.niedersachsen.zeugnis.NdsReportBuilderFactory;
import org.thespheres.betula.niedersachsen.zeugnis.SubjectOrderDefinition;
import org.thespheres.betula.niedersachsen.zeugnis.ZeugnisBuilder;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ColumnKey.MarkerColumnKey;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ZensurenListe.Column;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ZensurenListe.Footnote;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ZensurenListe.DataLine;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ZensurenListeCsv.ColumnCsv;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ZensurenListeCsv.DataLineCsv;
import org.thespheres.betula.niedersachsen.zeugnis.listen.ZensurenListeCsv.FootnoteCsv;
import org.thespheres.betula.util.StudentComparator;

/**
 *
 * @author boris.heithecker
 */
public class ZensurenListeCsv implements ZensurenListe<DataLineCsv, FootnoteCsv, ColumnCsv> {

    private String name;
    private String ldate;
    private final List<DataLineCsv> lines = new ArrayList<>();
    private final List<FootnoteCsv> footnotes = new ArrayList<>();
    private final SubjectOrderDefinition ORDER = ZeugnisBuilder.FACH_COMPARATOR;

    public ZensurenListeCsv(String name) {
        this.name = name;
    }

    @Override
    public String getListName() {
        return name;
    }

    @Override
    public void setListName(String lname) {
        this.name = lname;
    }

    @Override
    public String getListDate() {
        return this.ldate;
    }

    @Override
    public void setListDate(String ldate) {
        this.ldate = ldate;
    }

    @Override
    public DataLineCsv addLine(String sName) {
        final DataLineCsv ret = new DataLineCsv(sName);
        lines.add(ret);
        Collections.sort(lines, Comparator.comparing(l -> StudentComparator.sortStringFromDirectoryName(l.name), ZensurenListeXml.COLLATOR));
        return ret;
    }

    @Override
    public ColumnCsv setValue(DataLineCsv line, int tier, Set<Marker> fach, Grade g, String ifGradeNull) {
        MarkerColumnKey ck = new MarkerColumnKey(tier, fach);
        if (g == null && line.map.containsKey(ck)) {
            return null;
        }
        final String label = g != null ? g.getShortLabel() : (ifGradeNull != null ? ifGradeNull : null);
        final Marker min = fach.stream().min(ORDER).get();
        final ColumnCsv ret = new ColumnCsv(label, tier, ORDER.positionOf(min));
        line.map.put(ck, ret);
        return ret;
    }

    @Override
    public FootnoteCsv addFootnote(String text) {
        final FootnoteCsv ret = new FootnoteCsv(text);
        footnotes.add(ret);
        return ret;
    }

    public byte[] toString(final String enc) throws UnsupportedEncodingException {
        final List<MarkerColumnKey> cols = this.lines.stream()
                .flatMap(l -> l.map.keySet().stream())
                .sorted(Comparator.comparing(ColumnKey::getTier))
                .sorted(Comparator.comparing(c -> ORDER.positionOf(c.comparingMarker(ORDER))))
                .distinct()
                .collect(Collectors.toList());
        final StringBuilder sb = new StringBuilder();
        final StringJoiner sj = new StringJoiner(";", "", "\n");
        sj.add("Name");
        cols.stream()
                .map(c -> mapToColumn(c))
                .forEach(sj::add);
        sj.add("Bemerkungen");
        sb.append(sj.toString());
        for (final DataLineCsv l : lines) {
            final StringJoiner sjj = new StringJoiner(";", "", "\n");
            String sName = l.name;
            if (l.studentHint != null) {
                sName += " (" + l.studentHint + ")";
            }
            sjj.add(sName);
            cols.stream()
                    .map(c -> l.get(c))
                    .forEach(sjj::add);
            if (l.note != null) {
                sjj.add(l.note);
            } else {
                sjj.add("");
            }
            sb.append(sjj.toString());
        }
        return sb.toString().getBytes(enc);
    }

    private String mapToColumn(MarkerColumnKey key) throws IllegalArgumentException {
        final String fName = key.marker.size() == 1 ? key.marker.iterator().next().getLongLabel() : key.marker.stream()
                .sorted(ORDER)
                .map(Marker::getLongLabel)
                .collect(NdsReportBuilderFactory.SUBJECT_JOINING_COLLECTOR);
        final StringJoiner sj = new StringJoiner(" ");
        if (key.tier == 1) {
            sj.add(StudentDetailsXml.WPK);
        }
        sj.add(fName);
        return sj.toString();
    }

    public void removeEmptyColumns() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public class ColumnCsv implements Column<FootnoteCsv> {

        private final String value;
        private final int tier;
        private final int order;
        private String level;
        private FootnoteCsv footnote;

        private ColumnCsv(String value, int tier, int pos) {
            this.value = value;
            this.tier = tier;
            this.order = pos;
        }

        @Override
        public String getLevel() {
            return level;
        }

        @Override
        public void setLevel(String level) {
            this.level = level;
        }

        public FootnoteCsv getFootnote() {
            return footnote;
        }

        @Override
        public void setFootnote(FootnoteCsv footnote) {
            this.footnote = footnote;
        }

    }

    public class DataLineCsv implements DataLine {

        private final String name;
        private String studentHint;
        private String note;
        private final Map<MarkerColumnKey, ColumnCsv> map = new HashMap<>();

        private DataLineCsv(String name) {
            this.name = name;
        }

        @Override
        public String getStudentHint() {
            return studentHint;
        }

        @Override
        public void setStudentHint(String studentHint) {
            this.studentHint = studentHint;
        }

        @Override
        public String getNote() {
            return note;
        }

        @Override
        public void setNote(String note) {
            this.note = note;
        }

        private String get(MarkerColumnKey c) {
            final ColumnCsv col = map.get(c);
            if (col == null) {
                return "";
            }
            final StringJoiner sj = new StringJoiner(" ");
            if (col.level != null) {
                sj.add(col.level);
            }
            sj.add(col.value != null ? col.value : "");
            if (col.footnote != null) {
                sj.add(col.footnote.getValue());
            }
            return sj.toString();
        }

    }

    public class FootnoteCsv implements Footnote {

        private String value;

        private FootnoteCsv(String text) {
            this.value = text;
        }

        @Override
        public String getIndex() {
            final int i = ZensurenListeCsv.this.footnotes.indexOf(this);
            if (i < 1) {
                return null;
            }
            return Integer.toString(i);
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public void setValue(String value) {
            this.value = value;
        }

    }
}
