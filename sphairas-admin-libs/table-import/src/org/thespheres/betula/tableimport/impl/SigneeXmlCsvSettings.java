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
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;
import org.thespheres.betula.tableimport.csv.XmlCsvDictionary;
import org.thespheres.betula.tableimport.csv.XmlCsvFile;
import org.thespheres.betula.tableimport.ui.ConfigureDictionaryVisualPanel;
import org.thespheres.betula.tableimport.ui.XmlDataImportConfigVisualPanel;
import org.thespheres.betula.xmlimport.model.XmlItem;
import org.thespheres.betula.xmlimport.model.XmlSigneeItem;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportWizard;

/**
 *
 * @author boris.heithecker
 */
class SigneeXmlCsvSettings extends XmlCsvImportSettings<SigneeXmlCsvItem> {

    private static Templates XSLT;

    SigneeXmlCsvSettings(XmlCsvFile[] file) {
        super(file, false);
    }

    @Override
    public WizardDescriptor.Iterator<XmlCsvImportSettings<SigneeXmlCsvItem>> createIterator(final WizardDescriptor.Panel<XmlCsvImportSettings<SigneeXmlCsvItem>> firstPanel) {
        return new XmlDataImportActionWizardIterator(firstPanel);
    }

    @Override
    protected synchronized Templates getTemplate() {
        if (XSLT == null) {
            try (InputStream is = SigneeXmlCsvSettings.class.getResourceAsStream("csv2signees.xsl")) {
                XSLT = FACTORY.newTemplates(new StreamSource(is));
            } catch (TransformerConfigurationException | IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return XSLT;
    }

    @Override
    protected void configureTransformer(final Transformer transformer) {
        final String defaultSigneeSuffix = getImportTargetProperty().getDefaultSigneeSuffix();
        transformer.setParameter("signee.suffix", defaultSigneeSuffix);
    }

    @Override
    public synchronized XmlCsvDictionary createDictionary() throws IOException {
        final FileObject d = FileUtil.getConfigFile("/Imports/signees-dictionary.xml");
        try (final InputStream is = d.getInputStream()) {
            return (XmlCsvDictionary) DICTJAXB.createUnmarshaller().unmarshal(is);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    @Messages("SigneeXmlCsvItem.createItem.sourceLabel={1}\u2116{0}")
    @Override
    protected SigneeXmlCsvItem createItem(final XmlItem i, final String csvId) {
        if (i instanceof XmlSigneeItem) {
            final XmlSigneeItem xti = (XmlSigneeItem) i;
            String tablePos = "?";
            if (xti.getPosition() != null) {
                tablePos = Integer.toString(xti.getPosition());
            }
            String table = "";
            if (csvId != null) {
                table = csvId + "/";
            }
            final String name = ImportItemsUtil.findSigneeName(xti);
            final Signee signee = xti.getSignee();
            if (name != null && !name.equals("null") && signee != null) {
                final String label = NbBundle.getMessage(SigneeXmlCsvSettings.class,
                        "SigneeXmlCsvItem.createItem.sourceLabel", tablePos, table);
                return new SigneeXmlCsvItem(label, name, signee, xti, getImportTargetProperty());
            }
        }
        return null;

    }

    static class XmlDataImportActionWizardIterator extends AbstractFileImportWizard<XmlCsvImportSettings<SigneeXmlCsvItem>> {

        private final WizardDescriptor.Panel<XmlCsvImportSettings<SigneeXmlCsvItem>> first;

        XmlDataImportActionWizardIterator(final WizardDescriptor.Panel<XmlCsvImportSettings<SigneeXmlCsvItem>> firstPanel) {
            this.first = firstPanel;
        }

        //Vier verschiedene Typen: SuS,Klassen - Kurse - Kurszuordnungen - Lehrer
        @Override
        protected ArrayList<WizardDescriptor.Panel<XmlCsvImportSettings<SigneeXmlCsvItem>>> createPanels() {
            final ArrayList<WizardDescriptor.Panel<XmlCsvImportSettings<SigneeXmlCsvItem>>> ret = new ArrayList<>();
            if (first != null) {
                ret.add(first);
            }
            ret.add(new XmlDataImportConfigVisualPanel.XmlDataImportConfigPanel());
            ret.add(new ConfigureDictionaryVisualPanel.ConfigureDictionaryPanel());
            ret.add(new SigneeXmlCsvImportVisualPanel.SigneeXmlCsvImportPanel());
            return ret;
        }

    }
}
