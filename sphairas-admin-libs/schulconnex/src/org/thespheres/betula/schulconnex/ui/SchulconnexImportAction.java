/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.schulconnex.Schulconnex;
import org.thespheres.betula.schulconnex.SchulconnexImportConfiguration;
import org.thespheres.betula.schulconnex.SchulconnexImportData;
import org.thespheres.betula.schulconnex.SchulconnexKlasseItem;
import org.thespheres.betula.schulconnex.SchulconnexKursItem;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.uiutil.AbstractImportAction;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;
import org.thespheres.betula.xmlimport.utilities.UpdaterFilter;

/**
 * Menu action that triggers one of the Schulconnex import wizard variants.
 * <p>
 * The action is registered in the {@code Menu/import-export} folder. When
 * invoked it will:
 * <ol>
 * <li>Connect to the Schulconnex API (TODO - not yet implemented)</li>
 * <li>Show a wizard for the user to select the target provider and review the
 * fetched data for the selected import type</li>
 * <li>Push the selected entries to the sphairas web application</li>
 * </ol>
 *
 * @author boris.heithecker
 */
@Messages({
    "SchulconnexImportAction.signee.displayName=Schulconnex (Lehrende)",
    "SchulconnexImportAction.primaryUnit.displayName=Schulconnex (Klassen)",
    "SchulconnexImportAction.targetItem.displayName=Schulconnex (Kurse)",
    "SchulconnexImportAction.dialog.title=Schulconnex-Import"
})
public class SchulconnexImportAction extends AbstractImportAction<SchulconnexImportData<?>, SchulconnexImportConfiguration, ImportItem> implements PropertyChangeListener {

    public static final String SCHULCONNEX_IMPORT_TYPE = "schulconnex-import-type";
    public static final String SIGNEE = "signee";
    public static final String PRIMARY_UNIT = "primary-unit";
    public static final String TARGET_ITEM = "target-item";

    private static final RequestProcessor RP = new RequestProcessor(SchulconnexImportAction.class);
    private final String type;

    @ActionID(category = "Betula",
            id = "org.thespheres.betula.schulconnex.ui.SchulconnexImportAction.signee")
    @ActionRegistration(displayName = "#SchulconnexImportAction.signee.displayName")
    @ActionReference(path = "Menu/import-export", position = 3500)
    public static SchulconnexImportAction signeeImport() {
        return new SchulconnexImportAction(SIGNEE);
    }

    @ActionID(category = "Betula",
            id = "org.thespheres.betula.schulconnex.ui.SchulconnexImportAction.primaryUnit")
    @ActionRegistration(displayName = "#SchulconnexImportAction.primaryUnit.displayName")
    @ActionReference(path = "Menu/import-export", position = 3520)
    public static SchulconnexImportAction primaryUnitImport() {
        return new SchulconnexImportAction(PRIMARY_UNIT);
    }

    @ActionID(category = "Betula",
            id = "org.thespheres.betula.schulconnex.ui.SchulconnexImportAction.targetItem")
    @ActionRegistration(displayName = "#SchulconnexImportAction.targetItem.displayName")
    @ActionReference(path = "Menu/import-export", position = 3540)
    public static SchulconnexImportAction targetItemImport() {
        return new SchulconnexImportAction(TARGET_ITEM);
    }

    private SchulconnexImportAction(final String type) {
        super(org.openide.util.NbBundle.getMessage(SchulconnexImportAction.class, "SchulconnexImportAction.dialog.title"));
        this.type = type;
    }

    @Override
    protected Product getProduct() {
        return Schulconnex.getProduct();
    }

