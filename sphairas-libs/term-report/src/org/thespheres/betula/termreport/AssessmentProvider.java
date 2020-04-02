/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport;

import java.awt.datatransfer.DataFlavor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.undo.UndoableEditSupport;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.tag.AbstractState;
import org.thespheres.betula.tag.State;

/**
 *
 * @author boris.heithecker
 * @param <V>
 */
public abstract class AssessmentProvider<V> { //Umstrukturieren wie RemoteTargetAssessmentDocument, abstracte Klasse

    public static final String PROP_STATUS = "status";
    public static final String PROP_DISPLAYNAME = "displayName";
    public static final String PROP_EDITABLE = "editable";
    public static final State READY = new Initialization(1, false, "ready");
    public static final State LOADING = new Initialization(0, false, "loading");
    public static final State BROKEN_LINK = new Initialization(-1, true, "broken.link");

    public static final DataFlavor ASSESSMENT_PROVIDER_FLAVOR = new DataFlavor(AssessmentProvider.class, "AssessmentProvider") {
    };
    String PRPO_DATA = "data";
    private final String id;
    protected String displayName;
    protected PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    protected boolean editable = false;
    private final Map<String, Object> properties = new HashMap<>();
    protected final Set<Marker> markers = new HashSet<>();
    protected final UndoableEditSupport undoSupport = new UndoableEditSupport(this);
    protected final State[] initialization;
    private Node node;
    private Lookup lookup;
    protected final InstanceContent ic = new InstanceContent();
    private TableColumnConfiguration tableConfig;

    protected AssessmentProvider(String id, State initial) {
        this.id = id;
        initialization = new State[]{initial};
    }

    public String getId() {
        return id;
    }

    public Set<Marker> getMarkers() {
        return markers;
    }

    public Lookup getLookup() {
        if (lookup == null) {
            lookup = createLookup();
        }
        return lookup;
    }

    protected Lookup createLookup() {
        ic.add(this);
        return new AbstractLookup(ic);
    }

    public State getInitialization() {
        State ret;
        synchronized (initialization) {
            ret = initialization[0];
        }
        return ret;
    }

    public String getDisplayName() {
        return displayName;
    }

    protected void setDisplayName(String displayName) {
        String old = getDisplayName();
        this.displayName = displayName;
        pSupport.firePropertyChange(PROP_DISPLAYNAME, old, getDisplayName());
    }

    public abstract boolean isEditable();

//    public abstract Action[] getActions();
    public Node getNodeDelegate() {
        if (node == null) {
            node = createNodeDelegate();
        }
        return node;
    }

    protected abstract Node createNodeDelegate();

    public abstract AssessmentProviderEnvironment getEnvironment();

    //Must be initialized lazily, after deserialization, ui properties may not be ready
    public final TableColumnConfiguration getTableColumnConfiguration() {
        if (tableConfig == null) {
            tableConfig = createTableColumnConfiguration();
        }
        return tableConfig;
    }

    protected TableColumnConfiguration createTableColumnConfiguration() {
        return new TableColumnConfiguration(this);
    }

    public abstract V select(StudentId student);

    public void submit(StudentId student, V value, Timestamp ts) {
        if (!isEditable()) {
            throw new UnsupportedOperationException("AssessmentProvider is not editable.");
        }
    }

    public abstract Timestamp timestamp(StudentId student);

    public abstract Set<StudentId> students();

    public abstract void remove() throws IOException;

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pSupport.removePropertyChangeListener(l);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, Object value) {
        Object old = properties.put(key, value);
        pSupport.firePropertyChange(key, old, value);
    }

    public static final class Initialization extends AbstractState<Initialization> {

        private final static String NAME = "assessment.provider.initialization";

        public Initialization(int level, boolean error, String gradeId) {
            super(level, error, NAME, gradeId);
        }

    }
}
