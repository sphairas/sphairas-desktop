/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.model;

import com.google.common.eventbus.Subscribe;
import org.thespheres.betula.termreport.XmlAssessmentProviderDataProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.Icon;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.openide.awt.NotificationDisplayer;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.termreport.AssessmentProvider;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.termreport.AssessmentProviderEnvironment;
import org.thespheres.betula.termreport.NumberAssessmentProvider;
import org.thespheres.betula.termreport.TargetAssessmentProvider;
import org.thespheres.betula.termreport.TermReport;
import org.thespheres.betula.termreport.TermReportActions;
import org.thespheres.betula.termreport.model.XmlNumberAssessmentProvider.XmlProviderReference;
import org.thespheres.betula.termreport.xml.XmlTermReportNumberAssessment;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.assess.NumberValueGrade;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 */
@Messages({"XmlNumberAssessmentProvider.init.IllegalStateException=Ein Fehler ist aufgetreten.",
    "XmlNumberAssessmentProvider.defaultDisplayName=Schnitt"})
public class XmlNumberAssessmentProvider extends NumberAssessmentProvider implements XmlAssessmentProviderDataProvider<NumberAssessmentProvider> {

    private final List<XmlProviderReference> reff = new ArrayList<>();
    final ReferenceChildren children = new ReferenceChildren();
    private final DescriptiveStatistics vstat = new DescriptiveStatistics();
    private final DescriptiveStatistics wstat = new DescriptiveStatistics();
    private final Map<StudentId, Double> values = new HashMap<>();
    private final XmlAssessmentProviderEnvironment env;
    private final Listener refListener = new Listener();

    @SuppressWarnings({"LeakingThisInConstructor", "OverridableMethodCallInConstructor"})
    public XmlNumberAssessmentProvider(XmlTermReportNumberAssessment data, Lookup context) {
        super(data.getId(), AssessmentProvider.READY);
        this.env = new XmlAssessmentProviderEnvironment(this, context);
        setDisplayName(data.getDisplayName());
        ic.add(children.getIndex());
        newInitialize(data);
    }

    private void newInitialize(XmlTermReportNumberAssessment data) {
        final Initialize ni = new Initialize(data, env.getContextLookup());
        ni.res.addLookupListener(ni);
        ni.resultChanged(null);
    }

    public static XmlNumberAssessmentProvider create(TermReportActions context) {
        final String id = TermReportUtilities.findId(context.getTermReport());
        final XmlTermReportNumberAssessment.Ref[] init = context.getTermReport().getProviders().stream()
                .map(p -> new XmlTermReportNumberAssessment.Ref(p.getId(), 1.0))
                .toArray(XmlTermReportNumberAssessment.Ref[]::new);
        final XmlTermReportNumberAssessment data = new XmlTermReportNumberAssessment(id, init);
        data.setDisplayName(NbBundle.getMessage(XmlTargetAssessmentProvider.class, "XmlNumberAssessmentProvider.defaultDisplayName"));
        return new XmlNumberAssessmentProvider(data, context.getContext());
    }

    @Override
    public AssessmentProviderEnvironment getEnvironment() {
        return env;
    }

    @Override
    public List<? extends ProviderReference> getProviderReferences() {
        return reff;
    }

    public XmlProviderReference addReference(AssessmentProvider ap) {
        XmlProviderReference ret = addImpl(ap, 1.0, -1);
        Mutex.EVENT.writeAccess(children::update);
        pSupport.firePropertyChange(PROP_PROVIDER_REFERENCES, null, null);
        return ret;
    }

    //-1 = add at end
    private XmlProviderReference addImpl(AssessmentProvider p, final double w, int position) {
        synchronized (reff) {
            if (contains(p)) {
                throw new IllegalStateException("Provider " + p.getId() + " is already contained in reference list of " + getId());
            }
            final XmlProviderReference ret = new XmlProviderReference(p, w);
            if (position > 0 && position <= reff.size()) {
                reff.add(position, ret);
            } else {
                reff.add(ret);
            }
            resetValues();
            ret.pSupport.addPropertyChangeListener(refListener);
            return ret;
        }
    }

    public boolean contains(final AssessmentProvider p) {
        synchronized (reff) {
            return reff.stream()
                    .anyMatch(pr -> pr.ref == p || pr.ref.equals(p));
        }
    }

