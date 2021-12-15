/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank;

import org.thespheres.betula.services.CommonStudentProperties;
import org.thespheres.betula.services.CommonTargetProperties;
import org.thespheres.betula.services.ws.CommonDocuments;
import org.thespheres.betula.sibank.ui2.SiBankCreateDocumentsTableModel;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportTargetFactory;

/**
 *
 * @author boris.heithecker
 */
public interface SiBankImportTarget extends ImportTarget, CommonTargetProperties, CommonStudentProperties, CommonDocuments {

    public static final String LINKS_FILENAME = "sibank-assoziationen.xml";

    public void checkAssoziationen(SiBankAssoziationenCollection assoziationen);

    default public SiBankCreateDocumentsTableModel createCreateDocumentsTableModel2() {
        return new SiBankCreateDocumentsTableModel();
    }

    public String getStudentsAuthority(Object... params);
    
    public boolean permitAltSubjectNames();

    public static abstract class Factory extends ImportTargetFactory<SiBankImportTarget> {

        protected Factory() {
            super(SiBankPlus.getProduct(), SiBankImportTarget.class);
        }

    }
}
