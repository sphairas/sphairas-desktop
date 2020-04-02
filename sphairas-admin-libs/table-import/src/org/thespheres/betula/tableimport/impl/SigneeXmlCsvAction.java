/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.impl;

import java.util.Set;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.tableimport.action.XmlCsvImportAction;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;
import org.thespheres.betula.tableimport.csv.XmlCsvFile;
import org.thespheres.betula.xmlimport.model.ImportSigneeItem;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.SigneeUpdater;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula",
        id = "org.thespheres.betula.tableimport.impl.SigneeXmlCsvAction")
@ActionRegistration(
        displayName = "#SigneeXmlCsvAction.displayName")
@ActionReference(path = "Menu/import-export", position = 52000)
@Messages({"SigneeXmlCsvAction.displayName=Import aus Tabelle (Lehrer/Unterzeichner)"})
public class SigneeXmlCsvAction extends XmlCsvImportAction<SigneeXmlCsvItem> {

    public SigneeXmlCsvAction() {
        super();
    }

    @Override
    protected Product getProduct() {
        return Product.NO;
    }

    @Override
    protected XmlCsvImportSettings<SigneeXmlCsvItem> createSettings(XmlCsvFile[] xml) {
        return new SigneeXmlCsvSettings(xml);
    }

    @Override
    protected AbstractUpdater<SigneeXmlCsvItem> createUpdater(Set<?> selected, ConfigurableImportTarget config, Term term, XmlCsvImportSettings<SigneeXmlCsvItem> wiz) {
        final SigneeXmlCsvItem[] iti = selected.stream()
                .map(SigneeXmlCsvItem.class::cast)
                .filter(ImportSigneeItem::isSelected)
                .toArray(SigneeXmlCsvItem[]::new);
        return new SigneeUpdater<>(config, iti, SigneeXmlCsvItem::doUpdate);
        }

}