    private void removeImpl(final XmlProviderReference toRemove) {
        boolean contained;
        synchronized (reff) {
            contained = reff.remove(toRemove);
        }
        if (contained) {
            toRemove.pSupport.removePropertyChangeListener(refListener);
            resetValues();
            Mutex.EVENT.writeAccess(children::update);
            pSupport.firePropertyChange(PROP_PROVIDER_REFERENCES, null, null);
        }
    }

    void setDisplayNameImpl(String displayName) {
        super.setDisplayName(displayName);
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    protected Node createNodeDelegate() {
        return new XmlNumberAssessmentProviderNode(this);
    }

    @Override
    public Number select(StudentId student) {
        synchronized (values) {
            return values.computeIfAbsent(student, this::calculate);
        }
    }

    private synchronized Double calculate(StudentId student) {
        if (this.reff.isEmpty()) {
            return null;
        }
        vstat.clear();
        wstat.clear();
        for (XmlProviderReference r : this.reff) {
            Object o = r.ref.select(student);
            if (o instanceof NumberValueGrade) {
                final NumberValueGrade g = (NumberValueGrade) o;
                wstat.addValue(r.getWeight());
                vstat.addValue(((NumberValueGrade) g).getNumberValue().doubleValue());
            } else {
                return null;
            }
        }
        Mean m = new Mean();
        try {
            return m.evaluate(vstat.getValues(), wstat.getValues());
        } catch (MathIllegalArgumentException maex) {
            return Double.NaN;
        }
    }

    @Messages({"XmlNumberAssessmentProvider.addOneReference.message.missing=AssessmentProvider {1} is missing in {0}. Cannot add number assessment reference."})
    private void addOneReference(final String idr, final double w, int position) throws MissingResourceException {
        try {
            final AssessmentProvider p = env.getTermReport().getProviders().stream()
                    .filter(ap -> ap.getId().equals(idr))
                    .collect(CollectionUtil.requireSingleOrNull());
            if (p != null) {
                addImpl(p, w, position);
            } else {
                String msg = NbBundle.getMessage(XmlNumberAssessmentProvider.class, "XmlNumberAssessmentProvider.addOneReference.message.missing", getDisplayName(), idr);
                PlatformUtil.getCodeNameBaseLogger(XmlNumberAssessmentProvider.class).log(Level.INFO, msg);
            }
        } catch (IllegalStateException ex) {
            logIllex(ex);
        }
    }

    private void resetValues() {
        synchronized (values) {
            values.clear();
        }
    }

    @Override
    public Timestamp timestamp(StudentId student) {
        return null;
    }

    @Override
    public Set<StudentId> students() {
        return Collections.EMPTY_SET;
    }

    @Override
    public void remove() throws IOException {
        env.remove();
    }

    @Override
    public XmlTermReportNumberAssessment getXmlAssessmentProviderData() {
        return XmlTermReportNumberAssessment.create(this);
    }

    private static void logIllex(IllegalStateException ex) throws MissingResourceException {
        Logger.getLogger(XmlNumberAssessmentProvider.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage());
        Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        String title = NbBundle.getMessage(XmlNumberAssessmentProvider.class, "XmlNumberAssessmentProvider.init.IllegalStateException");
        String detail = ex.getMessage();
        NotificationDisplayer.getDefault()
                .notify(title, ic, detail != null ? detail : "", null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    private final class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ProviderReference.PROP_WEIGHT.equals(evt.getPropertyName())) {
                env.getDataObject().setModified(true);
            }
        }

    }

    private final class Initialize implements LookupListener {

        private Lookup.Result<TermReport> res;
        private XmlTermReportNumberAssessment data;

        @SuppressWarnings("LeakingThisInConstructor")
        private Initialize(XmlTermReportNumberAssessment data, Lookup context) {
            res = context.lookupResult(TermReport.class);
            this.data = data;
        }

        @Override
        public synchronized void resultChanged(LookupEvent ev) {
            final TermReport tr = env.getTermReport();
            if (tr != null && data != null) {
                res.removeLookupListener(this);
                res = null;
                XmlTermReportNumberAssessment.Ref[] rr = data.getReferences();
                data = null;
                if (rr != null) {
                    int index = 0;
                    for (XmlTermReportNumberAssessment.Ref r : rr) {
                        final String idr = r.getIdReference();
                        final double w = r.getWeight();
                        addOneReference(idr, w, index++);
                    }
                }
                tr.getEventBus().register(this);
                Mutex.EVENT.writeAccess(children::update);
                pSupport.firePropertyChange(PROP_PROVIDER_REFERENCES, null, null);
            }
        }

