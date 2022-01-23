/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.impl;

import java.awt.Component;
import java.awt.EventQueue;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsFactory;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.io.NullInputStream;
import org.openide.util.io.NullOutputStream;
import org.openide.xml.XMLUtil;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.adminreports.editor.RemoteSectionsProvider;
import org.thespheres.betula.reports.model.EditableReportCollection;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.admin.units.UnitOpenSupport;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.document.model.MarkerDecoration;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.util.ServiceConfiguration;

/**
 *
 * @author boris.heithecker
 */
public class RemoteReportsSupport extends CloneableEditorSupport implements UnitOpenSupport, GuardedEditorSupport, OpenCookie, EditCookie, EditorCookie.Observable, PrintCookie, CloseCookie, SaveAsCapable, LineCookie {

    public static final String EDITOR_MIME = "text/betula-remote-reports";
    private final static HashSet<RemoteReportsSupport> OBJECTS = new HashSet<>();
    private StyledDocument docLoadingSaving;
    private final RemoteReportsModel remoteModel;
    private RemoteSectionsProvider guardedProvider;
    private final RequestProcessor.Task initTask;
    private final DescriptorProjectProperties properties;
    private final Listener listener = new Listener();

    @SuppressWarnings("LeakingThisInConstructor")
    private RemoteReportsSupport(RemoteReportsDescriptorFileDataObject rdob, RemoteReportsModel remoteModel, DescriptorProjectProperties props) {
        super(new Environment(rdob, remoteModel), rdob.getLookup());
        this.remoteModel = remoteModel; //
        initTask = Util.RP(remoteModel.getDescriptor().getProvider()).create(remoteModel::initialize);
//this.remoteModel.getRemoteLookup().getRequestProcessor().create(remoteModel::initialize);
        properties = props;
//        this.remoteModel.addPropertyChangeListener(this);
        remoteModel.addPropertyChangeListener(listener);
    }

    public static RemoteReportsSupport find(RemoteReportsDescriptorFileDataObject rdob) throws IOException {
        synchronized (OBJECTS) {
            for (RemoteReportsSupport ur : OBJECTS) {
                if (ur.getDataObject().equals(rdob)) {
                    return ur;
                }
            }
            final DescriptorProjectProperties props = DescriptorProjectProperties.create(rdob);
            RemoteReportsModel rm = new RemoteReportsModel(rdob.getDescriptor(), rdob.getPrimaryFile().getPath(), rdob.getLookup());
            RemoteReportsSupport ur = new RemoteReportsSupport(rdob, rm, props);
            OBJECTS.add(ur);
            return ur;
        }
    }

    public DataObject getDataObject() {
        return ((Environment) env).dataObject;
    }

    public RemoteReportsModel getRemoteReportsModel() {
        return remoteModel;
    }

    @Override
    public Node getNodeDelegate() {
        return getDataObject().getNodeDelegate();
    }

    public Optional<Signees> getSignees() {
        return properties.getSignees();
    }

