/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsFactory;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;
import org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider;
import org.openide.util.NbBundle;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.model.JournalEditor;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages("MinutesSectionsProvider.recordTextToBody.default=Kein Eintrag")
class MinutesSectionsProvider extends AbstractGuardedSectionsProvider implements GuardedSectionsProvider {

    public static final String NB_ENDOFLINE = "\n";
    private final JournalMinutesSupport context;
    private final static String DEFAULT_BODY = NbBundle.getMessage(MinutesSectionsProvider.class, "MinutesSectionsProvider.recordTextToBody.default");

    private MinutesSectionsProvider(GuardedEditorSupport editor) {
        super(editor, false);
        context = (JournalMinutesSupport) editor;
    }

    @Override
    public char[] writeSections(List<GuardedSection> sections, char[] content) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Result readSections(char[] content) {
        EntryInGuardWatch.clear(context.getDocument());
        try {
            final EditableJournal journal = context.getJournal();
            final JournalEditor editor = context.getDataObject().getLookup().lookup(JournalEditor.class);
            return readSectionsFromModel(journal, editor);
        } catch (IOException ex) {
        }
        return new Result(content, Collections.EMPTY_LIST);
    }

    protected Result readSectionsFromModel(EditableJournal ej, JournalEditor editor) {
        final StringBuilder sb = new StringBuilder();
        final List<GuardedSection> sections = new ArrayList<>();
        if (ej != null) {
            final List<EditableRecord> l = ej.getEditableRecords();
            final int size = l.size();
            int pos = 0;
            for (int i = 0; i < size; i++) {
                EditableRecord r = l.get(i);
                final boolean last = i == size - 1;
                String id = idForIndex(i);
                pos = readOneNote(id, r, sb, editor, pos, sections, last);
            }
        }
//        context.getDocument().putProperty(GuardedNotesCollection.class.getCanonicalName(), coll);
        return new Result(sb.toString().toCharArray(), sections);
    }

    static String idForIndex(final int i) {
        final String id = "record" + Integer.toString(i);
        return id;
    }

    private int readOneNote(String id, EditableRecord er, StringBuilder sb, JournalEditor editor, int pos, List<GuardedSection> sections, boolean last) throws RuntimeException {

        String header;
        if (editor != null) {
            header = editor.formatLocalDate(er, true);
        } else {
            header = er.getRecordId().getId();
        }
        final String body = recordTextToBody(er);
        final String footer = "\u0020" + (!last ? NB_ENDOFLINE : "");
        sb.append(header).append(body).append(footer);
        try {
            int headerStart = pos;
            int headerEnd = pos + header.length();
            int footerStart = headerEnd + body.length();
            int footerEnd = footerStart + footer.length();
            InteriorSection ss = createInteriorSection(id, headerStart, headerEnd, footerStart, footerEnd);
            EntryInGuardWatch.create(id, er, ss, context.getDocument());
            sections.add(ss);
            return footerEnd;
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

    static String recordTextToBody(final EditableRecord er) {
        final String v = er.getListingText();
        return NB_ENDOFLINE + (v != null ? v : DEFAULT_BODY);
    }

    @MimeRegistration(mimeType = "text/betula-journal-file-minutes-editor", service = GuardedSectionsFactory.class)
    public static class Factory extends GuardedSectionsFactory {

        @Override
        public GuardedSectionsProvider create(GuardedEditorSupport editor) {
            return new MinutesSectionsProvider(editor);
        }
    }

}
