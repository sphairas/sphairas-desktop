/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport;

import java.io.File;
import java.io.IOException;
import org.thespheres.betula.tableimport.csv.XmlCsvFile;

/**
 *
 * @author boris.heithecker
 */
public abstract class TableImportConverter {

    private final String mime;
    private final String description;
    private final boolean requireCharsetParameter;

    protected TableImportConverter(String mime, String description, boolean requireCharsetParameter) {
        this.mime = mime;
        this.description = description;
        this.requireCharsetParameter = requireCharsetParameter;
    }

    protected TableImportConverter(String mime, String description) {
        this(mime, description, false);
    }

    public String getFileMimeType() {
        return mime;
    }

    public String getFileTypeDescription() {
        return description;
    }

    public boolean isRequireCharsetParameter() {
        return requireCharsetParameter;
    }

    public abstract XmlCsvFile[] load(final File file, final String charset) throws IOException;
}
