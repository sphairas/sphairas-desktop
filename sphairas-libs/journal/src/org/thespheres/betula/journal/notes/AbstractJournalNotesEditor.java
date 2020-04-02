/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.notes;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorKit;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.thespheres.betula.journal.module.Constants;
import org.thespheres.betula.journal.module.JournalDataObject;
import org.thespheres.betula.ui.util.UIUtilities;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractJournalNotesEditor extends CloneableTopComponent {

    protected final Listener listener = new Listener();
    protected final NbEditorKit kit;
    protected final JEditorPane pane;
    protected JournalDataObject current;
    protected final RequestProcessor RP = new RequestProcessor(JournalNotesEditor2.class);

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    protected AbstractJournalNotesEditor() {
        super();
        kit = MimeLookup.getLookup(Constants.JOURNAL_NOTES_EDITOR_MIME).lookup(NbEditorKit.class);
        pane = new JEditorPane();
        pane.setEditorKit(kit);
        kit.install(pane);
        final EditorUI editorUI = Utilities.getEditorUI(pane);
        final JComponent ec = editorUI.getExtComponent();
        setLayout(new BorderLayout());
        add(ec, BorderLayout.CENTER);
        add(editorUI.getToolBarComponent(), BorderLayout.NORTH);
        updateName();
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        TopComponent.getRegistry().addPropertyChangeListener(listener);
        RP.post(this::initialize);
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
        TopComponent.getRegistry().removePropertyChangeListener(listener);
    }

    protected void initialize() {
        final JournalDataObject jdo = Arrays.stream(TopComponent.getRegistry().getActivatedNodes())
                .flatMap((Node n) -> n.getLookup().lookupAll(JournalDataObject.class).stream())
                .collect(CollectionUtil.singleOrNull());
        if (setCurrentCalendar(jdo)) {
            reload();
        }
    }

    protected void updateView(final StyledDocument d) {
        pane.setDocument(d);
        updateName();
    }

    protected void reload() {
        if (current != null) {
            try {
                final StyledDocument document = (StyledDocument) kit.createDefaultDocument();
                reload(kit, document);
                EventQueue.invokeLater(() -> updateView(document));
            } catch (IOException ioex) {
                Exceptions.printStackTrace(ioex);
            }
        }
    }

    protected abstract void reload(EditorKit kit, StyledDocument doc) throws IOException;

    protected boolean setCurrentCalendar(JournalDataObject m) {
        if (!Objects.equals(m, current)) {
            current = m;
            return true;
        }
        return false;
    }

    protected void updateName() {
        String name;
        if (current == null) {
            name = NbBundle.getMessage(JournalNotesEditor2.class, "JournalNotesEditor2.displayName");
        } else {
            final String n = UIUtilities.findDisplayName(current);
            name = NbBundle.getMessage(JournalNotesEditor2.class, "JournalNotesEditor2.current.displayName", n);
        }
        setName(name);
    }

    final class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case TopComponent.Registry.PROP_ACTIVATED_NODES:
                    RP.post(AbstractJournalNotesEditor.this::initialize);
                    break;
                case TopComponent.Registry.PROP_TC_CLOSED:
                    TopComponent closed = (TopComponent) evt.getNewValue();
                    JournalDataObject uos = null;
                    if (closed != null && (uos = closed.getLookup().lookup(JournalDataObject.class)) != null) {
                        if (Objects.equals(current, uos)) {
                            setCurrentCalendar(null);
                            final StyledDocument document = (StyledDocument) kit.createDefaultDocument();
                            EventQueue.invokeLater(() -> updateView(document));
                        }
                    }
                    break;
            }
        }

    }
}
