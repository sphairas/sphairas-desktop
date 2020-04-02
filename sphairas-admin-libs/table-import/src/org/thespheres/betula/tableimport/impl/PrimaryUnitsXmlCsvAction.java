/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.impl;

import java.util.Collections;
import java.util.Set;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.tableimport.action.XmlCsvImportAction;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;
import org.thespheres.betula.tableimport.csv.XmlCsvFile;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdaterDescriptions;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula",
        id = "org.thespheres.betula.tableimport.impl.PrimaryUnitXmlCsvAction")
@ActionRegistration(
        displayName = "#PrimaryUnitXmlCsvAction.displayName")
@ActionReference(path = "Menu/import-export", position = 50500)
@Messages({"PrimaryUnitXmlCsvAction.displayName=Import aus Tabelle (Klassen)"})
public class PrimaryUnitsXmlCsvAction extends XmlCsvImportAction<PrimaryUnitsXmlCsvItem> {

    public PrimaryUnitsXmlCsvAction() {
        super();
    }

    @Override
    protected Product getProduct() {
        return Product.NO;
    }

    @Override
    protected XmlCsvImportSettings<PrimaryUnitsXmlCsvItem> createSettings(XmlCsvFile[] xml) {
        return new PrimaryUnitsXmlCsvSettings(xml);
    }

    @Override
    protected AbstractUpdater<PrimaryUnitsXmlCsvItem> createUpdater(Set<?> selected, ConfigurableImportTarget config, Term term, XmlCsvImportSettings<PrimaryUnitsXmlCsvItem> wiz) {
        final PrimaryUnitsXmlCsvItem[] iti = selected.stream()
                .map(PrimaryUnitsXmlCsvItem.class::cast)
                .filter(AbstractXmlCsvImportItem::isSelected)
                .toArray(PrimaryUnitsXmlCsvItem[]::new);
        final TargetItemsUpdaterDescriptions d = createTargetItemsUpdaterDescriptions(config, wiz);
        return new PrimaryUnitsUpdater(iti, config.getWebServiceProvider(), term, Collections.singletonList(new PrimaryUnitsXmlCsvItem.Filter()), config, d);
    }

}
