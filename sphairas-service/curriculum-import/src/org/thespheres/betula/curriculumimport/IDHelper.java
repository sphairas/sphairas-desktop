package org.thespheres.betula.curriculumimport;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.parse.NameParser;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris
 */
class IDHelper implements VetoableChangeListener {

    private final StundentafelImportTargetsItem item;
    private DocumentId targetDocBase;

    @SuppressWarnings("LeakingThisInConstructor")
    IDHelper(final StundentafelImportTargetsItem item) {
        item.addVetoableChangeListener(this);
        this.item = item;
    }

    public DocumentId getTargetDocumentIdBase() {
        if (targetDocBase == null) {
            final UnitId u = item.getUnitId();
            if (u != null) {
                final NameParser pn2 = createNameParser();
                targetDocBase = pn2.translateUnitIdToTargetDocumentBase(u.getId(), item.getSubjectMarker(), null);
//            final String id = TranslateID.translateUnitToTarget(u.getId(), item.getSubjectMarker(), (String) null); //item.getCustomDocumentIdIdentifier());
            }
        }
        return targetDocBase;
    }

    public void setTargetDocumentBase(DocumentId targetDocBase) {
        this.targetDocBase = targetDocBase;
    }

    protected NameParser createNameParser() {
        final NamingResolver nr = item.getConfiguration().getNamingResolver();
        final String first = nr.properties().get("first-element");
        final String bl = nr.properties().get("base-level");
        Integer baseLevel = null;
        if (bl != null) {
            try {
                baseLevel = Integer.parseInt(bl);
            } catch (NumberFormatException nfex) {
            }
        }
        final NameParser pn2 = new NameParser(item.getConfiguration().getAuthority(), first, baseLevel);
        return pn2;
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        final String pn = evt.getPropertyName();
        if (ImportTargetsItem.PROP_UNIQUE_SUBJECT.equals(pn)
                || StundentafelImportTargetsItem.PROP_TARGET_ID.equals(pn)
                || StundentafelImportTargetsItem.PROP_IMPORT_TARGET.equals(pn)
                || StundentafelImportTargetsItem.PROP_UNITID.equals(pn)) {
            final ConfigurableImportTarget cfg = item.getConfiguration();
            if (cfg != null) {
//                isGeneratedUnitIdInitialized = false;
                targetDocBase = null;
                //Must be called after config field reset
//                existsUnit = Units.get(cfg.getWebServiceProvider().getInfo().getURL())
//                        .map(u -> u.hasUnit(item.getUnitId()))
//                        .orElse(Boolean.FALSE);
            }
        }
    }
}
