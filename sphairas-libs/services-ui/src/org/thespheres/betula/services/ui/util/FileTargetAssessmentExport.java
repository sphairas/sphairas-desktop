/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.thespheres.betula.Unit;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.TermSchedule;

/**
 *
 * @author boris.heithecker
 */
public class FileTargetAssessmentExport extends TargetAssessmentExport {

    private FileObject folder;
    private String filename;

    public FileTargetAssessmentExport(String provider, TargetAssessment<Grade, ?> assessment, Unit unit, DocumentId target, TermSchedule ts, NamingResolver nr, LocalProperties lp) {
        super(provider, assessment, unit, target, ts, nr, lp);
    }

    public void setFile(FileObject destfolder, String name) {
        this.folder = destfolder;
        this.filename = name;
    }

    @Override
    protected void write(Container container) throws IOException {
        ContainerUtil.write(container, folder, filename, getSignAlias());
    }
    //        tae.getHints().putAll(td.getProcessorHints());

}
