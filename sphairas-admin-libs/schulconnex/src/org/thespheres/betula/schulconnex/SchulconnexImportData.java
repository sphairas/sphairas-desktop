/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex;

import de.schulconnex.ApiClient;
import de.schulconnex.ApiException;
import de.schulconnex.SchulconnexQSApi;
import de.schulconnex.auth.HttpTokenAuth;
import de.schulconnex.qs.model.Gruppendatensatz;
import de.schulconnex.qs.model.Personendatensatz;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.WizardDescriptor;
import org.thespheres.betula.schulconnex.ui.SchulconnexImportAction;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.uiutil.AbstractImportAction;
import org.thespheres.betula.xmlimport.uiutil.DefaultImportWizardSettings;

/**
 * Wizard settings / data holder for the Schulconnex import workflow.
 *
 * @author boris.heithecker
 * @param <T>
 */
public class SchulconnexImportData<T extends ImportItem> extends DefaultImportWizardSettings<SchulconnexImportConfiguration, T> {

    public static final String SCHULCONNEX_PERSONEN_DATA = "schulconnex-api-personen-data";
    public static final String SCHULCONNEX_GRUPPEN_DATA = "schulconnex-api-gruppen-data";

    private static final Logger LOG = Logger.getLogger(SchulconnexImportData.class.getName());

    public SchulconnexImportConfiguration getConfiguration() {
        return (SchulconnexImportConfiguration) getProperty(AbstractImportAction.IMPORT_TARGET);
    }

    public String getImportType() {
        return (String) getProperty(SchulconnexImportAction.SCHULCONNEX_IMPORT_TYPE);
    }

    public void fetchSchulconnexData() {
        final ApiClient client = ApiClient.create(1, getConfiguration().getSchulconnexApiEndpoint());
        final HttpTokenAuth clientAuth = client.getAuthentication("token", HttpTokenAuth.class);
        clientAuth.setTokenEndpoint(getConfiguration().getSchulconnexTokenEndpoint());
        clientAuth.setUsername(getConfiguration().getSchulconnexClientId());
        clientAuth.setPassword(getConfiguration().getSchulconnexClientSecret());
        final SchulconnexQSApi api = new SchulconnexQSApi(client);
        putProperty(SCHULCONNEX_GRUPPEN_DATA, null);
        putProperty(SCHULCONNEX_PERSONEN_DATA, null);
        try {
            switch (getImportType()) {
                case SchulconnexImportAction.SIGNEE:
                    final List<Personendatensatz> persons = api.searchPersons(null, null, null, null);
                    putProperty(SCHULCONNEX_PERSONEN_DATA, persons);
                    break;
                case SchulconnexImportAction.PRIMARY_UNIT:
                    loadPrimaryUnits(api);
                    break;
                case SchulconnexImportAction.TARGET_ITEM:
                    final List<Gruppendatensatz> gruppenForTargetItem = api.searchGruppen(null, null, null, null, null, null, null, null);
                    putProperty(SCHULCONNEX_GRUPPEN_DATA, gruppenForTargetItem);
                    break;
            }
        } catch (ApiException ex) {
            LOG.log(Level.SEVERE, "Schulconnex API error", ex);
            putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ex.getLocalizedMessage());
        }
    }

    private void loadPrimaryUnits(final SchulconnexQSApi api) throws ApiException {
        final List<Gruppendatensatz> gruppen = api.searchGruppen(null, null, null, null, null, null, null, null);
        final List<Personendatensatz> personsForPrimaryUnit = api.searchPersons(null, null, null, null);
        putProperty(SCHULCONNEX_GRUPPEN_DATA, gruppen);
        putProperty(SCHULCONNEX_PERSONEN_DATA, personsForPrimaryUnit);
        final ChangeSet<T> cs = getSelectedNodesProperty();
        cs.clear();
        gruppen.stream()
                .filter(g -> "KLASSE".equalsIgnoreCase(g.getGruppe().getTyp()))
                .map(gds -> (T) new SchulconnexKlasseItem(gds, personsForPrimaryUnit, getConfiguration()))
                .forEach(cs::add);
    }
}
