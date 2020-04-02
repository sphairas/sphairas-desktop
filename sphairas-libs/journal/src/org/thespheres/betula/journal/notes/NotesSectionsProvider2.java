/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.notes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;
import org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableNote;
import org.thespheres.betula.journal.model.EditableParticipant;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.journal.module.JournalDataObject;

/**
 *
 * @author boris.heithecker
 */
public class NotesSectionsProvider2 extends AbstractGuardedSectionsProvider implements GuardedSectionsProvider {

    public static final String NB_ENDOFLINE = "\n";
    private final JournalDataObject context;
    private final AbstractJournalNotesEditor support;
    private final StyledDocument document;

    public NotesSectionsProvider2(AbstractJournalNotesEditor sup, StyledDocument d, JournalDataObject obj) {
        super(() -> d, false);
        document = d;
        context = obj;
        support = sup;
    }

    @Override
    public char[] writeSections(List<GuardedSection> sections, char[] content) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Result readSections(char[] content) {
        GuardedSectionManager manager = GuardedSectionManager.getInstance(document);
        for (GuardedSection s : manager.getGuardedSections()) {
            s.removeSection();
        }
        if (context != null && context != null) {
            final EditableJournal ej = context.getLookup().lookup(EditableJournal.class);
            final JournalEditor ed = context.getLookup().lookup(JournalEditor.class);
            return readSectionsFromModel(ej, ed);
        }
        return new Result(content, Collections.EMPTY_LIST);
    }

    protected Result readSectionsFromModel(EditableJournal<?, ?> ej, JournalEditor ed) {

//        NoteInGuardWatch.clear();
        final StringBuilder sb = new StringBuilder();
//        CharBuffer cb = CharBuffer.allocate(content.length);
        final List<GuardedSection> sections = new ArrayList<>();
        if (ej != null) {
            List<EditableNote> l = selectNotes(ej);
            final int size = l.size();
            int pos = 0;
            for (int i = 0; i < size; i++) {
                final EditableNote note = l.get(i);
                final boolean last = i != size - 1;
                final String id = Integer.toString(i);
                pos = readOneNote(id, note, sb, ej, ed, pos, sections, last);
            }
        }
        return new Result(sb.toString().toCharArray(), sections);
    }

    protected List<EditableNote> selectNotes(EditableJournal<?, ?> ej) {
        return Stream.concat(ej.getEditableNotes().stream(), ej.getEditableRecords().stream()
                .flatMap(er -> er.getEditableNotes().stream()))
                .collect(Collectors.toList());
    }

    private int readOneNote(String id, EditableNote n, StringBuilder sb, EditableJournal journal, JournalEditor editor, int pos, List<GuardedSection> sections, boolean last) throws RuntimeException {

        final StringJoiner msg = new StringJoiner(": ");
        if (!"default".equals(n.getScope())) {
            msg.add(n.getScope());
        }
        final EditableRecord er = n.getRecord();
        if (er != null) {
            String d;
            if (editor != null) {
                d = editor.formatLocalDate(er, false);
            } else {
                d = er.getRecordId().getId();
            }
            msg.add(d);
        }
        final StudentId student = n.getStudent();
        final EditableParticipant ep;
        if (student != null && (ep = journal.findParticipant(student)) != null) {
            msg.add(ep.getFullname());
        }
        String text = n.getText();
        if (text.startsWith(Kit.DATAIMAGEJPEGBASE64)) {
            text = "image";
        }
        final String userText = NB_ENDOFLINE + text;
        final String footer = "\u0020" + (last ? NB_ENDOFLINE : "");
        final String header = msg.toString();
        sb.append(header).append(userText).append(footer);
        try {
            int headerStart = pos;
            int headerEnd = pos + header.length();
            int footerStart = headerEnd + userText.length();
            int footerEnd = footerStart + footer.length();
            final InteriorSection ss = createInteriorSection(id, headerStart, headerEnd, footerStart, footerEnd);
            NoteInGuardWatch.create(n, ss, document);
            sections.add(ss);
            return footerEnd;
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

}
