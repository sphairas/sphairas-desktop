/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.imports.implementation;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.model.XmlTargetImportSettings;
import org.thespheres.betula.xmlimport.utilities.ImportConfigurationException;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;

/**
 *
 * @author boris.heithecker
 */
class DefaultTargetDocument extends TargetDocumentProperties {

    private final XmlTargetImportSettings defaultGrades;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    DefaultTargetDocument(DocumentId id, ImportTargetsItem imp, String targetType, boolean text, String preferredConvention, XmlTargetImportSettings settings, Map<String, String> hints) {
        super(id, imp.allMarkers(), targetType, text, preferredConvention, imp.getDeleteDate());
        this.defaultGrades = settings;
        getProcessorHints().putAll(hints);
//        getProcessorHints().put("process-bulk", "true");
//        if (!isAG(imp)) {
//            getProcessorHints().put("update-pu-links", "true");
//        }
        getSignees().put("entitled.signee", imp.getSignee());
    }

    @Messages({"DefaultTargetDocument.create.missingAssessmentConvention.message=Für den Import der Liste \"{0}\" konnte keine Notensystem-Vorgabe gefunden werden."})
    public static DefaultTargetDocument create(final String suffix, ImportTargetsItem imp, String preferredConvention, XmlTargetImportSettings settings, Map<String, String> hints, boolean text) {
        //TODO: really override imp.getPreferredConvention ???? 
        AssessmentConvention gv = null;
        if (preferredConvention != null) {
            gv = GradeFactory.findConvention(preferredConvention);
        } else if (!text) {
            gv = GradeFactory.findConvention(imp.getPreferredConvention());
        }
        if (gv == null && !text) {
            final String msg = NbBundle.getMessage(DefaultTargetDocument.class, "DefaultTargetDocument.create.missingAssessmentConvention.message", imp.getSourceNodeLabel());
            throw new ImportConfigurationException(msg);
        }
        DocumentId baseId = imp.getTargetDocumentIdBase();
        if (baseId.getId().endsWith(suffix)) {
            throw new IllegalArgumentException("Suffix already added.");
        }
        DocumentId id = new DocumentId(baseId.getAuthority(), baseId.getId() + "-" + suffix, baseId.getVersion());
        return new DefaultTargetDocument(id, imp, StringUtils.capitalize(suffix), text, gv != null ? gv.getName() : null, settings, hints);
    }

    @Override
    public boolean isFragment() {
        return false;
    }

    @Override
    public Grade getDefaultGrade() {
        //TODO: use NdsDefaultproperties and XmlDefaultGrades
//        return DEFAULT_GRADES.getDefaultValue(getDocument(), this);
        return defaultGrades.getDefaultValue(getDocument(), this);
    }

    @Override
    public String getDefaultText() {
        return defaultGrades.getDefaultText(getDocument(), this);
    }

}
