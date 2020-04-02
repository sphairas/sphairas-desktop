/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;
import java.util.Properties;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEditSupport;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.CloneableOpenSupport;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.util.OpenSupportProperties;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.document.model.MarkerDecoration;
import org.thespheres.betula.document.model.Subject;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractUnitOpenSupport extends CloneableOpenSupport implements OpenCookie, EditCookie, CloseCookie, NavigatorLookupHint, UnitOpenSupport, Lookup.Provider {

    public static final String FACTORY = "service"; //"beans"
    public static final String MIME = "application/betula-unit-data";
    private final Node[] delegate = new Node[]{null};
    private final RequestProcessor RP = new RequestProcessor(AbstractUnitOpenSupport.class.getName());
    protected final InstanceContent ic;
    private final Lookup[] lookup = new Lookup[]{null};
    protected final OpenSupportProperties properties;
    protected final UndoableEditSupport undoSupport = new UndoableEditSupport(this);
    private final Properties loadingProps = new Properties();

    protected AbstractUnitOpenSupport(final AbstractEnv env, final OpenSupportProperties properties) {
        super(env);
        this.properties = properties;
        ic = new InstanceContent(RP);
    }

    @Override
    public Lookup getLookup() {
        synchronized (lookup) {
            if (lookup[0] == null) {
                Lookup base = new AbstractLookup(ic);
                ic.add(this);
                lookup[0] = createLookup(base);
            }
            return lookup[0];
        }
    }

    protected RemoteUnitsModel createRemoteUnitsModel(final UnitId[] units) throws IOException {
        final RemoteUnitsModel.Factory fac = Lookup.getDefault().lookupAll(RemoteUnitsModel.Factory.class).stream()
                .filter(f -> f.id().equals(FACTORY))
                .collect(CollectionUtil.requireSingleOrNull());
        if (fac == null) {
            throw new IllegalStateException("Factory " + FACTORY + " not found.");
        }
        return fac.create(findProviderUrl(), this, units);
//        if (USE_BEAN) {
//            return new BeanRemoteUnitsModel(this, units);
//        } else {
//            return new ServiceRemoteUnitsModel(findProviderUrl(), this, units);
//        }
    }

    protected Lookup createLookup(Lookup base) {
        return base;
    }

    public RequestProcessor getRP() {
        return RP;
    }

    public UndoableEditSupport getUndoSupport() {
        return undoSupport;
    }

    public synchronized void addUndoableEditListener(UndoableEditListener l) {
        undoSupport.addUndoableEditListener(l);
    }

    public synchronized void removeUndoableEditListener(UndoableEditListener l) {
        undoSupport.removeUndoableEditListener(l);
    }

    @Override
    public LocalProperties findBetulaProjectProperties() throws IOException {
        return properties.findBetulaProjectProperties();
    }

    public WebServiceProvider findWebServiceProvider() throws IOException {
        return properties.findWebServiceProvider();
    }

    public MoveStudentsToTargetDropSupport findDropSupport() {
        return Lookup.getDefault().lookup(MoveStudentsToTargetDropSupport.class);
    }

    protected String findProviderUrl() throws IOException {
        return properties.findProviderUrl();
    }

    protected String findDomainProviderUrl() throws IOException {
        return properties.findDomainProviderUrl();
    }

    public Optional<Signees> getSignees() {
        return properties.getSignees();
    }

    public Optional<Units> getUnits() {
        return properties.getUnits();
    }

    public JMSTopicListenerService findJMSTopicListenerService(final String topic) throws IOException {
        return properties.findJMSTopicListenerService(topic);
    }

    public SchemeProvider findSchemeProvider(String betulaProperty) throws IOException {
        return properties.findSchemeProvider(betulaProperty);
    }

    public TermSchedule findTermSchedule() throws IOException {
        return properties.findTermSchedule();
    }

    public NamingResolver findNamingResolver() throws IOException {
        return properties.findNamingResolver();
    }

    public DocumentsModel findDocumentsModel() throws IOException {
        return properties.findDocumentsModel();
    }

    @Override
    public MarkerDecoration findMarkerDecoration() throws IOException {
        return properties.findMarkerDecoration();
    }

    public Subject findSubject(AbstractTargetAssessmentDocument rtad) {
        return rtad.getSubject();
    }

    @Override
    public Node getNodeDelegate() {
        synchronized (delegate) {
            if (delegate[0] == null) {
                delegate[0] = createNodeDelegate();
            }
            return delegate[0];
        }
    }

    @Override
    public String getContentType() {
        return MIME;
    }

    @Messages("AbstractUnitOpenSupport.messageOpening=Kurse werden geladen.")
    @Override
    protected String messageOpening() {
        return NbBundle.getMessage(AbstractUnitOpenSupport.class, "AbstractUnitOpenSupport.messageOpening");
    }

    @Override
    protected String messageOpened() {
        return null;
    }

    public Properties getLoadingProperties() {
        return loadingProps;
    }

    public abstract RemoteUnitsModel getRemoteUnitsModel() throws IOException;

    public abstract RemoteUnitsModel getRemoteUnitsModel(final RemoteUnitsModel.INITIALISATION stage) throws IOException;

    protected abstract Node createNodeDelegate();

    public static abstract class AbstractEnv implements Serializable, Lookup.Provider, CloneableOpenSupport.Env {

        public static final long serialVersionUID = 1L;
        private transient PropertyChangeSupport pSupport; //do not initialize in constructor, missing after deserialization
        private transient VetoableChangeSupport vetoableChangeSupport; //see above

        protected PropertyChangeSupport getPropertyChangeSupport() {
            synchronized (this) {
                if (pSupport == null) {
                    pSupport = new PropertyChangeSupport(this);
                }
                return pSupport;
            }
        }

        protected VetoableChangeSupport getVetoableChangeSupport() {
            synchronized (this) {
                if (vetoableChangeSupport == null) {
                    vetoableChangeSupport = new VetoableChangeSupport(this);
                }
                return vetoableChangeSupport;
            }
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            getPropertyChangeSupport().addPropertyChangeListener(l);
        }

        @Override
        public void addVetoableChangeListener(VetoableChangeListener l) {
            getVetoableChangeSupport().addVetoableChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            getPropertyChangeSupport().removePropertyChangeListener(l);
        }

        @Override
        public void removeVetoableChangeListener(VetoableChangeListener l) {
            getVetoableChangeSupport().removeVetoableChangeListener(l);
        }

    }
}
