/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.project;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.project.UnitTargetProjectTemplate;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.util.Targets;
import org.thespheres.betula.services.util.UnitUtilities;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class DefaultBetulaProjectTemplate extends UnitTargetProjectTemplate {

    protected String baseURL;

    public DefaultBetulaProjectTemplate(String providerUrl, Properties template, String baseUrl) {
        super(providerUrl, template);
        this.baseURL = baseUrl;
    }

    @Override
    public List<UnitTargetSelection> createList() throws IOException {
        LocalFileProperties lp = LocalFileProperties.find(getProvider());
        String url = lp.getProperty("providerURL", getProvider());
        final WebServiceProvider wsp = WebProvider.find(url, WebServiceProvider.class);
        try {
            Map<DocumentId, List<UnitId>> td = wsp.getDefaultRequestProcessor().submit(() -> {
                return Targets.get(wsp.getInfo().getURL()).getTargetDocuments();
            }).get();
            return createFromRawTargets(td, lp);
        } catch (InterruptedException | ExecutionException ex) {
            throw new IOException(ex);
        }
    }

    protected List<UnitTargetSelection> createFromRawTargets(final Map<DocumentId, List<UnitId>> targets, LocalProperties props) {
        final DocumentsModel dm = new DocumentsModel();
        dm.initialize(props.getProperties());
        final String p = props.getProperty("providerURL", getProvider());
        final String np = props.getProperty("naming.providerURL", p);
        final Map<DocumentId, List<DocumentId>> t = targets.keySet().stream().collect(Collectors.groupingBy(dm::convert));
        return targets.entrySet().stream()
                .map(e -> {
                    final DocumentId b = dm.convert(e.getKey());
                    final UnitId uid;
                    final List<UnitId> l = e.getValue();
                    if (l.isEmpty()) {
                        return null;
                    } else if (e.getValue().size() == 1) {
                        uid = e.getValue().get(0);
                    } else {
                        uid = l.stream().filter((UnitId u) -> u.getAuthority().equals(b.getAuthority()) && u.getId().equals(b.getId())).collect(CollectionUtil.singleOrNull());
                    }
                    if (uid != null) {
                        return new DefaultUnitTargetSelection(uid, b, p, np);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .peek(ut -> t.get(ut.getTargetBase()).stream().forEach(d -> ut.getTargetDocuments().add(d)))
                .collect(Collectors.toList());
    }

    protected String getBaseURL() {
        return baseURL;
    }

    protected String getDefaultConvention() {
        return "de.notensystem";
    }

    protected String getTermScheduleProvider() {
        return "mk.niedersachsen.de";
    }

    class DefaultUnitTargetSelection extends UnitTargetSelection {

        DefaultUnitTargetSelection(UnitId unit, DocumentId base, String provider, String namingProv) {
            super(unit, base, provider, namingProv);
        }

        @Override
        public Properties createProjectProperties(Properties defaults) {
            final Properties ret = super.createProjectProperties(null);
            ret.setProperty("termSchedule.providerURL", getTermScheduleProvider());
            final String calendarUrl = getBaseURL() + "/calendar/stundenplan?" + UnitUtilities.queryEncodeUnitId(getUnitId());
            ret.setProperty("calendarUrl", calendarUrl);
            //Find default target doc from docmodel
            //use targets.fetchtarget to access documentproperties
            //write target.
            final LocalFileProperties lp = LocalFileProperties.find(getProvider());
            final DocumentsModel dm = new DocumentsModel();
            dm.initialize(lp.getProperties());
            final String pSuffix = dm.getModelPrimarySuffix();
            String preferrecConvention = null;
            final DocumentId target
                    = //is default suffix, webuiconfiguration, docmodel
                    getTargetDocuments().stream() //is default suffix, webuiconfiguration, docmodel
                            .filter((DocumentId d) -> dm.getSuffix(d).equals(pSuffix)).collect(CollectionUtil.singleOrNull());
            if (target != null) {
                String url = ret.getProperty("providerURL", getProvider());
                final WebServiceProvider wsp = WebProvider.find(url, WebServiceProvider.class);
                try {
                    preferrecConvention = wsp.getDefaultRequestProcessor().submit(() -> {
                        return Targets.get(url).fetchTargetAssessment(getUnitId(), target);
                    }).get().getPreferredConvention();
                } catch (InterruptedException | ExecutionException ex) {
                }
            }
            if (preferrecConvention == null) {
                preferrecConvention = getDefaultConvention();
            }
            ret.setProperty("preferredConvention", preferrecConvention);
            return ret;
        }
    }
}
