/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsFactory;
import org.netbeans.spi.project.LookupProvider;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.io.NullInputStream;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.module.Constants;
import org.thespheres.betula.journal.module.JournalDataObject;
import org.thespheres.betula.services.util.NbUtilities;
import org.thespheres.betula.ui.util.UIUtilities;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"NotesTopComponent.displayName=Berichtsheft-Bemerkungen",
    "NotesTopComponent.current.displayName=Berichtsheft-Bemerkungen ({0})"})
public class JournalMinutesSupport extends CloneableEditorSupport implements GuardedEditorSupport { //, EditorCookie, OpenCookie { //, CloseCookie {

    private final static HashSet<JournalMinutesSupport> OBJECTS = new HashSet<>();
    private MinutesSectionsProvider guardedProvider;
    private StyledDocument docLoadingSaving;
    private EditableJournal journal;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    private JournalMinutesSupport(JournalDataObject data) {
        super(new Environment(data));
    }

    public JournalDataObject getDataObject() {
        return ((Environment) env).dataObject;
    }

    @Override
    protected CloneableEditor createCloneableEditor() {
        throw new UnsupportedOperationException("Not supported.");
    }

//    @Override
//    protected boolean canClose() {
//        if (env.isModified()) {
//            try {
//                saveDocument();
//            } catch (IOException ex) {
//            }
//        }
//        return super.canClose();
//    }
    @Override
    public StyledDocument getDocument() {
        if (docLoadingSaving != null) {
            return docLoadingSaving;
        }
        return super.getDocument();
    }

    EditableJournal getJournal() throws IOException {
        if (journal == null) {
            journal = NbUtilities.waitForLookup(getDataObject().getLookup(), EditableJournal.class, 0l);
//            journal.getEventBus().register(this);
        }
        return journal;
    }

    @Override
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        if (guardedProvider == null) {
            guardedProvider = (MinutesSectionsProvider) GuardedSectionsFactory.find(Constants.JOURNAL_MINUTES_EDITOR_MIME).create(this);
        }
        // load content to kit
        if (guardedProvider != null) {
            docLoadingSaving = doc;
            try (Reader reader = guardedProvider.createGuardedReader(stream, Charset.defaultCharset())) {
                kit.read(reader, doc, 0);
            }
        } else {
            kit.read(stream, doc, 0);
        }
        docLoadingSaving = null;
    }

    @Override
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        if (guardedProvider != null) {
            docLoadingSaving = doc;
            try (Writer writer = guardedProvider.createGuardedWriter(stream, Charset.defaultCharset())) {
                kit.write(writer, doc, 0, doc.getLength());
            }
        } else {
            kit.write(stream, doc, 0, doc.getLength());
        }
        docLoadingSaving = null;
    }

    @Override
    protected String messageName() {
        if (!((Environment) env).isValid()) {
            return ""; // NOI18N
        }
        final JournalDataObject current = getDataObject();
        if (current == null) {
            return NbBundle.getMessage(JournalMinutesSupport.class, "NotesTopComponent.displayName");
        } else {
            String n = UIUtilities.findDisplayName(current);
            return NbBundle.getMessage(JournalMinutesSupport.class, "NotesTopComponent.current.displayName", n);
        }
    }

    @Override
    protected String messageSave() {
        return null;
    }

    @Override
    protected String messageToolTip() {
        return null;
    }

    @Override
    protected String messageOpening() {
        return null;
    }

    @Override
    protected String messageOpened() {
        return null;
    }

    private static class Environment implements CloneableEditorSupport.Env, Externalizable {

        private static final long serialVersionUID = 1L;
        private JournalDataObject dataObject;
        private Date time;
        private final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);

        public Environment() {
        }

        @Override
        public String getMimeType() {
            return Constants.JOURNAL_MINUTES_EDITOR_MIME;
        }

        private Environment(JournalDataObject rdob) {
            this.dataObject = rdob;
        }

        @Override
        public boolean isValid() {
//            return dataObject.isValid();
            return true;
        }

        @Override
        public boolean isModified() {
//            return dataObject.isModified();
            return false;
        }

        @Override
        public void markModified() throws IOException {
//            findCloneableOpenSupport().setModified(true);
//            dataObject.setModified(true);
        }

        @Override
        public void unmarkModified() {
//            findCloneableOpenSupport().setModified(false);
//            dataObject.setModified(false);
        }

        @Override
        public Date getTime() {
            return time == null ? dataObject.getPrimaryFile().lastModified() : time;
        }

        private void reset() {
            Date old = getTime();
            time = new Date();
            pSupport.firePropertyChange(PROP_TIME, old, time);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
//            dataObject.addPropertyChangeListener(l);
            pSupport.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
//            dataObject.removePropertyChangeListener(l);
            pSupport.removePropertyChangeListener(l);
        }

        @Override
        public void addVetoableChangeListener(VetoableChangeListener l) {
            dataObject.addVetoableChangeListener(l);
        }

        @Override
        public void removeVetoableChangeListener(VetoableChangeListener l) {
            dataObject.removeVetoableChangeListener(l);
        }

        @Override
        public InputStream inputStream() throws IOException {
            return new NullInputStream();
        }

        @Override
        public OutputStream outputStream() throws IOException {
            return null;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(dataObject);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            Object o = in.readObject();
            if (o instanceof JournalDataObject) {
                dataObject = (JournalDataObject) o;
            }
            if (dataObject == null) {
                throw new IOException();
            }
//            remoteModel = dataObject.getLookup().lookup(RemoteReportsModel.class);
//            if (remoteModel == null) {
//                throw new IOException("Could not find RemoteReportsModel");
//            }
        }

        @Override
        public JournalMinutesSupport findCloneableOpenSupport() {
            if (dataObject.isValid()) {
                return dataObject.getLookup().lookup(JournalMinutesSupport.class);
//                try {
//                    return find(dataObject.getLookup());
//                } catch (IOException ex) {
//                    //Will never happen if dataObject is valid.
//                }
            }
            return null;
        }

    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/text/betula-journal-file+xml/Lookup")
    public static class JournalMinutesSupportRegistration implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            JournalDataObject dob = base.lookup(JournalDataObject.class);
            if (dob != null) {
                final JournalMinutesSupport s = new JournalMinutesSupport(dob);
                return Lookups.singleton(s);
            }
            return Lookup.EMPTY;
        }
    }
}
