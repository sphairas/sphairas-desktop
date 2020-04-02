/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.model;

import org.thespheres.betula.termreport.AssessmentProviderNode;
import org.thespheres.betula.termreport.XmlAssessmentProviderDataProvider;
import java.awt.EventQueue;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.termreport.AssessmentProvider;
import org.thespheres.betula.termreport.AssessmentProviderEnvironment;
import org.thespheres.betula.termreport.TargetAssessmentProvider;
import org.thespheres.betula.termreport.TermReport;
import org.thespheres.betula.termreport.action.UnresolvedLink;
import org.thespheres.betula.termreport.xml.XmlFileTargetAssessmentReference;
import org.thespheres.betula.tag.State;
import org.thespheres.betula.termreport.module.TargetAssessmentDelegate;

/**
 *
 * @author boris.heithecker
 */
public class XmlFileTargetAssessmentProvider extends TargetAssessmentProvider implements XmlAssessmentProviderDataProvider<TargetAssessmentProvider> {

    private TargetAssessment target;
    private final RequestProcessor RP = new RequestProcessor(XmlFileTargetAssessmentProvider.class);
    private final XmlFileTargetAssessmentReference reference;
    private final FileListener listener = new FileListener();
    private final XmlAssessmentProviderEnvironment env;

    public XmlFileTargetAssessmentProvider(XmlFileTargetAssessmentReference ref, Lookup context) {
        super(ref.getId(), AssessmentProvider.LOADING);
        this.reference = ref;
        this.env = new XmlAssessmentProviderEnvironment(this, context);
        RP.post(this::linkData);
    }

    public static XmlFileTargetAssessmentProvider create(final URI link, final Lookup context, final String refId) {
        final String id = TermReportUtilities.findId(context.lookup(TermReport.class));
        final Project p = FileOwnerQuery.getOwner(context.lookup(DataObject.class).getPrimaryFile());
        final URI relLink = p.getProjectDirectory().toURI().relativize(link);
        return new XmlFileTargetAssessmentProvider(new XmlFileTargetAssessmentReference(id, relLink, refId), context);
    }

    @Override
    public AssessmentProviderEnvironment getEnvironment() {
        return env;
    }

