/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.ui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.thespheres.betula.termreport.NumberAssessmentProvider;
import org.thespheres.betula.termreport.model.XmlNumberAssessmentProvider;
import org.thespheres.betula.termreport.model.XmlTermReportImpl;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
class NumberAssessmentConfigurationModel implements PropertyChangeListener, LookupListener {

    final XmlNumberAssessmentProvider[] current = new XmlNumberAssessmentProvider[]{null};
    final Lookup.Result<XmlNumberAssessmentProvider> result;
    private final NumberAssessmentConfigurationTopComponent component;
    private XmlTermReportImpl currentReport;

    NumberAssessmentConfigurationModel(NumberAssessmentConfigurationTopComponent component) {
        this.result = Utilities.actionsGlobalContext().lookupResult(XmlNumberAssessmentProvider.class);
        this.component = component;
    }

    private void updateComponent() {
        final List<XmlNumberAssessmentProvider.XmlProviderReference> l;
        final DataObject currentCtx;
        synchronized (current) {
            if (current[0] == null) {
                return;
            }
            l = current[0].getProviderReferences().stream()
                    .map(XmlNumberAssessmentProvider.XmlProviderReference.class::cast)
                    .collect(Collectors.toList());
            currentCtx = current[0].getEnvironment().getContextLookup().lookup(DataObject.class);
        }
        int i = component.panels.size();
        if (l.size() > i) {
            component.createPanels(i - l.size());
        }
        for (int c = 0; c < component.panels.size(); c++) {
            final NumberAssessmentConfigurationTopComponent.SpinnerPanel sp = component.panels.get(c);
            if (c < l.size()) {
                sp.initialize(l.get(c));
            } else {
                sp.uninitialize();
            }
        }
        component.updateWidths();
        component.updateContext(currentCtx, current[0]);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case TopComponent.Registry.PROP_ACTIVATED_NODES:
                final XmlTermReportImpl singleTr = Arrays.stream(TopComponent.getRegistry().getActivatedNodes())
                        .map(Node::getLookup)
                        .flatMap(n -> n.lookupAll(XmlTermReportImpl.class).stream())
                        .collect(CollectionUtil.singleOrNull());
                synchronized (current) {
                    if (!Objects.equals(currentReport, singleTr)) {
                        currentReport = singleTr;
                    }
                }
                break;
            case NumberAssessmentProvider.PROP_PROVIDER_REFERENCES:
                EventQueue.invokeLater(this::updateComponent);
                break;
        }
    }

    @Override
    public synchronized void resultChanged(LookupEvent ev) {
        final XmlNumberAssessmentProvider c = result.allInstances().stream()
                .map(XmlNumberAssessmentProvider.class::cast)
                .collect(CollectionUtil.singleOrNull());
        if (c == null) {
//            synchronized (current) {
//                if (current[0] == null || (currentReport != null && !currentReport.getProviders().contains(c))) {
//                    return;
//                }
//                current[0] = null;
//            }
//            EventQueue.invokeLater(this::updateComponent);
        } else {
            final XmlNumberAssessmentProvider before;
            synchronized (current) {
                before = current[0];
                if (before != null) {
                    before.removePropertyChangeListener(this);
                }
                current[0] = c;
                current[0].addPropertyChangeListener(this);
            }
            if (!Objects.equals(before, current[0])) {
                EventQueue.invokeLater(this::updateComponent);
            }
        }
    }

}
