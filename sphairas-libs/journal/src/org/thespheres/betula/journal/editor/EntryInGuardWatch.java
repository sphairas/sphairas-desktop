/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.editor;

import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.editor.guards.InteriorSection;
import org.openide.text.NbDocument;
import org.openide.util.WeakListeners;
import org.thespheres.betula.journal.model.EditableRecord;

/**
 *
 * @author boris.heithecker
 */
class EntryInGuardWatch implements DocumentListener {

    static final String ENTRY_IN_GUARD_WATCH_MAP = "EntryInGuardWatch.Map";
    private final WeakReference<EditableRecord> record;
    private final WeakReference<InteriorSection> section;
    private final WeakReference<StyledDocument> document;
    private boolean dirty;

    private EntryInGuardWatch(EditableRecord rec, InteriorSection section, StyledDocument d) {
        this.record = new WeakReference(rec);
        this.section = new WeakReference(section);
        this.document = new WeakReference(d);
    }

    static EntryInGuardWatch create(String id, EditableRecord r, InteriorSection s, final StyledDocument d) {
        EntryInGuardWatch ret = new EntryInGuardWatch(r, s, d);
        d.addDocumentListener(WeakListeners.document(ret, d));
//        r.addPropertyChangeListener(WeakListeners.propertyChange(ret, EditableRecord.PROP_TEXT, ret));
        synchronized (d) {
            Map<String, EntryInGuardWatch> m = (Map<String, EntryInGuardWatch>) d.getProperty(ENTRY_IN_GUARD_WATCH_MAP);
            if (m == null) {
                m = new HashMap<>();
                d.putProperty(ENTRY_IN_GUARD_WATCH_MAP, m);
            }
            m.put(id, ret);
        }
        return ret;
    }

    static void clear(StyledDocument d) {
        GuardedSectionManager manager = GuardedSectionManager.getInstance(d);
        for (GuardedSection s : manager.getGuardedSections()) {
            s.removeSection();
        }
        synchronized (d) {
            final Map<String, EntryInGuardWatch> m = (Map<String, EntryInGuardWatch>) d.getProperty(ENTRY_IN_GUARD_WATCH_MAP);
            if (m != null) {
                m.clear();
            }
        }
    }

    static Optional<EntryInGuardWatch> find(StyledDocument d, String id) {
        synchronized (d) {
            Map<String, EntryInGuardWatch> m = (Map<String, EntryInGuardWatch>) d.getProperty(ENTRY_IN_GUARD_WATCH_MAP);
            if (m != null) {
                return Optional.ofNullable(m.get(id));
            }
        }
        return Optional.empty();
    }

    static List<EntryInGuardWatch> findAll(StyledDocument d) {
        Map<String, EntryInGuardWatch> m;
        synchronized (d) {
            m = (Map<String, EntryInGuardWatch>) d.getProperty(ENTRY_IN_GUARD_WATCH_MAP);
        }
        if (m != null) {
            return m.values().stream().collect(Collectors.toList());
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    private void update(DocumentEvent e) {
        if (e instanceof AbstractDocument.DefaultDocumentEvent) {
            final AbstractDocument.DefaultDocumentEvent edit = (AbstractDocument.DefaultDocumentEvent) e;
            final int offset = edit.getOffset();
            final InteriorSection s = section.get();
            final EditableRecord r = record.get();
            if (s == null || r == null) {
                throw new IllegalStateException();
            }
            if (s.getBodyStartPosition().getOffset() < offset && s.getBodyEndPosition().getOffset() >= offset) {
                final String text = s.getBody();
                r.setListing(text);
            }
        }
    }

    void markDirty() {
        synchronized (this) {
            dirty = true;
        }
    }

    void updateDocumentIfDirty() {
        final boolean d;
        synchronized (this) {
            d = dirty;
        }
        if (d) {
            final InteriorSection s = section.get();
            final EditableRecord r = record.get();
            final StyledDocument doc = document.get();
            if (s == null || r == null || doc == null) {
                throw new IllegalStateException();
            }
            final String body = MinutesSectionsProvider.recordTextToBody(r);
//            s.setBody(body);
            setText(doc, s, body);
        }
    }

    private static void setText(final StyledDocument doc, final InteriorSection s, final String text) {
//        final StyledDocument doc = guards.getDocument();
//        final BadLocationException[] hold = new BadLocationException[]{null};
        Runnable run = new Runnable() {
            public void run() {
                try {
                    final Position begin = s.getBodyStartPosition();
                    int p1 = begin.getOffset();
                    final Position end = s.getBodyEndPosition();
                    int p2 = end.getOffset();
                    int len = text.length();

                    if (len == 0) {
                        if (p2 > p1) {
                            doc.remove(p1, p2 - p1);
                        }
                    } else { 

                        int docLen = doc.getLength();

                        if ((p2 - p1) >= 1) {
                            doc.insertString(p1 + 1, text, null);

                            // [MaM] compute length of inserted string
                            len = doc.getLength() - docLen;
                            doc.remove(p1 + 1 + len, p2 - p1 - 1);
                            doc.remove(p1, 1);
                        } else {
                            // zero or exactly one character:
                            // adjust the positions if they are
                            // biased to not absorb the text inserted at the start/end
                            // it would be ridiculous not to have text set by setText
                            // be part of the bounds.
                            doc.insertString(p1, text, null);

                            // [MaM] compute length of inserted string
                            len = doc.getLength() - docLen;

                            if (p2 > p1) {
                                doc.remove(p1 + len, p2 - p1);
                            }

                            if (begin.getOffset() != p1) {
//                                begin = doc.createPosition(p1);
                            }

                            if ((end.getOffset() - p1) != len) {
//                                end = doc.createPosition(p1 + len);
                            }
//                            assertPositionBounds();
                        }
                    }
                } catch (BadLocationException e) {
//                    hold[0] = e;
                }
            }
        };

//        GuardedSectionsImpl.doRunAtomic(doc, run);
        NbDocument.runAtomic(doc, run);

//        if (hold[0] != null) {
//            throw hold[0];
//        }
    }

//    static void doRunAtomic(Document doc, Runnable r) {
//        AtomicLockDocument ald = LineDocumentUtils.asRequired(doc, AtomicLockDocument.class);
//        ald.runAtomic(r);
//    }
    public void propertyChange(PropertyChangeEvent evt) {
        final InteriorSection s = section.get();
        final EditableRecord r = record.get();
        if (s != null && r != null) {
            final String body = MinutesSectionsProvider.recordTextToBody(r);
//            Mutex.EVENT.writeAccess(() -> {
//                updating = true;
//                s.setBody(body);
//                updating = false;
//            });
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
