/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.editor;

import com.google.common.eventbus.Subscribe;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import javax.swing.text.StyledDocument;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableRecord;

/**
 *
 * @author boris.heithecker
 */
@MultiViewElement.Registration(
        displayName = "#JournalMinutesEditor.displayname",
        iconBase = "org/thespheres/betula/journal/resources/betulacal16.png",
        mimeType = "text/betula-journal-file+xml",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "JournalMinutesEditor",
        position = 3000)
@NbBundle.Messages({"JournalMinutesEditor.displayname=Protokolle"})
public class JournalMinutesEditor extends MultiViewEditorElement implements Serializable {

    public static final long serialVersionUID = 1l;
    private MultiViewElementCallback callback;
    private final JournalMinutesSupport support;
    private boolean listening = false;

    public JournalMinutesEditor(Lookup lkp) throws IOException {
        super(lkp);
        support = lkp.lookup(JournalMinutesSupport.class);
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        EventQueue.invokeLater(() -> WindowManager.getDefault().findTopComponent("navigatorTC").open());
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        final EditableJournal journal = support.getDataObject().getLookup().lookup(EditableJournal.class);
        if (journal != null && listening) {
            journal.getEventBus().unregister(this);
            listening = false;
        }
        final StyledDocument d = support.getDocument();
        if (d != null) {
            EntryInGuardWatch.findAll(d).stream()
                    .forEach(EntryInGuardWatch::updateDocumentIfDirty);
        }
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
        final EditableJournal journal = support.getDataObject().getLookup().lookup(EditableJournal.class);
        if (journal != null) {
            journal.getEventBus().register(this);
            listening = true;
        }
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
        updateName();
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Subscribe
    public void onRecordTextChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof EditableRecord && EditableRecord.PROP_TEXT.equals(evt.getPropertyName())) {
//            ((JournalMinutesSupport.Environment) env).reset();
            final int index = ((EditableRecord) evt.getSource()).getIndex();
            final String id = MinutesSectionsProvider.idForIndex(index);
            EntryInGuardWatch.find(support.getDocument(), id).ifPresent(EntryInGuardWatch::markDirty);
        }
    }

    protected Object writeReplace() throws ObjectStreamException {
        return new Replacer(this);
    }

    private static final class Replacer implements Serializable {

        static final long serialVersionUID = 1l;
        transient JournalMinutesSupport support;

        private Replacer(JournalMinutesEditor ed) {
            this.support = ed.support;
        }

        private void writeObject(ObjectOutputStream oos) throws IOException, ClassNotFoundException {
            oos.writeObject(support.getDataObject());
        }

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            Object in = ois.readObject();
            if (in instanceof DataObject) {
                final JournalMinutesSupport s = ((DataObject) in).getLookup().lookup(JournalMinutesSupport.class);
                if (s == null) {
                    throw new IOException("No JournalMinutesSupport found.");
                }
                support = s;
            }
        }

        private Object readResolve() throws ObjectStreamException {
            return support.createCloneableEditor();
        }
    }

}
