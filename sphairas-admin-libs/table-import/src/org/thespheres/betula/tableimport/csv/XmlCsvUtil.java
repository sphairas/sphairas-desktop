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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang3.StringUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.tableimport.csv.XmlCsvDictionary.Entry;
import org.thespheres.betula.tableimport.csv.XmlCsvFile.Column;
import org.thespheres.betula.tableimport.csv.XmlCsvFile.DefaultValue;
import org.thespheres.betula.tableimport.csv.XmlCsvFile.XmlCsvFiles;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class XmlCsvUtil {

    public static JAXBContext JAXB;

    static {
        try {
            JAXB = JAXBContext.newInstance(XmlCsvParserSettings.class, XmlCsvFile.class, XmlCsvFiles.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private XmlCsvUtil() {
    }

    public static XmlCsvFile read(Path source, String encoding, FileObject parserSettings) throws IOException {
//        final BOMInputStream bom = new BOMInputStream(Files.newInputStream(source));
//        final Reader r = Files.newBufferedReader(source, Charset.forName(encoding));
        final XmlCsvParserSettings settings;
        if (parserSettings == null) {
            settings = new XmlCsvParserSettings();
//            final List<String> ll = Files.readAllLines(source, Charset.forName(encoding));
//            final char[] tryChars = {',', ';', '\t'};
//            if (!ll.isEmpty()) {
//                for (final char c : tryChars) {
//                    final int firstLineCount = StringUtils.countMatches(ll.get(0), c);
//                    if (ll.stream().allMatch(l -> StringUtils.countMatches(l, c) == firstLineCount)) {
//                        settings.setSeparatorChar(c);
//                        break;
//                    }
//                };
//            }
        } else {
            try (InputStream is = parserSettings.getInputStream()) {
                settings = (XmlCsvParserSettings) JAXB.createUnmarshaller().unmarshal(is);
            } catch (JAXBException ex) {
                throw new IOException(ex);
            }
        }
        final CsvParser p = settings.createCSVParser();
        return XmlCsvFile.read(source, Charset.forName(encoding), p);
    }

    public static void assignDefaultValues(XmlCsvFile csv, Properties dictionary) {
        final DefaultValue[] dv = dictionary.entrySet().stream()
                .filter(e -> e.getValue() instanceof String && e.getKey() instanceof String)
                .map(e -> new DefaultValue((String) e.getKey(), (String) e.getValue()))
                .toArray(DefaultValue[]::new);
        csv.setDefaultValues(dv);
    }

    public static void assignKeys(XmlCsvFile csv, Properties dictionary) {
        for (Column col : csv.getColumns()) {
            final String lbl = StringUtils.trimToEmpty(col.getLabel()).toLowerCase(Locale.getDefault());
            final String key = (String) dictionary.entrySet().stream()
                    .filter(e -> e.getValue() instanceof String && e.getKey() instanceof String)
                    .filter(e -> checkLabel(lbl, (String) e.getValue()))
                    .collect(CollectionUtil.singleton())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            if (key != null) {
                col.setAssignedKey(key);
            } else {
                final String msg = NbBundle.getMessage(XmlCsvUtil.class, "XmlCsvUtil.assignKeys.message.noKey", col.getLabel());
                PlatformUtil.getCodeNameBaseLogger(XmlCsvUtil.class).log(Level.FINE, msg);
            }
        }
    }

    @Messages({"XmlCsvUtil.assignKeys.message.noKey=Kein Schlüssel für die Spalte {0} gefunden."})
    public static void assignKeys(XmlCsvFile csv, XmlCsvDictionary dictionary) {
        for (Column col : csv.getColumns()) {
            final String lbl = StringUtils.trimToEmpty(col.getLabel()).toLowerCase(Locale.getDefault());
            final String key = Arrays.stream(dictionary.getEntries())
                    .filter(e -> checkLabel(lbl, e.getValue()))
                    .collect(CollectionUtil.singleton())
                    .map(Entry::getAssignedKey)
                    .orElse(null);
            if (key != null) {
                col.setAssignedKey(key);
            } else {
                final String msg = NbBundle.getMessage(XmlCsvUtil.class, "XmlCsvUtil.assignKeys.message.noKey", col.getLabel());
                PlatformUtil.getCodeNameBaseLogger(XmlCsvUtil.class).log(Level.FINE, msg);
            }
        }
    }

    private static boolean checkLabel(final String lbl, final String dictValue) {
        final String value;
        if ((value = StringUtils.trimToNull(dictValue)) == null) {
            return false;
        }
        final String[] lbls = value.toLowerCase(Locale.getDefault()).split(",");
        for (String label : lbls) {
            boolean matches;
            if (label.endsWith("*")) {
                matches = lbl.startsWith(label.substring(0, label.length() - 2));
            } else {
                matches = lbl.equals(label);
            }
            if (matches) {
                return true;
            }
        }
        return false;
    }
}
