/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.csv;

import com.univocity.parsers.csv.CsvParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.io.input.BOMInputStream;
import org.thespheres.betula.xmlimport.model.Product;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "xml-csv-file") //, namespace = "http://www.thespheres.org/xsd/betula/csv-import.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlCsvFile {

    @XmlAttribute(name = "id")
    private String id;
    @XmlElementWrapper(name = "products")
    @XmlElement(name = "product")
    private final List<Product> products = new ArrayList<>();
    @XmlElementWrapper(name = "columns")
    @XmlElement(name = "column")
    private Column[] cols;
    @XmlElementWrapper(name = "lines")
    @XmlElement(name = "line")
    private Line[] lines;
    @XmlElementWrapper(name = "default-values")
    @XmlElement(name = "value")
    private DefaultValue[] defaultValues;
    @XmlElementWrapper(name = "grouping-keys")
    @XmlElement(name = "grouping-key")
    private XmlCsvFileGroupingKey[] keys;
    private XmlCsvDictionary dictionary;

    public XmlCsvFile() {
    }

    public String getId() {
        return id;
    }

    public XmlCsvFile(Column[] cols, Line[] lines) {
        this.cols = cols;
        this.lines = lines;
    }

    public Column[] getColumns() {
        return cols != null ? cols : new Column[0];
    }

    public Line[] getLines() {
        return lines != null ? lines : new Line[0];
    }

    public DefaultValue[] getDefaultValues() {
        return defaultValues;
    }

    public void setDefaultValues(DefaultValue[] defaultValues) {
        this.defaultValues = defaultValues;
    }

    void setKeys(XmlCsvFileGroupingKey[] key) {
        this.keys = key;
    }

    XmlCsvFileGroupingKey[] getKeys() {
        return keys;
    }

    public XmlCsvDictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(XmlCsvDictionary dictionary) {
        this.dictionary = dictionary;
    }

    public static XmlCsvFile read(final Path file, final Charset enc, final CsvParser parser) throws IOException {
//        final CSVReader csv = new CSVReader(reader, 0, parser);
        final List<String[]> lines;
        try (final InputStream is = new BOMInputStream(Files.newInputStream(file))) {
            lines = parser.parseAll(is, enc);//  csv.readAll();
        }
        if (lines.isEmpty()) {
            throw new IOException("Empty csv-file.");
        }
        final String[] columns = lines.get(0);
        final Column[] cols = new Column[columns.length];
        for (int ci = 0; ci < columns.length; ci++) {
            final String id = org.thespheres.betula.util.Utilities.createId(ci);
            cols[ci] = new Column(id, columns[ci]);
        }
        final Line[] lns = new Line[lines.size() - 1];
        for (int c = 1; c < lines.size(); c++) {
            final String[] line = lines.get(c);
            if (line.length != cols.length) {
                throw new IOException("Columns size and line length mismatch.");
            }
            Value[] lv = new Value[line.length];
            for (int ci = 0; ci < line.length; ci++) {
                Column col = cols[ci];
                lv[ci] = new Value(col, line[ci]);
            }
            lns[c - 1] = new Line(lv);
        }
        return new XmlCsvFile(cols, lns);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Column {

        @XmlID
        @XmlAttribute(name = "column-id", required = true)
        private String id;
        @XmlElement(name = "display-label", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        private String label;
        @XmlAttribute(name = "assigned-key")
        private String assignedKey;

        public Column() {
        }

        public Column(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public String getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public String getAssignedKey() {
            return assignedKey;
        }

        public void setAssignedKey(String assignedKey) {
            this.assignedKey = assignedKey;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Line {

        @XmlElement(name = "value")
        private Value[] values;
        @XmlIDREF
        @XmlAttribute(name = "grouping-key")
        private XmlCsvFileGroupingKey key;

        public Line() {
        }

        public Line(Value[] values) {
            this.values = values;
        }

        public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
            values = Arrays.stream(values)
                    .filter(v -> v.column != null)
                    .toArray(Value[]::new);
        }

        public Value[] getValues() {
            return values != null ? values : new Value[0];
        }

        void setGroupingKey(XmlCsvFileGroupingKey key) {
            this.key = key;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public final static class DefaultValue {

        @XmlAttribute(name = "assigned-key")
        private String assignedKey;
        @XmlValue
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        private String value;

        public DefaultValue() {
        }

        public DefaultValue(String key, String value) {
            this.assignedKey = key;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String getAssignedKey() {
            return assignedKey;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public final static class Value {

        @XmlIDREF
        @XmlAttribute(name = "column-id", required = true)
        private Column column;
        @XmlValue
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        private String value;

        public Value() {
        }

        public Value(Column column, String value) {
            this.column = column;
            this.value = value;
        }

        public Column getColumn() {
            return column;
        }

        public String getValue() {
            return value;
        }
    }

    @XmlRootElement(name = "xml-csv-files") //, namespace = "http://www.thespheres.org/xsd/betula/csv-import.xsd")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlCsvFiles {

//        @XmlElementRef
        @XmlElement(name = "xml-csv-file")
        private XmlCsvFile[] files;

        public XmlCsvFiles() {
        }

        public XmlCsvFiles(XmlCsvFile[] files) {
            this.files = files;
        }

        public XmlCsvFile[] getFiles() {
            return files;
        }

    }
}