        private void updateOrder(String[] newOrder) {
            synchronized (reff) {
                final Map<String, XmlProviderReference> refs = reff.stream()
                        .collect(Collectors.toMap(pr -> pr.getReferenced().getId(), pr -> pr));
                int index = 0;
                for (String p : newOrder) {
                    if (refs.containsKey(p)) {
                        reff.set(index++, refs.get(p));
                    }
                }
            }
        }

        @Subscribe
        public void onChange(CollectionChangeEvent event) {
            if (event.getCollectionName().equals(TermReport.PROP_ASSESSMENTS) && event.getSource() instanceof TermReport) {
                event.getItemAs(AssessmentProvider.class).ifPresent(provider -> {
                    if (event.getType().equals(CollectionChangeEvent.Type.REORDER)) {
                        reordered((TermReport) event.getSource());
                    } else if (event.getType().equals(CollectionChangeEvent.Type.REMOVE)) {
                        assessmentRemoved(provider);
                    }
                });
            }
        }

        private void assessmentRemoved(final AssessmentProvider provider) {
            final XmlProviderReference removed;
            synchronized (reff) {
                removed = reff.stream()
                        .filter(ref -> ref.getReferenced().equals(provider)) //TODO getId().equals????
                        .collect(CollectionUtil.requireSingleOrNull());
            }
            if (removed != null) {
                removed.remove();
            }
        }

        private void reordered(TermReport tr) {
            String[] order;
            synchronized (tr) {
                order = tr.getProviders().stream()
                        .map(AssessmentProvider::getId)
                        .toArray(String[]::new);
            }
            updateOrder(order);
            children.update();
            pSupport.firePropertyChange(PROP_PROVIDER_REFERENCES, null, null);
        }

    }

    final class ReferenceChildren extends Index.KeysChildren<XmlProviderReference> {

        public ReferenceChildren() {
            super(XmlNumberAssessmentProvider.this.reff);
        }

        @Override
        protected Node[] createNodes(XmlProviderReference key) {
            return new Node[]{key.getNodeDelegate()};
        }

    }

    public final class XmlProviderReference implements PropertyChangeListener, TargetAssessment.Listener<Grade>, ProviderReference {

        protected PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
        private final AssessmentProvider ref;
        private double weight;
        private Node refNode;

        @SuppressWarnings("LeakingThisInConstructor")
        private XmlProviderReference(AssessmentProvider ref, double initialWeight) {
            this.ref = ref;
            this.weight = initialWeight;
            ref.addPropertyChangeListener(this);
            setTargetAssessmentListener();
        }

        private void setTargetAssessmentListener() {
            if (ref instanceof TargetAssessmentProvider && ref.getInitialization().equals(AssessmentProvider.READY)) {
                TargetAssessmentProvider tap = (TargetAssessmentProvider) ref;
                tap.addListener(this);
            }
        }

        @Override
        public AssessmentProvider getReferenced() {
            return ref;
        }

        public XmlNumberAssessmentProvider getNumberAssessmentProvider() {
            return XmlNumberAssessmentProvider.this;
        }

        @Override
        public Node getNodeDelegate() {
            if (refNode == null) {
                refNode = new ProviderReferenceNode(this);
            }
            return refNode;
        }

        @Override
        public double getWeight() {
            return weight;
        }

        @Override
        public void setWeight(double weight) {
            double old = this.weight;
            this.weight = weight;
            resetValues();
            pSupport.firePropertyChange(PROP_WEIGHT, old, this.weight);
        }

        public void remove() {
            ref.removePropertyChangeListener(this);
            if (ref instanceof TargetAssessmentProvider) {
                ((TargetAssessmentProvider) ref).removeListener(this);
            }
            removeImpl(this);
            env.getDataObject().setModified(true);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (AssessmentProvider.PROP_STATUS.equals(evt.getPropertyName())) {
                setTargetAssessmentListener();
            }
        }

        @Override
        public void valueForStudentChanged(Object source, StudentId s, Grade old, Grade newGrade, Timestamp timestamp) {
            synchronized (values) {
                values.remove(s);
            }
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pSupport.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pSupport.removePropertyChangeListener(listener);
        }

    }
}
