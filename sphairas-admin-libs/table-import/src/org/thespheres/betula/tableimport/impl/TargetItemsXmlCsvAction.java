/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.tableimport.action.XmlCsvImportAction;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;
import org.thespheres.betula.tableimport.csv.XmlCsvFile;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdaterDescriptions;
import org.thespheres.betula.xmlimport.utilities.UpdaterFilter;

/**
 * @author boris.heithecker
 */
@ActionID(category = "Betula",
        id = "org.thespheres.betula.tableimport.impl.TargetItemsXmlCsvAction")
@ActionRegistration(
        displayName = "#TargetItemsXmlCsvAction.displayName")
@ActionReference(path = "Menu/import-export", position = 51000)
@Messages({"TargetItemsXmlCsvAction.displayName=Import aus Tabelle (Unterricht/Kurse)"})
public class TargetItemsXmlCsvAction extends XmlCsvImportAction<TargetItemsXmlCsvItem> {

    public TargetItemsXmlCsvAction() {
        super();
    }

    @Override
    protected Product getProduct() {
        return Product.NO;
    }

    @Override
    protected XmlCsvImportSettings<TargetItemsXmlCsvItem> createSettings(XmlCsvFile[] xml) {
        return new TargetItemsXmlCsvSettings(xml);
    }

    @Override
    protected AbstractUpdater<TargetItemsXmlCsvItem> createUpdater(final Set<?> selected, final ConfigurableImportTarget config, final Term term, final XmlCsvImportSettings<TargetItemsXmlCsvItem> wiz) {
        final ImportTargetsItem[] iti = selected.stream()
                .map(TargetItemsXmlCsvItem.class::cast)
                .filter(AbstractXmlCsvImportItem::isSelected)
                .flatMap(i -> map(i, config, term))
                .toArray(ImportTargetsItem[]::new);
        final TargetItemsUpdaterDescriptions d = createTargetItemsUpdaterDescriptions(config, wiz);
        return new TargetItemsUpdater(iti, config.getWebServiceProvider(), term, Collections.singletonList(new Filter()), d);
    }

    @Override
    protected void onUpdateFinished(ConfigurableImportTarget config, Set<?> selected, XmlCsvFile[] xml, XmlCsvImportSettings<TargetItemsXmlCsvItem> wiz, AbstractUpdater<?> u) {
    }

    private Stream<ImportTargetsItem> map(final TargetItemsXmlCsvItem i, final ConfigurableImportTarget config, final Term term) {
        Stream.Builder<ImportTargetsItem> builder = Stream.builder();
        Lookup.getDefault().lookupAll(ImportTargetsItemMapper.class).stream()
                .map(ImportTargetsItemMapper.class::cast)
                .sorted(Comparator.comparingInt(ImportTargetsItemMapper::position))
                .forEach(mapper -> {
                    final ImportTargetsItem[] mapped = mapper.map(config, i, term);
                    if (mapped == null) {
                        builder.add(i);
                    } else {
                        Arrays.stream(mapped)
                                .forEach(builder::add);
                    }
                });
        return builder.build();
    }

    public static class Filter implements UpdaterFilter<ImportTargetsItem, TargetDocumentProperties> {

        @Override
        public boolean accept(ImportTargetsItem iti) {
            return iti.isValid();
        }

        @Override
        public boolean accept(ImportTargetsItem iti, TargetDocumentProperties td, StudentId stud) {
            return td.getPreferredConvention() != null;
        }

        @Override
        public boolean accept(ImportTargetsItem iti, TargetDocumentProperties td, StudentId student, TermId term, ImportTargetsItem.GradeEntry entry) {
            return StringUtils.equalsIgnoreCase(td.getTargetType(), "zeugnisnoten");
        }

    }
}
