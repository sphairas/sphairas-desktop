/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.impl;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = Processor.class)
@SupportedAnnotationTypes({"org.thespheres.betula.xmlimport.uiutil.ImportTableColumn.Factory.Registration",
    "org.thespheres.betula.xmlimport.uiutil.ImportTableColumn.Factory.Registrations"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ImportTableColumnLayerGenerator extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(ImportTableColumn.Factory.Registration.class)) {
            final ImportTableColumn.Factory.Registration r = e.getAnnotation(ImportTableColumn.Factory.Registration.class);
            writeOne(r, e);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(ImportTableColumn.Factory.Registrations.class)) {
            final ImportTableColumn.Factory.Registrations rr = e.getAnnotation(ImportTableColumn.Factory.Registrations.class);
            for (ImportTableColumn.Factory.Registration r : rr.value()) {
                writeOne(r, e);
            }
        }
        return true;
    }

    protected void writeOne(ImportTableColumn.Factory.Registration r, Element e) throws LayerGenerationException, IllegalArgumentException {
        final String componentPath = r.component() + "/Columns";
        layer(e).instanceFile(componentPath, null, ImportTableColumn.Factory.class)
                .write();
    }

}
