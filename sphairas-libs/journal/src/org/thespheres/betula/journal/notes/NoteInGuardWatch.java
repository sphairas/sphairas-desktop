/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.notes;

import java.lang.ref.WeakReference;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.InteriorSection;
import org.openide.util.WeakListeners;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.journal.model.EditableNote;

/**
 *
 * @author boris.heithecker
 */
class NoteInGuardWatch implements DocumentListener {

    private final WeakReference<EditableNote> note;
    private final WeakReference<InteriorSection> section;

    private NoteInGuardWatch(EditableNote note, InteriorSection section) {
        this.note = new WeakReference(note);
        this.section = new WeakReference(section);
    }

    static NoteInGuardWatch create(EditableNote n, InteriorSection s, StyledDocument d) {
        NoteInGuardWatch ret = new NoteInGuardWatch(n, s);
        d.addDocumentListener(WeakListeners.document(ret, d));
        return ret;
    }

    private void update(DocumentEvent e) {
        if (e instanceof AbstractDocument.DefaultDocumentEvent) {
            final AbstractDocument.DefaultDocumentEvent edit = (AbstractDocument.DefaultDocumentEvent) e;
            final int offset = edit.getOffset();
            final InteriorSection s = section.get();
            final EditableNote n = note.get();
            if (s == null || n == null) {
                throw new IllegalStateException();
            }
            if (s.getBodyStartPosition().getOffset() < offset && s.getBodyEndPosition().getOffset() >= offset) {
                final String text = s.getBody();
                n.setText(text, Timestamp.now());
            }
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        update(e);
    }

}
