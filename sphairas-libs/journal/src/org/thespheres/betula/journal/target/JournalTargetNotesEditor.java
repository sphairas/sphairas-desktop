/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.target;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.thespheres.betula.journal.notes.*;
import java.io.IOException;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.io.NullInputStream;
import org.openide.windows.TopComponent;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableNote;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"JournalTargetNotesEditor.openAction=Einzelemerkungen (Berichtshefte)",
    "JournalTargetNotesEditor.displayName=Berichtsheft-Einzelbemerkungen",
    "JournalTargetNotesEditor.current.displayName=Berichtsheft-Einzelbemerkungen ({0})"})
@ActionID(category = "Window", id = "org.thespheres.betula.journal.target.JournalTargetNotesEditor")
@ActionReference(path = "Menu/Window/betula-project-local-windows", position = 215)
@ConvertAsProperties(dtd = "-//org.thespheres.betula.journal.target//JournalTargetNotesEditor//EN",
        autostore = false)
@TopComponent.OpenActionRegistration(displayName = "#JournalTargetNotesEditor.openAction",
        preferredID = "JournalTargetNotesEditor")
@TopComponent.Description(preferredID = "JournalTargetNotesEditor",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "markers", openAtStartup = false)
public class JournalTargetNotesEditor extends AbstractJournalNotesEditor {
    
    private final StudentListener studentListener = new StudentListener();
    
    public JournalTargetNotesEditor() {
        super();
        TopComponent.getRegistry().addPropertyChangeListener(studentListener);
    }
    
    @Override
    protected synchronized void reload(EditorKit kit, StyledDocument doc) throws IOException {
        final TargetNotesSectionsProvider nsp = new TargetNotesSectionsProvider(doc);
        try (Reader reader = nsp.createGuardedReader(new NullInputStream(), Charset.defaultCharset())) {
            kit.read(reader, doc, 0);
        } catch (BadLocationException ex) {
            throw new IOException(ex);
        }
    }
    
    private StudentId getStudent() {
        final Optional<EditableParticipant> ep = Arrays.stream(TopComponent.getRegistry().getCurrentNodes())
                .map(n -> n.getLookup().lookupAll(EditableParticipant.class))
                .flatMap(Collection::stream)
                .collect(CollectionUtil.singleton());
        return ep.map(EditableParticipant::getStudentId).orElse(null);
    }
    
    void writeProperties(Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }
    
    void readProperties(Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
    
    class StudentListener implements PropertyChangeListener, LookupListener {
        
        private WeakReference<TopComponent> current;
        private Lookup.Result<EditableParticipant> result;
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(evt.getPropertyName())) {
                final TopComponent tc = TopComponent.getRegistry().getActivated();
                final TopComponent before = current != null ? current.get() : null;
                if (!Objects.equals(tc, before)) {
                    if (result != null) {
                        result.removeLookupListener(this);
                    }
                    current = new WeakReference<>(tc);
                    if (tc != null) {
                        result = tc.getLookup().lookupResult(EditableParticipant.class);
                        result.addLookupListener(this);
                    }
                    resultChanged(null);
                }
                
            }
        }
        
        @Override
        public void resultChanged(LookupEvent ev) {
            EventQueue.invokeLater(() -> JournalTargetNotesEditor.this.reload());
        }
        
    }
    
    class TargetNotesSectionsProvider extends NotesSectionsProvider2 {
        
        public TargetNotesSectionsProvider(StyledDocument d) {
            super(JournalTargetNotesEditor.this, d, current);
        }
        
        @Override
        protected List<EditableNote> selectNotes(final EditableJournal<?, ?> ej) {
            final StudentId sid = getStudent();
            if (sid == null) {
                return Collections.EMPTY_LIST;
            }
            return ej.getEditableRecords().stream()
                    .map(EditableRecord::getEditableNotes)
                    .flatMap(Collection::stream)
                    .filter(n -> n.getStudent() != null && n.getStudent().equals(sid))
                    .collect(Collectors.toList());
        }
        
    }
    
}
