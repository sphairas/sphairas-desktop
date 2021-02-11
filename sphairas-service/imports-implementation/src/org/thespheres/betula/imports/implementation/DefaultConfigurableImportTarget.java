/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.imports.implementation;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.*;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetDocument;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.scheme.spi.Scheme;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.services.ws.CommonDocuments;
import org.thespheres.betula.sibank.SiBankAssoziationenCollection;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.model.XmlTargetImportSettings;
import org.thespheres.betula.xmlimport.parse.ImportScripts;
import org.thespheres.betula.xmlimport.parse.TranslateID;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;

/**
 *
 * @author boris.heithecker
 */
public class DefaultConfigurableImportTarget extends ConfigurableImportTarget implements SiBankImportTarget, UntisImportConfiguration {

    private final XmlTargetImportSettings importSettings;
    private final XmlTargetProcessorHintsSettings processorHints;
    private DocumentId careersDocument;
    private String untisHref;
    private final String davBase;

    public DefaultConfigurableImportTarget(String provider, Product prod, XmlTargetImportSettings settings, XmlTargetProcessorHintsSettings hints) {
        super(provider, prod);
        importSettings = settings;
        processorHints = hints;
        this.davBase = URLs.adminResourcesDavBase(LocalProperties.find(provider));
    }

    @Override
    public void initialize(final Map<String, String> properties) {
        super.initialize(properties);
        final String careers = properties.get(LocalFileProperties.PROP_STUDENT_CAREER_CONVENTIONS);
        if (!StringUtils.isBlank(careers)) {
            this.studentCareerConventions = careers.split(",");
        }
        String careersDocId = properties.get("bildungsgang.document.id");
        if (!StringUtils.isBlank(careersDocId)) {
            this.careersDocument = new DocumentId(getAuthority(), careersDocId, DocumentId.Version.LATEST);
        }
    }

    void setScripts(final ImportScripts scripts) {
        this.importScripts = scripts;
    }

    void setSourceTargetLinksWebDavUrl(String sourceTargetLinksWebDavUrl) {
        this.sourceTargetLinksWebDavUrl = sourceTargetLinksWebDavUrl;
    }

    void setSourceTargetLinksConfigFile(String sourceTargetLinksConfigFile) {
        this.sourceTargetLinksConfigFile = sourceTargetLinksConfigFile;
    }

    String getDavBase() {
        return davBase;
    }

    @Override
    public TargetDocumentProperties[] createTargetDocuments(ImportTargetsItem lesson) {
        final XmlTargetImportSettings.TargetDefault[] targetDefaults = importSettings.getTargetDefaults(lesson);
        return Arrays.stream(targetDefaults)
                .map(xtd -> DefaultTargetDocument.create(xtd.getTargetType(), lesson, xtd.getPreferredConvention(), importSettings, processorHints.getTargetDefaults(xtd, lesson), isTextValueTarget(xtd.getTargetType())))
                .toArray(TargetDocumentProperties[]::new);
    }

    boolean isTextValueTarget(final String targetType) {
        return Optional.ofNullable(this.properties.get("text.target.types"))
                .map(v -> Set.of(v.split(",")))
                .map(v -> v.contains(targetType))
                .orElse(false);
    }

    @Override
    public Grade getDefaultValue(DocumentId id, TargetDocument document) {
        return importSettings.getDefaultValue(id, document);
    }

    @Override
    public void checkAssoziationen(SiBankAssoziationenCollection assoziationen) {
        final SchemeProvider sp = getTermSchemeProvider();
        if (sp != null) {
            TermSchedule ts = sp.getScheme(Scheme.DEFAULT_SCHEME, TermSchedule.class);
            if (ts != null) {
                Term currentTerm = ts.getCurrentTerm();
                int hj = (int) currentTerm.getParameter("halbjahr");
//            if (assoziationen.get(currentTerm.getScheduledItemId()).isEmpty() && hj == 2) {
//                Term past = Terms.getTerm((int) currentTerm.getParameter(Terms.JAHR), 1);
//                Set<ImportierterKurs> pastAssoz = assoziationen.get(past.getScheduledItemId());
//                assoziationen.get(currentTerm.getScheduledItemId()).addAll(pastAssoz);
//            }
            }
        }
    }

    @Override
    public UnitId initPreferredTarget(int stufe, Marker fach, String kursnr, int rjahr) {
//        return TranslateID.findId(stufe, rjahr, fach, kursnr, "kgs");
        final String kursid = kursnr != null ? kursnr.substring(1) : null;
        String uid;
        if (stufe == 10 && false) {
            uid = TranslateID.findId(stufe, rjahr, 10, false, fach, kursid, "kgs");
        } else if (stufe >= 11) {
            uid = TranslateID.findAbiturId(stufe - 10, rjahr, fach, kursnr, "kgs");
        } else {
            uid = TranslateID.findId(stufe, rjahr, fach, kursid, "kgs");
        }
        UnitId ret = new UnitId(getAuthority(), uid);
        return ret;
    }

    @Override //TODO use TranslateID
    public UnitId initPreferredPrimaryUnitId(final String resolvedName, final int referenzjahr) {
        String uid;
        int jahr;
        boolean jg10 = false;
        if (resolvedName.startsWith("Q") && resolvedName.length() == 2) {
            int qp = Integer.parseInt(resolvedName.substring(1, 2));
            String id = "kgs-abitur" + Integer.toString(referenzjahr + +3 - qp);
            return new UnitId(authority, id);
        } else if (resolvedName.startsWith("11.") || resolvedName.startsWith("12.")) {
            int qp = Integer.parseInt(resolvedName.substring(4, 5));
            String id = "kgs-abitur" + Integer.toString(referenzjahr + 3 - qp);
            return new UnitId(authority, id);
        } else if (resolvedName.startsWith("10") && false) {
            jg10 = true;
            uid = resolveJg10uid(resolvedName);
            jahr = referenzjahr;
        } else {
            uid = resolvedName.startsWith("10") ? resolvedName.toLowerCase(Locale.GERMANY).substring(2) : resolvedName.toLowerCase(Locale.GERMANY).substring(1);
            int stufe = resolvedName.startsWith("10") ? 10 : Integer.parseInt(resolvedName.substring(0, 1));
            jahr = referenzjahr - (stufe - 5);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("kgs-klasse-").append(Integer.toString(jahr)).append("-");
        if (jg10) {
            sb.append("jg10-");
        }
        sb.append(uid);
        return new UnitId(authority, sb.toString());
    }

    private static String resolveJg10uid(String resolvedName) {
        int index = 2;
        if (resolvedName.length() > 2 && resolvedName.charAt(index) == '.') {
            index++;
        }
        String uid = resolvedName.substring(index).toLowerCase(Locale.GERMANY);
        if (!uid.startsWith("e")) {
            uid = "hr" + uid;
        }
        return uid;
    }

    @Override
    public DocumentId forName(final String name) {
        switch (name) {
            case CommonDocuments.STUDENT_CAREERS_DOCID:
                return careersDocument;
        }
        return null;
    }

    @Override
    public String getUntisXmlDocumentUploadHref() {
        return untisHref;
    }

    public void setUntisXmlDocumentUploadHref(String untisHref) {
        this.untisHref = untisHref;
    }

}
