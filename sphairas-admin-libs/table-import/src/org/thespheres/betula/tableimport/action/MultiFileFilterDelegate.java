/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.tableimport.DataImportSettings;
import org.thespheres.betula.tableimport.TableImportConverter;
import org.thespheres.betula.tableimport.TableImportConverterService;
import org.thespheres.betula.tableimport.csv.XmlCsvFile;
import org.thespheres.betula.tableimport.csv.XmlCsvUtil;
import org.thespheres.betula.ui.util.MimeFileFilter;

/**
 *
 * @author boris.heithecker
 */
class MultiFileFilterDelegate extends FileFilter {

    @NbBundle.Messages({"MultiFileFilterDelegate.FileChooser.FileDescription=xml-Dateien",
        "MultiFileFilterDelegate.FileChooser.description=Importierbare Tabellen-Dateien"})
    private final static javax.swing.filechooser.FileFilter BASE_FILE_FILTER = new MimeFileFilter("text/xml",
            NbBundle.getMessage(MultiFileFilterDelegate.class, "MultiFileFilterDelegate.FileChooser.FileDescription"));
    private final Map<String, List<MimeFileFilterExt>> filter;
    private final boolean addDefaultFilter;

    private MultiFileFilterDelegate(final Map<String, List<MimeFileFilterExt>> filter, boolean addDefault) {
        this.filter = filter;
        this.addDefaultFilter = addDefault;
    }

    static MultiFileFilterDelegate create(DataImportSettings.Type[] type, boolean defaultPermitted) {
        final Map<String, List<MimeFileFilterExt>> m = Lookup.getDefault().lookupAll(TableImportConverterService.class).stream()
                .flatMap(s -> s.converters(type).stream())
                .collect(Collectors.groupingBy(TableImportConverter::getFileMimeType, Collectors.mapping(MimeFileFilterExt::new, Collectors.toList())));
        return new MultiFileFilterDelegate(m, defaultPermitted);
    }

    List<FileFilter> allFilters() {
        final List<FileFilter> ret = filter.values().stream()
                .filter(l -> l.size() == 1)
                .map(l -> l.get(0))
                .sorted(Comparator.comparing(MimeFileFilter::getDescription, Collator.getInstance(Locale.getDefault())))
                .collect(Collectors.toList());
        if (addDefaultFilter) {
            ret.add(BASE_FILE_FILTER);
        }
        return ret;
    }

    @Override
    public boolean accept(final File file) {
        final FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) {
            return false;
        }
        final String mime = fo.getMIMEType();
        if (filter.containsKey(mime)) {
            return true;
        }
        return addDefaultFilter ? BASE_FILE_FILTER.accept(file) : false;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(MultiFileFilterDelegate.class, "MultiFileFilterDelegate.FileChooser.description");
    }

    public XmlCsvFile[] load(final File file, final String enc) throws IOException {
        final String mime = getMimeType(file);
        final List<MimeFileFilterExt> l = filter.getOrDefault(mime, Collections.EMPTY_LIST);
        if (l.size() == 1) {
            final TableImportConverter converter = l.get(0).converter;
            return converter.load(file, converter.isRequireCharsetParameter() ? enc : null);
        } else if (l.isEmpty() && addDefaultFilter) {
            return loadDefault(file);
        }
        throw new IOException("Could not load " + file.getAbsolutePath());
    }

    String getMimeType(final File file) throws IOException {
        final FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) {
            throw new IOException("FileObject does not exist.");
        }
        return fo.getMIMEType();
    }

    static XmlCsvFile[] loadDefault(final File file) throws IOException {
        try (final InputStream is = Files.newInputStream(file.toPath())) {
            final Object res = XmlCsvUtil.JAXB.createUnmarshaller().unmarshal(is);
            if (res instanceof XmlCsvFile) {
                return new XmlCsvFile[]{(XmlCsvFile) res};
            } else if (res instanceof XmlCsvFile.XmlCsvFiles) {
                final XmlCsvFile[] files = ((XmlCsvFile.XmlCsvFiles) res).getFiles();
                if (files != null && files.length > 0) {
                    return files;
                }
            }
            throw new IOException("Corrupted file.");
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    static class MimeFileFilterExt extends MimeFileFilter {

        final TableImportConverter converter;

        MimeFileFilterExt(TableImportConverter c) {
            super(c.getFileMimeType(), c.getFileTypeDescription());
            this.converter = c;
        }
    }

}
