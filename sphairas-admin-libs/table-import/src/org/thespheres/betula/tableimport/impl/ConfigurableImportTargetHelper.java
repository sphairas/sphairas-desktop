/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.regex.Pattern;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.model.XmlUnitItem;
import org.thespheres.betula.xmlimport.parse.NameParser;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.NameParserSettings;

/**
 *
 * @author boris.heithecker
 */
public class ConfigurableImportTargetHelper<I extends AbstractXmlCsvImportItem<? extends XmlUnitItem>> implements VetoableChangeListener {

    XmlCsvImportSettings settings;

    private boolean isGeneratedUnitIdInitialized = false;
    private UnitId generated;
    private final I item;
    private DocumentId targetDocBase;
    private boolean existsUnit;
    private final static Pattern LEVEL_STPLITTER = Pattern.compile("\\D+");

    @SuppressWarnings("LeakingThisInConstructor")
    ConfigurableImportTargetHelper(I item) {
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

    public void setTargetDocBase(DocumentId targetDocBase) {
        this.targetDocBase = targetDocBase;
    }

    synchronized UnitId getGeneratedUnitId() {
        if (!isGeneratedUnitIdInitialized) {
            final Term term = item.getTerm();
            if (term != null) {
                final Object jProp = term.getParameter("jahr");
                if (jProp instanceof Integer) {
                    final String sl = item.getSource().getSourceLevel();
                    Integer level = null;
                    if (sl != null) {
                        try {
                            level = Integer.parseInt(sl);
                        } catch (NumberFormatException nfex) {
                        }
                    }
                    //TODO: use getSourcePrimaryUnit()
                    final String sourceUnit = item.getSource().getSourceUnit();
                    final String sourcePU = item.getSource().getSourcePrimaryUnit();
                    final int rYear = (int) jProp;
                    final NameParser pn2 = createNameParser();
                    if (sourcePU != null) {
                        if (level == null) {
                            generated = pn2.findUnitId(sourcePU, rYear);
                        } else {
                            generated = pn2.findUnitId(sourcePU, rYear, level);
                        }
                    } else {
                        generated = null;
                    }
                    if (generated == null && sourceUnit != null) {
                        final NameParserSettings nps = item.getConfiguration().getNameParserSettings();
                        final String name = nps.prepareSourceUnitName(sourceUnit);
                        if (level == null) {
                            generated = pn2.findUnitId(name, item.getSubjectMarker(), rYear);
                        } else {
                            generated = pn2.findUnitId(name, item.getSubjectMarker(), rYear, level);
                        }
                    }
                }
            }
            isGeneratedUnitIdInitialized = true;
        }
        return generated;
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

    Integer findLevel() {
        final String sl = item.getSource().getSourceLevel();
        final String su = item.getSource().getSourceUnit();
        if (sl != null) {
            try {
                return Integer.parseInt(sl);
            } catch (NumberFormatException nfex) {
            }
        } else if (su != null) {
            final String[] s = LEVEL_STPLITTER.split(su);
            if (s.length > 0) {
                try {
                    return Integer.parseInt(s[0]);
                } catch (NumberFormatException nfex) {
                }
            }
        }
        return null;
    }

    public String getUnitDisplayName() {
        final UnitId u = item.getUnitId();
        final ConfigurableImportTarget cfg = item.getConfiguration();
        if (u != null && cfg != null) {
            final NamingResolver naming = cfg.getNamingResolver();
            try {
                return naming.resolveDisplayName(u, item.getTerm());
            } catch (IllegalAuthorityException ex) {
            }
            return u.getId();
        }
        return null;
    }

    public boolean existsUnit() {
        return existsUnit;
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        final String pn = evt.getPropertyName();
        if (ImportTargetsItem.PROP_UNIQUE_SUBJECT.equals(pn)
                || TargetItemsXmlCsvItem.PROP_TARGET_ID.equals(pn)
                || TargetItemsXmlCsvItem.PROP_IMPORT_TARGET.equals(pn)
                || TargetItemsXmlCsvItem.PROP_UNITID.equals(pn)) {
            final ConfigurableImportTarget cfg = item.getConfiguration();
            if (cfg != null) {
                isGeneratedUnitIdInitialized = false;
                targetDocBase = null;
                //Must be called after config field reset
                existsUnit = Units.get(cfg.getWebServiceProvider().getInfo().getURL())
                        .map(u -> u.hasUnit(item.getUnitId()))
                        .orElse(Boolean.FALSE);
            }
        }
    }
}
