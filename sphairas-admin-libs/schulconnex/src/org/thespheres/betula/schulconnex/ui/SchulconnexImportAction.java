/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex.ui;

import de.schulconnex.ApiClient;
import de.schulconnex.ApiException;
import de.schulconnex.SchulconnexQSApi;
import de.schulconnex.auth.HttpTokenAuth;
import de.schulconnex.qs.model.Gruppendatensatz;
import de.schulconnex.qs.model.Personendatensatz;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.schulconnex.Schulconnex;
import org.thespheres.betula.schulconnex.SchulconnexImportConfiguration;
import org.thespheres.betula.schulconnex.SchulconnexImportData;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.uiutil.AbstractImportAction;
import org.openide.WizardDescriptor;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;

/**
 * Menu action that triggers one of the Schulconnex import wizard variants.
 * <p>
 * The action is registered in the {@code Menu/import-export} folder.
 * When invoked it will:
 * <ol>
 *   <li>Connect to the Schulconnex API (TODO - not yet implemented)</li>
 *   <li>Show a wizard for the user to select the target provider and review the
 *       fetched data for the selected import type</li>
 *   <li>Push the selected entries to the sphairas web application</li>
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
public class SchulconnexImportAction extends AbstractImportAction<SchulconnexImportData, SchulconnexImportConfiguration, ImportItem> implements PropertyChangeListener {

    public static final String SIGNEE = "signee";
    public static final String PRIMARY_UNIT = "primary-unit";
    public static final String TARGET_ITEM = "target-item";
    public static final String IMPORT_TYPE = "schulconnex-import-type";
    public static final String SCHULCONNEX_DATA = "schulconnex-api-data";
    public static final String SCHULCONNEX_PERSONEN_DATA = "schulconnex-api-personen-data";
    public static final String SCHULCONNEX_GRUPPEN_DATA = "schulconnex-api-gruppen-data";

    private static final RequestProcessor RP = new RequestProcessor(SchulconnexImportAction.class);
    private static final Logger LOG = Logger.getLogger(SchulconnexImportAction.class.getName());

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
    protected SchulconnexImportData createSettingsAndIterator() {
        final SchulconnexImportData d = new SchulconnexImportData();
        d.putProperty(IMPORT_TYPE, type);
        d.addPropertyChangeListener(this);
        iterator = new SchulconnexImportActionWizardIterator(type);
        return d;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (IMPORT_TARGET.equals(evt.getPropertyName())) {
            final SchulconnexImportConfiguration config = (SchulconnexImportConfiguration) evt.getNewValue();
            final SchulconnexImportData wiz = (SchulconnexImportData) evt.getSource();
            if (config != null) {
                RP.post(() -> fetchSchulconnexData(config, wiz));
            }
        }
    }

    private void fetchSchulconnexData(final SchulconnexImportConfiguration config, final SchulconnexImportData wiz) {
        final ApiClient client = ApiClient.create(1, config.getSchulconnexApiEndpoint());
        final HttpTokenAuth clientAuth = client.getAuthentication("token", HttpTokenAuth.class);
        clientAuth.setTokenEndpoint(config.getSchulconnexTokenEndpoint());
        clientAuth.setUsername(config.getSchulconnexClientId());
        clientAuth.setPassword(config.getSchulconnexClientSecret());
        final SchulconnexQSApi api = new SchulconnexQSApi(client);
        try {
            switch (type) {
                case SIGNEE:
                    final List<Personendatensatz> persons = api.searchPersonenkontexte(null, null, null, null);
                    wiz.putProperty(SCHULCONNEX_DATA, persons);
                    wiz.putProperty(SCHULCONNEX_PERSONEN_DATA, persons);
                    break;
                case PRIMARY_UNIT:
                    final List<Gruppendatensatz> gruppen = api.searchGruppen(null, null, null, null, null, null, null, null);
                    final List<Personendatensatz> personsForPrimaryUnit = api.searchPersonenkontexte(null, null, null, null);
                    wiz.putProperty(SCHULCONNEX_DATA, gruppen);
                    wiz.putProperty(SCHULCONNEX_GRUPPEN_DATA, gruppen);
                    wiz.putProperty(SCHULCONNEX_PERSONEN_DATA, personsForPrimaryUnit);
                    break;
                case TARGET_ITEM:
                    final List<Gruppendatensatz> gruppenForTargetItem = api.searchGruppen(null, null, null, null, null, null, null, null);
                    wiz.putProperty(SCHULCONNEX_DATA, gruppenForTargetItem);
                    wiz.putProperty(SCHULCONNEX_GRUPPEN_DATA, gruppenForTargetItem);
                    break;
            }
        } catch (ApiException ex) {
            LOG.log(Level.SEVERE, "Schulconnex API error", ex);
            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ex.getLocalizedMessage());
        }
    }

    @Override
    protected String findLastImportTargetUrl() {
        return NbPreferences.forModule(SchulconnexImportAction.class).get(SAVED_IMPORT_TARGET_PROVIDER, null);
    }

    /**
    * Creates the updater that pushes the selected data to the web
     * application.
     * <p>
     * <b>TODO:</b> Implement once the API fetch step is in place.
     *
     * @return {@code null} until the implementation is complete (no-op stub)
     */
    @Override
    protected AbstractUpdater<?> createUpdater(Set<?> selected, SchulconnexImportConfiguration config,
            Term term, SchulconnexImportData wiz) {
        // TODO: build and return the appropriate AbstractUpdater / SigneeUpdater
        return null;
    }
}
