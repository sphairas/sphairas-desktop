package org.thespheres.betula.termreport.model;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import org.thespheres.betula.termreport.module.TargetAssessmentDelegate;
import com.google.common.eventbus.Subscribe;
import java.util.Objects;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.termreport.TermReport;
import org.thespheres.betula.termreport.module.TermReportDataObject;
import org.thespheres.betula.util.CollectionChangeEvent;
import org.thespheres.betula.util.CollectionChangeEvent.Type;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
class XmlTargetAssessmentProviderLookup implements LookupListener {

    private final TermReportDataObject dob;
    private Lookup.Result<TermReport> result;
    private final RequestProcessor RP = new RequestProcessor(XmlTargetAssessmentProviderLookup.class);
    private final InstanceContent ic = new InstanceContent(RP);
    private final Lookup lookup = new AbstractLookup(ic);
    private TermReport report;

    @SuppressWarnings("LeakingThisInConstructor")
    private XmlTargetAssessmentProviderLookup(TermReportDataObject dataobj) {
        this.dob = dataobj;
        RP.post(this::initialize);
    }

    static Lookup createLookup(TermReportDataObject dob) {
        XmlTargetAssessmentProviderLookup ret = new XmlTargetAssessmentProviderLookup(dob);
        return ret.lookup;
    }

    private void initialize() {
        this.result = this.dob.getLookup().lookupResult(TermReport.class);
        this.result.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public final synchronized void resultChanged(LookupEvent ev) {
        TermReport old = report;
        report = result.allInstances().stream()
                .map(TermReport.class::cast)
                .collect(CollectionUtil.singleOrNull());
        if (!Objects.equals(old, report)) {
            if (old != null) {
                old.getEventBus().unregister(this);
                lookup.lookupAll(XmlTargetAssessmentProvider.class).stream()
                        .map(XmlTargetAssessmentProvider.class::cast)
                        .forEach(this::addTargetAssessmentProvider);
            }
            if (report != null) {
                report.getEventBus().register(this);
                report.getProviders().stream()
                        .filter(XmlTargetAssessmentProvider.class::isInstance)
                        .map(XmlTargetAssessmentProvider.class::cast)
                        .forEach(this::addTargetAssessmentProvider);
            }
        }
    }

    @Subscribe
    public void onCollectionChangeChange(CollectionChangeEvent evt) {
        final String name = evt.getCollectionName();
        if (TermReport.PROP_ASSESSMENTS.equals(name)) {
            evt.getItemAs(XmlTargetAssessmentProvider.class).ifPresent(tap -> {
                if (evt.getType().equals(Type.ADD)) {
                    addTargetAssessmentProvider(tap);
                } else if (evt.getType().equals(Type.REMOVE)) {
                    removeTargetAssessmentProvider(tap);
                }
            });
        }
    }

    private synchronized void removeTargetAssessmentProvider(XmlTargetAssessmentProvider tap) {
        lookup.lookupAll(XmlTargetAssessmentProvider.class).stream()
                .filter(item -> item.getId().equals(tap.getId()))
                .forEach(ic::remove);
    }

    private synchronized void addTargetAssessmentProvider(XmlTargetAssessmentProvider tap) {
        ic.add(new TargetAssessmentDelegate(tap));
    }


    @ServiceProvider(service = LookupProvider.class, path = "Loaders/text/term-report-file+xml/Lookup")
    public static class Registration implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup baseContext) {
            TermReportDataObject dob = baseContext.lookup(TermReportDataObject.class);
            if (dob != null) {
                return XmlTargetAssessmentProviderLookup.createLookup(dob);
            }
            return Lookup.EMPTY;
        }
    }
}
