/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this XSLT file, choose Tools | Templates
 * and open the XSLT in the editor.
 */
package org.thespheres.betula.tableimport.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.lang3.StringUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;
import org.thespheres.betula.tableimport.csv.XmlCsvDictionary;
import org.thespheres.betula.tableimport.csv.XmlCsvFile;
import org.thespheres.betula.tableimport.ui.ConfigureDictionaryVisualPanel;
import org.thespheres.betula.tableimport.ui.XmlDataImportConfigVisualPanel;
import org.thespheres.betula.tableimport.ui.XmlTargetDataDocumentsVisualPanel;
import org.thespheres.betula.xmlimport.model.XmlItem;
import org.thespheres.betula.xmlimport.model.XmlTargetItem;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportWizard;

/**
 *
 * @author boris.heithecker
 */
class TargetItemsXmlCsvSettings extends XmlCsvImportSettings<TargetItemsXmlCsvItem> {

    private static Templates XSLT;

    TargetItemsXmlCsvSettings(XmlCsvFile[] file) {
        super(file, false);
    }

    @Override
    public boolean allowSelectUseGrouping() {
        return true;
    }

    @Override
    public WizardDescriptor.Iterator<XmlCsvImportSettings<TargetItemsXmlCsvItem>> createIterator(final WizardDescriptor.Panel<XmlCsvImportSettings<TargetItemsXmlCsvItem>> first) {
        return new XmlDataImportActionWizardIterator(first);
    }

    @Override
    protected synchronized Templates getTemplate() {
        if (XSLT == null) {
            try (InputStream is = TargetItemsXmlCsvSettings.class.getResourceAsStream("table2target.xsl")) {
                XSLT = FACTORY.newTemplates(new StreamSource(is));
            } catch (TransformerConfigurationException | IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return XSLT;
    }

    @Override
    protected void configureTransformer(final Transformer transformer) {
        super.configureTransformer(transformer);
        transformer.setParameter("student.authority", getConfiguration().getStudentsAuthority());
        transformer.setParameter("entries", Boolean.TRUE);
    }

    @Override
    public synchronized XmlCsvDictionary createDictionary() throws IOException {
        final FileObject d = FileUtil.getConfigFile("/Imports/default-dictionary.xml");
        try (final InputStream is = d.getInputStream()) {
            return (XmlCsvDictionary) DICTJAXB.createUnmarshaller().unmarshal(is);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    @Messages({"TargetItemsXmlCsvSettings.createItem.sourceLabel={1} ({2}\u2116 {0})",
        "TargetItemsXmlCsvSettings.createItemWithSubject.sourceLabel={1} {3} ({2}\u2116 {0})"})
    @Override
    protected TargetItemsXmlCsvItem createItem(final XmlItem i, final String csvId) {
        if (i.getPosition() == null) {
            return null;
        }
        final String tablePos = Integer.toString(i.getPosition());
        final String table;
        if (csvId != null) {
            table = csvId + "/";
        } else {
            table = "";
        }
        final String sourceLabel = StringUtils.trimToNull(i.getSourceLabel());
        if (i instanceof XmlTargetItem) {
            final XmlTargetItem xti = (XmlTargetItem) i;
            final String sourceUnit;
            if (xti.getUnit() != null) {
                sourceUnit = xti.getUnit().getId();
            } else if (!StringUtils.isBlank(xti.getSourceUnit())) {
                sourceUnit = StringUtils.trimToNull(xti.getSourceUnit());
            } else {
                sourceUnit = StringUtils.trimToNull(xti.getSourcePrimaryUnit());
            }
            if (sourceUnit != null && !sourceUnit.equals("null")) {
                final String label;
                if (StringUtils.isBlank(xti.getSourceSubject())) {
                    label = NbBundle.getMessage(TargetItemsXmlCsvSettings.class,
                            "TargetItemsXmlCsvSettings.createItem.sourceLabel", tablePos, sourceUnit, table);
                } else {
                    label = NbBundle.getMessage(TargetItemsXmlCsvSettings.class,
                            "TargetItemsXmlCsvSettings.createItemWithSubject.sourceLabel", tablePos, sourceUnit, table, xti.getSourceSubject());
                }
                return new TargetItemsXmlCsvItem(label, xti, false);
            } else if (useGrouping() && sourceLabel != null) {
                final String label = NbBundle.getMessage(TargetItemsXmlCsvSettings.class,
                        "TargetItemsXmlCsvSettings.createItem.sourceLabel", tablePos, sourceLabel, table);
                return new TargetItemsXmlCsvItem(label, xti, true);
            }
        }
        return null;

    }

    static class XmlDataImportActionWizardIterator extends AbstractFileImportWizard<XmlCsvImportSettings<TargetItemsXmlCsvItem>> {

        private WizardDescriptor.Panel<XmlCsvImportSettings<TargetItemsXmlCsvItem>> first;

        XmlDataImportActionWizardIterator(final WizardDescriptor.Panel<XmlCsvImportSettings<TargetItemsXmlCsvItem>> first) {
            this.first = first;
        }

        //Vier verschiedene Typen: SuS,Klassen - Kurse - Kurszuordnungen - Lehrer
        @Override
        protected ArrayList<WizardDescriptor.Panel<XmlCsvImportSettings<TargetItemsXmlCsvItem>>> createPanels() {
            final ArrayList<WizardDescriptor.Panel<XmlCsvImportSettings<TargetItemsXmlCsvItem>>> ret = new ArrayList<>();
            if (first != null) {
                ret.add(first);
            }
            ret.add(new XmlDataImportConfigVisualPanel.XmlDataImportConfigPanel());
            ret.add(new ConfigureDictionaryVisualPanel.ConfigureDictionaryPanel());
            ret.add(new XmlTargetDataDocumentsVisualPanel.XmlDataDocumentsPanel());
            return ret;
        }

    }
}