    /**
     * Creates the wizard settings object and sets {@link #iterator}.
     * <p>
     * Sets up the Schulconnex API client with authentication. The actual fetch
     * of readPersonen/readGruppen data will be triggered after the user selects
     * a provider in step 1 and confirms to proceed to step 2.
     */
    @Override
    protected SchulconnexImportData<?> createSettingsAndIterator() {
        final SchulconnexImportData<?> d = new SchulconnexImportData<>();
        d.putProperty(SCHULCONNEX_IMPORT_TYPE, type);
        d.addPropertyChangeListener(this);
        iterator = new SchulconnexImportActionWizardIterator(type);
        return d;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (IMPORT_TARGET.equals(evt.getPropertyName())) {
            final SchulconnexImportConfiguration config = (SchulconnexImportConfiguration) evt.getNewValue();
            @SuppressWarnings("unchecked")
            final SchulconnexImportData<SchulconnexKlasseItem> wiz = (SchulconnexImportData<SchulconnexKlasseItem>) evt.getSource();
            if (config != null) {
                RP.post(() -> wiz.fetchSchulconnexData());
            }
        }
    }

    @Override
    protected String findLastImportTargetUrl() {
        return NbPreferences.forModule(SchulconnexImportAction.class).get(SAVED_IMPORT_TARGET_PROVIDER, null);
    }

    /**
     * Creates the updater that pushes the selected data to the web application.
     * <p>
     * <b>TODO:</b> Implement once the API fetch step is in place.
     *
     * @return {@code null} until the implementation is complete (no-op stub)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected AbstractUpdater<?> createUpdater(Set<?> selected, SchulconnexImportConfiguration config, Term term, SchulconnexImportData<?> wiz) {
        switch (type) {
            case PRIMARY_UNIT:
                final SchulconnexKlasseItem[] items = selected.stream()
                        .map(SchulconnexKlasseItem.class::cast)
                        .filter(SchulconnexKlasseItem::isSelected)
                        .toArray(SchulconnexKlasseItem[]::new);
                //see XmlCsvImportAction
//                final TargetItemsUpdaterDescriptions d = createTargetItemsUpdaterDescriptions(config, wiz);
                return new SchulconnexPrimaryUnitsUpdater(items,
                        config.getWebServiceProvider(),
                        term,
                        Collections.singletonList(new PrimaryUnitUpdaterFilter()),
                        config,
                        null);
            case SIGNEE:
                // TODO Schulconnex: implement signee updater once ImportSigneeItem mapping is in place.
                return null;
            case TARGET_ITEM:
                final SchulconnexKursItem[] iti = selected.stream()
                        .map(SchulconnexKursItem.class::cast)
                        .filter(SchulconnexKursItem::isSelected)
                        .toArray(SchulconnexKursItem[]::new);
//                final TargetItemsUpdaterDescriptions d = createTargetItemsUpdaterDescriptions(config, wiz);
                return new TargetItemsUpdater<>(iti, config.getWebServiceProvider(), term, Collections.singletonList(new TargetItemsUpdaterFilter()), null);
            default:
                return null;
        }
    }

    static class PrimaryUnitUpdaterFilter implements UpdaterFilter<ImportTargetsItem, TargetDocumentProperties> {

        @Override
        public boolean accept(final ImportTargetsItem iti) {
            if (iti instanceof SchulconnexKlasseItem) {
                final SchulconnexKlasseItem item = (SchulconnexKlasseItem) iti;
                return item.isSelected() && item.isValid();
            }
            return iti != null && iti.isValid();
        }

        @Override
        public boolean accept(final ImportTargetsItem iti, final UnitId u, final StudentId stud) {
            // TODO Schulconnex: wire per-student selection when student items are implemented.
            return UpdaterFilter.super.accept(iti, u, stud);
        }
    }

    static class TargetItemsUpdaterFilter implements UpdaterFilter<ImportTargetsItem, TargetDocumentProperties> {

        @Override
        public boolean accept(ImportTargetsItem iti) {
            return iti.isValid();
        }

        @Override
        public boolean accept(ImportTargetsItem iti, TargetDocumentProperties td, StudentId stud) {
            return td.getPreferredConvention() != null || td.isTextValueTarget();
        }

        @Override
        public boolean accept(ImportTargetsItem iti, TargetDocumentProperties td, StudentId student, TermId term, ImportTargetsItem.GradeEntry entry) {
            return StringUtils.equalsIgnoreCase(td.getTargetType(), "zeugnisnoten");
        }

    }
}
