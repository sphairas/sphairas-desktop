/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.swing.JScrollPane;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.journal.module.JournalDataObject;
import org.thespheres.betula.ui.swingx.AbstractTableElement;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public abstract class JournalOutputTable extends AbstractTableElement {

    protected final JScrollPane scrollPane;
    protected JournalDataObject current;
    private final Listener listener = new Listener();

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    protected JournalOutputTable() {
        super();
        scrollPane = new javax.swing.JScrollPane();
        setLayout(new java.awt.BorderLayout());
        add(scrollPane, java.awt.BorderLayout.CENTER);
        scrollPane.setViewportView(table);
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        TopComponent.getRegistry().addPropertyChangeListener(listener);
        WindowManager.getDefault().invokeWhenUIReady(this::onChange);
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
        TopComponent.getRegistry().removePropertyChangeListener(listener);
        onChange();
    }

    protected synchronized void onChange() {
        //        markerComboBox.removeActionListener(markerListener);
        //        customNoteButton.removeActionListener(cnbl);

        final JournalDataObject jdo = Arrays.stream(TopComponent.getRegistry().getActivatedNodes())
                .flatMap((Node n) -> n.getLookup().lookupAll(JournalDataObject.class).stream())
                .collect(CollectionUtil.singleOrNull());
        setCurrentCalendar(jdo);

//        selectionHighlighter.updateRecord();
        //        markerComboBox.addActionListener(markerListener);
        //        customNoteButton.addActionListener(cnbl);
    }

    protected abstract void setCurrentCalendar(JournalDataObject m);

    @Override
    protected Node getNodeForRow(int row) {
        if (current != null) {
            final EditableJournal<?, ?> j = current.getLookup().lookup(EditableJournal.class);
            final EditableParticipant er = j.getEditableParticipants().get(row);
            return er.getNodeDelegate();
        }
        return null;
    }

    @Override
    protected void activatedNodes(List<Node> selected) {
    }

    final class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case TopComponent.Registry.PROP_ACTIVATED_NODES:
                    onChange();
                    break;
                case TopComponent.Registry.PROP_TC_CLOSED:
                    TopComponent closed = (TopComponent) evt.getNewValue();
                    JournalDataObject uos = null;
                    if (closed != null && (uos = closed.getLookup().lookup(JournalDataObject.class)) != null) {
                        if (Objects.equals(current, uos)) {
                            setCurrentCalendar(null);
                        }
                    }
                    break;
            }
        }

    }
}
