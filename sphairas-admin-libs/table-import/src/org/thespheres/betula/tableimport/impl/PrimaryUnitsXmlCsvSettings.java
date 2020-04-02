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
import org.thespheres.betula.tableimport.ui.PrimaryUnitUpdateStudentsVisualPanel;
import org.thespheres.betula.tableimport.ui.XmlDataImportConfigVisualPanel;
import org.thespheres.betula.tableimport.ui.XmlUnitDataDocumentsVisualPanel;
import org.thespheres.betula.xmlimport.model.XmlItem;
import org.thespheres.betula.xmlimport.model.XmlTargetItem;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportWizard;

/**
 *
 * @author boris.heithecker
 */
class PrimaryUnitsXmlCsvSettings extends XmlCsvImportSettings<PrimaryUnitsXmlCsvItem> {

    private static Templates XSLT;

    PrimaryUnitsXmlCsvSettings(XmlCsvFile[] file) {
        super(file, true);
    }

    @Override
    public WizardDescriptor.Iterator<XmlCsvImportSettings<PrimaryUnitsXmlCsvItem>> createIterator(final WizardDescriptor.Panel<XmlCsvImportSettings<PrimaryUnitsXmlCsvItem>> firstPanel) {
        return new XmlDataImportActionWizardIterator(firstPanel);
    }

    @Override
    protected synchronized Templates getTemplate() {
        if (XSLT == null) {
            try (InputStream is = PrimaryUnitsXmlCsvSettings.class.getResourceAsStream("table2target.xsl")) {
                XSLT = FACTORY.newTemplates(new StreamSource(is));
            } catch (TransformerConfigurationException | IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return XSLT;
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

    @Override
    protected void configureTransformer(Transformer transformer) {
        super.configureTransformer(transformer);
        transformer.setParameter("student.authority", getConfiguration().getStudentsAuthority());
    }

    @Messages("PrimaryUnitsXmlCsvSettings.createItem.sourceLabel={1} ({2}\u2116{0})")
    @Override
    protected PrimaryUnitsXmlCsvItem createItem(final XmlItem i, final String csvId) {
        if (i instanceof XmlTargetItem) {
            final XmlTargetItem xti = (XmlTargetItem) i;
            String tablePos = "?";
            if (xti.getPosition() != null) {
                tablePos = Integer.toString(xti.getPosition());
            }
            String table = "";
            if (csvId != null) {
                table = csvId + "/";
            }
            final String sourceUnit;
            if (xti.getUnit() != null) {
                sourceUnit = xti.getUnit().getId();
            } else {
                sourceUnit = StringUtils.trimToNull(xti.getSourceUnit());
            }
            if (sourceUnit != null && !sourceUnit.equals("null")) {
                final String label = NbBundle.getMessage(PrimaryUnitsXmlCsvSettings.class,
                        "PrimaryUnitsXmlCsvSettings.createItem.sourceLabel", tablePos, sourceUnit, table);
                return new PrimaryUnitsXmlCsvItem(label, xti);
            }
        }
        return null;

    }

    static class XmlDataImportActionWizardIterator extends AbstractFileImportWizard<XmlCsvImportSettings<PrimaryUnitsXmlCsvItem>> {

        private final WizardDescriptor.Panel<XmlCsvImportSettings<PrimaryUnitsXmlCsvItem>> first;

        XmlDataImportActionWizardIterator(final WizardDescriptor.Panel<XmlCsvImportSettings<PrimaryUnitsXmlCsvItem>> firstPanel) {
            this.first = firstPanel;
        }

        //Vier verschiedene Typen: SuS,Klassen - Kurse - Kurszuordnungen - Lehrer
        @Override
        protected ArrayList<WizardDescriptor.Panel<XmlCsvImportSettings<PrimaryUnitsXmlCsvItem>>> createPanels() {
            final ArrayList<WizardDescriptor.Panel<XmlCsvImportSettings<PrimaryUnitsXmlCsvItem>>> ret = new ArrayList<>();
            if (first != null) {
                ret.add(first);
            }
            ret.add(new XmlDataImportConfigVisualPanel.XmlDataImportConfigPanel());
            ret.add(new ConfigureDictionaryVisualPanel.ConfigureDictionaryPanel());
            ret.add(new XmlUnitDataDocumentsVisualPanel.XmlDataDocumentsPanel());
            ret.add(new PrimaryUnitUpdateStudentsVisualPanel.PrimaryUnitUpdateStudentsPanel());
            return ret;
        }

    }
}
