/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank;

import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
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
//    @Deprecated
//    public Term getCurrentTerm();

    public void checkAssoziationen(SiBankAssoziationenCollection assoziationen);

//    @Deprecated
//    public void initSelectTerms(DefaultComboBoxModel model, int refjahr);
    public UnitId initPreferredTarget(int stufe, Marker fach, String kursnr, int rjahr);

    public UnitId initPreferredPrimaryUnitId(String resolvedName, int referenzjahr);

//    @Deprecated
//    public BetulaServiceConnector createServiceConnector(WizardDescriptor wiz) throws IOException;
//
//    @Deprecated
//    public Runnable[] createUpdaters(WizardDescriptor wiz);
//    @Deprecated
//    public Class<? extends ImportierterKurs> getImportierterKursImplementationClass();
//    public KursauswahlOutlineModel createKursauswahlOutlineModel();
//
//    public CreateDocumentsTableModel createCreateDocumentsTableModel();
//
//    public ImportStudentsTableModel createImportStudentsTableModel();
//    @Deprecated
//    public <I extends Identity> I createId(Class<I> aClass, String text);
    //TODO: remove when SiBank-Import has become independent of remote lookup!
//    public String getRemoteLookupUrl();
//    public RemoteLookup getRemoteLookup();
//    public Marker[] getAvailableKursart();
//
//    public Marker[] getAvailableFaecher();
    default public SiBankCreateDocumentsTableModel createCreateDocumentsTableModel2() {
        return new SiBankCreateDocumentsTableModel();
    }

    public String getStudentsAuthority(Object... params);

//    public TargetDocumentProperties[] createTargetDocuments(SiBankKursItem lesson);
    public DocumentId getStudentCareersDocumentId();

    public static abstract class Factory extends ImportTargetFactory<SiBankImportTarget> {

        protected Factory() {
            super(SiBankPlus.getProduct(), SiBankImportTarget.class);
        }

    }
}
