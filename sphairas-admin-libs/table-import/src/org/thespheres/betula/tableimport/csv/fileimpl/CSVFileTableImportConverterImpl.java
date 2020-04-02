/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.csv.fileimpl;

import java.io.File;
import java.io.IOException;
import org.openide.util.NbBundle;
import org.thespheres.betula.tableimport.TableImportConverter;
import org.thespheres.betula.tableimport.csv.XmlCsvFile;
import org.thespheres.betula.tableimport.csv.XmlCsvUtil;

/**
 *
 * @author boris.heithecker
 */
class CSVFileTableImportConverterImpl extends TableImportConverter {

    CSVFileTableImportConverterImpl() {
        super(CSVFileDataObject.MIME, NbBundle.getMessage(CSVFileDataObject.class, "CSVFileDataObject.displayName"), true);
    }

    @Override
    public XmlCsvFile[] load(final File file, final String charset) throws IOException {
        //TODO: wizard to select charset and parser settings
//        Charset.forName("ISO-8859-1");
//        final XmlCsvFile csv = XmlCsvUtil.read(file.toPath(), "ISO-8859-1", null);
        final XmlCsvFile csv = XmlCsvUtil.read(file.toPath(), charset, null);
        return new XmlCsvFile[]{csv};
    }

}
