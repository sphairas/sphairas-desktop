/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis;

import org.thespheres.betula.gpuntis.ui.UntisCreateDocumentsTableModel;
import org.thespheres.betula.services.CommonStudentProperties;
import org.thespheres.betula.services.CommonTargetProperties;
import org.thespheres.betula.services.ws.CommonDocuments;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportTargetFactory;

/**
 *
 * @author boris.heithecker
 */
public interface UntisImportConfiguration extends ImportTarget, CommonTargetProperties, CommonDocuments, CommonStudentProperties {

    public static final String LINKS_FILENAME = "untis-assoziationen.xml";
    public static final String UNTIS_XML_RESOURCE = "untis.xml";

    default public UntisCreateDocumentsTableModel createUntisCreateDocumentsTableModel() {
        return new UntisCreateDocumentsTableModel();
    }

    public String getDefaultSigneeSuffix();

    public String getUntisXmlDocumentResource();

    public static abstract class Factory extends ImportTargetFactory<UntisImportConfiguration> {

        protected Factory() {
            super(Untis.getProduct(), UntisImportConfiguration.class);
        }

    }
}