    public JMSTopicListenerService findJMSTopicListenerServiceProvider(String topic) throws IOException {
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

    @Override
    protected boolean asynchronousOpen() {
        return true;
    }

    @Override
    public void open() {
        startInitTask();
        super.open();
    }

    private void startInitTask() {
        if (!initTask.isFinished()) {
            initTask.schedule(0);
        }
    }

    @Override
    protected Pane createPane() {
        //Multiview:: create pane here
        return super.createPane();
    }

    @Override
    protected Component wrapEditorComponent(Component editorComponent) {
        //TODO: add toolbar
        return super.wrapEditorComponent(editorComponent); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void initializeCloneableEditor(CloneableEditor editor) {
        Node n = getNodeDelegate();
        editor.setIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
        editor.setActivatedNodes(new Node[]{n});
    }

    @Override
    protected StyledDocument createStyledDocument(EditorKit kit) {
        StyledDocument doc = super.createStyledDocument(kit);
        doc.putProperty(Document.TitleProperty, remoteModel.getDisplayName());
        doc.putProperty(Document.StreamDescriptionProperty, getDataObject());
        return doc;
    }

    @Override
    public StyledDocument getDocument() {
        if (docLoadingSaving != null) {
            return docLoadingSaving;
        }
        return super.getDocument();
    }

    @Override
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        startInitTask();
        if (guardedProvider == null) {
            String mimeType = ((CloneableEditorSupport.Env) this.env).getMimeType();
            guardedProvider = (RemoteSectionsProvider) GuardedSectionsFactory.find(mimeType).create(this);
        }

//        NbEditorKit k = (NbEditorKit) kit;
        // load content to kit
        if (guardedProvider != null) {
            docLoadingSaving = doc;
//            Charset cs = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
            if (!initTask.isFinished() && EventQueue.isDispatchThread()) {
                Logger.getLogger(RemoteReportsSupport.class.getName()).log(Level.WARNING, "RemoteReportsSupport.loadFromStreamToKit should not be called from AWT while RemoteReportsModel is still being initialized.");
            }
            final long max = ServiceConfiguration.getInstance().getMaxWaitTimeInEDT();
            try {
                initTask.waitFinished(max);
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            }
            try (Reader reader = guardedProvider.createGuardedReader(stream, Charset.defaultCharset())) {
                kit.read(reader, doc, 0);
            }
        } else {
            kit.read(stream, doc, 0);
        }

        docLoadingSaving = null;
    }

    @Override
    public void saveDocument() throws IOException {
        RemoteEditableReportCollection collection = (RemoteEditableReportCollection) getDocument().getProperty(EditableReportCollection.class.getCanonicalName());
        collection.submitCollection();
    }

    @Override
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
//        if (guardedProvider != null) {
//            docLoadingSaving = doc;
////            Charset cs = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
//            try (Writer writer = guardedProvider.createGuardedWriter(stream, Charset.defaultCharset())) {
//                kit.write(writer, doc, 0, doc.getLength());
//            }
//        } else {
//            kit.write(stream, doc, 0, doc.getLength());
//        }
//        docLoadingSaving = null;
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    protected String messageSave() {
        return null;
    }

    @Override
    protected String messageName() {
        if (!((Environment) env).isValid()) {
            return ""; // NOI18N
        }
        return DataEditorSupport.annotateName(getNodeDelegate().getDisplayName(), false, isModified(), false);
    }

    @Override
    protected String messageHtmlName() {
        if (!((Environment) env).isValid()) {
            return null;
        }

        String name = getNodeDelegate().getHtmlDisplayName();
        if (name == null) {
            try {
                name = XMLUtil.toElementContent(getNodeDelegate().getDisplayName());
            } catch (CharConversionException ex) {
                return null;
            }
        }

        return DataEditorSupport.annotateName(name, true, isModified(), false);
    }

    @Override
    protected String messageToolTip() {
        // update tooltip
        return null;
    }

    @Messages("RemoteReportsSupport.messageOpening=Berichte werden geladen.")
    @Override
    protected String messageOpening() {
        return NbBundle.getMessage(RemoteReportsSupport.class, "RemoteReportsSupport.messageOpening");
    }

    @Override
    protected String messageOpened() {
        return null;
    }

    @Override
    public void saveAs(FileObject folder, String name) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LocalFileProperties findBetulaProjectProperties() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MarkerDecoration findMarkerDecoration() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (RemoteReportsModel.PROP_MODIFIED.equals(evt.getPropertyName())) {
//                if (!getRemoteReportsModel().isModified()) {
//                    notifyUnmodified();
//                } else {
//                    notifyModified();
//                }
                updateTitles();
            }
        }

    }

    private static class Environment extends AbstractUnitOpenSupport.AbstractEnv implements CloneableEditorSupport.Env, Externalizable {

        private static final long serialVersionUID = 1L;
        private RemoteReportsModel remoteModel;
        private RemoteReportsDescriptorFileDataObject dataObject;

        public Environment() {
        }

        private Environment(RemoteReportsDescriptorFileDataObject rdob, RemoteReportsModel rrm) {
            this.remoteModel = rrm;
            this.dataObject = rdob;
        }

        @Override
        public boolean isValid() {
            return remoteModel != null;
        }

        @Override
        public boolean isModified() {
            return remoteModel.isModified();
        }

        @Override
        public void markModified() throws IOException {
//            findCloneableOpenSupport().setModified(true);
            remoteModel.setModified(true);
        }

        @Override
        public void unmarkModified() {
//            findCloneableOpenSupport().setModified(false);
            remoteModel.setModified(false);
        }

        @Override
        public Lookup getLookup() {
            return remoteModel.getLookup();
        }

        @Override
        public Date getTime() {
            return remoteModel.getTime();
        }

        @Override
        public String getMimeType() {
//            return RemoteReportsDescriptorFileDataObject.FILE_MIME;
            return RemoteReportsSupport.EDITOR_MIME;
        }

        @Override
        public InputStream inputStream() throws IOException {
            return new NullInputStream();
        }

        @Override
        public OutputStream outputStream() throws IOException {
            return new NullOutputStream();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(dataObject);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            Object o = in.readObject();
            if (o instanceof RemoteReportsDescriptorFileDataObject) {
                dataObject = (RemoteReportsDescriptorFileDataObject) o;
            }
            if (dataObject == null) {
                throw new IOException();
            }
            remoteModel = dataObject.getLookup().lookup(RemoteReportsModel.class);
            if (remoteModel == null) {
                throw new IOException("Could not find RemoteReportsModel");
            }
        }

        @Override
        public RemoteReportsSupport findCloneableOpenSupport() {
            if (dataObject.isValid()) {
                try {
                    return find(dataObject);
                } catch (IOException ex) {
                    //Will never happen if dataObject is valid.
                }
            }
            return null;
        }

    }
}