    @Override
    protected Node createNodeDelegate() {
        return new FileRefNode(this);
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    //Invoke outside AWT
    public void linkData() {
        FileObject ret = null;
        URI u = reference.getRelativeUri();
        if (u != null) {
            URI folderURI = env.getProject().getProjectDirectory().toURI();
            URI dataURI = folderURI.resolve(u);
            try {
                ret = URLMapper.findFileObject(dataURI.toURL());
            } catch (MalformedURLException ex) {
            }
        }
        if (ret != null) {
            try {
                DataObject linkedDataObject = DataObject.find(ret);
                EventQueue.invokeLater(() -> setDisplayName(linkedDataObject.getNodeDelegate().getDisplayName()));
                listener.initialize(linkedDataObject);
                return;
            } catch (DataObjectNotFoundException ex) {
            }
        }
        setBrokenLink();
    }

    private void setBrokenLink() {
        final UnresolvedLink ul = new UnresolvedLink(reference, this);
        ic.add(ul);
        setDisplayName(ul.getDisplayName());
        synchronized (initialization) {
            State before = getInitialization();
            initialization[0] = BROKEN_LINK;
            pSupport.firePropertyChange(PROP_STATUS, before, getInitialization());
        }
    }

    public void restoreLink(URI u) throws IOException {
        FileObject ret = null;
        try {
            ret = URLMapper.findFileObject(u.toURL());
        } catch (MalformedURLException ex) {
        }
        if (ret == null) {
            throw new IOException("URI " + u.toString() + " not found.");
        }
        try {
            DataObject linkedDataObject = DataObject.find(ret);
            EventQueue.invokeLater(() -> setDisplayName(linkedDataObject.getNodeDelegate().getDisplayName()));
            URI folderURI = env.getProject().getProjectDirectory().toURI();
            URI dataURI = folderURI.relativize(u);
            reference.setRelativeUri(dataURI);
            listener.initialize(linkedDataObject);
            env.getDataObject().setModified(true);
        } catch (DataObjectNotFoundException ex) {
            throw new IOException(ex);
        }

    }

    protected void checkInitialization() {
        if (!getInitialization().satisfies(READY)) {
            throw new IllegalStateException("TargetAssessment is not ready.");
        }
    }

    @Override
    public Grade select(StudentId student) {
        try {
            checkInitialization();
        } catch (IllegalStateException e) {
            return null;
        }
        return (Grade) target.select(student);
    }

    @Override
    public Timestamp timestamp(StudentId student) {
        checkInitialization();
        return target.timestamp(student);
    }

    @Override
    public Set<StudentId> students() {
        checkInitialization();
        return target.students();
    }

    @Override
    public void submit(StudentId student, Grade grade, Timestamp timestamp) {
        if (!isEditable()) {
            throw new UnsupportedOperationException("TargetAssessment is not editable.");
        }
        checkInitialization();
        target.submit(student, grade, timestamp);
    }

    @Override
    public String getPreferredConvention() {
        checkInitialization();
        return target.getPreferredConvention();
    }

    @Override
    public void addListener(Listener listener) {
        checkInitialization();
        target.addListener(listener);
    }

    @Override
    public void removeListener(Listener listener) {
//        checkInitialization();
        if (target != null) {
            target.removeListener(listener);
        }
    }

    private void setTargetAssessment(TargetAssessment target) {
        synchronized (initialization) {
            this.target = target;
            State before = getInitialization();
            initialization[0] = READY;
            pSupport.firePropertyChange(PROP_STATUS, before, getInitialization());
        }
    }

    @Override
    public void remove() throws IOException {
        env.remove();
    }

    @Override
    public XmlFileTargetAssessmentReference getXmlAssessmentProviderData() {
        return this.reference;
    }

    private final static class FileRefNode extends AssessmentProviderNode<XmlFileTargetAssessmentProvider> {

        public FileRefNode(XmlFileTargetAssessmentProvider p) {
            super(p);
        }

        @Override
        public Image getIcon(int type) {
            XmlFileTargetAssessmentProvider p = (XmlFileTargetAssessmentProvider) provider;
            DataObject ld = p.listener.getLinkedDataObject();
            if (ld != null && ld.isValid()) {
                Image badge = ImageUtilities.loadImage("org/thespheres/betula/termreport/resources/table-badge.png", true);
                Image orig = ld.getNodeDelegate().getIcon(type);
                return ImageUtilities.mergeImages(orig, badge, 6, 0);
            } else if (p.getInitialization().equals(BROKEN_LINK)) {
                return ImageUtilities.loadImage("org/thespheres/betula/termreport/resources/chain--exclamation.png", true);
            }
            return super.getIcon(type);
            //fireIconChange()
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            super.propertyChange(evt);
            if (AssessmentProvider.PROP_STATUS.equals(evt.getPropertyName())) {
                fireIconChange();
            }
        }

        @Override
        public String getHtmlDisplayName() {
            XmlFileTargetAssessmentProvider p = (XmlFileTargetAssessmentProvider) provider;
            if (p.getInitialization().equals(BROKEN_LINK)) {
                return "<html><i><font color=\"FFA500\">" + getDisplayName() + "</font></i></html>";
            }
            return super.getHtmlDisplayName();
        }

        @Override
        public Action[] getActions(boolean context) {
            return Utilities.actionsForPath("Loaders/text/betula-term-report-target-assessment-context/Actions").stream()
                    .map(Action.class::cast)
                    .toArray(Action[]::new);
        }

        @Override
        public Action getPreferredAction() {
            return Actions.forID("Betula", "org.thespheres.betula.termreport.action.ResolveLinksAction");
        }

    }

    private final class FileListener implements LookupListener, PropertyChangeListener {

        private Lookup.Result<TargetAssessment> result;
        private DataObject linkedDataObject;

        private void initialize(DataObject dob) {
            linkedDataObject = dob;
            result = linkedDataObject.getLookup().lookupResult(TargetAssessment.class);
            result.addLookupListener(this);
            linkedDataObject.addPropertyChangeListener(this);
            linkedDataObject.getNodeDelegate().addPropertyChangeListener(this);
            resultChanged(null);
        }

        public DataObject getLinkedDataObject() {
            return linkedDataObject;
        }

        @Override
        public synchronized void resultChanged(LookupEvent ev) {
            TargetAssessment[] found = result.allInstances().stream()
                    .map(TargetAssessment.class::cast)
                    .filter(ta -> reference.getReferencedId() == null
                    || (ta instanceof TargetAssessmentDelegate && ((TargetAssessmentDelegate) ta).getOriginal().getId().equals(reference.getReferencedId())))
                    .toArray(TargetAssessment[]::new);
            if (found.length == 1) {
                TargetAssessment target = found[0];
                setTargetAssessment(target);
            } else if (found.length > 1) {
                //Log warning, user message
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case DataObject.PROP_VALID:
                    if (!getLinkedDataObject().isValid()) {
//                        try {
//                            XmlFileTargetAssessmentProvider.this.remove();
//                        } catch (IOException ex) {
//                            Logger.getLogger(XmlFileTargetAssessmentProvider.class.getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
//                            XmlFileTargetAssessmentProvider.this.reference.setRelativeUri(null);
//                        }
                        setBrokenLink();
                    }
                    env.getDataObject().setModified(true);
                    break;
                case DataObject.PROP_PRIMARY_FILE: //File moved
                    updateLink();
                    break;
                case DataObject.PROP_NAME://File renamed
                    updateLink();
                    break;
                case Node.PROP_DISPLAY_NAME:
                    setDisplayName(linkedDataObject.getNodeDelegate().getDisplayName());
                    break;
            }
        }

        private void updateLink() {
            final URI link = getLinkedDataObject().getPrimaryFile().toURI();
            final URI relLink = XmlFileTargetAssessmentProvider.this.env.getProject().getProjectDirectory().toURI().relativize(link);
            if (!Objects.equals(XmlFileTargetAssessmentProvider.this.reference.getRelativeUri(), relLink)) {
                XmlFileTargetAssessmentProvider.this.reference.setRelativeUri(relLink);
                env.getDataObject().setModified(true);
            }
        }

    }

}
