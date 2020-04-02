/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.notes;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author boris.heithecker
 */
class NotesImageHighlights implements DocumentListener {

    private final PositionsBag bag;
    private final NbEditorDocument document;

    private NotesImageHighlights(PositionsBag bag, NbEditorDocument d) {
        this.bag = bag;
        this.document = d;
        try {
            update(document.getText(0, document.getLength()));
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        try {
            String text = document.getText(e.getOffset(), e.getLength());
            update(text);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    void update(String text) {
        if (text.contains("image")) {
            GuardedSectionManager gsm = GuardedSectionManager.getInstance(document);
            for (GuardedSection gs : gsm.getGuardedSections()) {
                if (gs instanceof InteriorSection) {
                    InteriorSection is = (InteriorSection) gs;
//                    System.out.println(is.getBody());
                    if (is.getBody().contains("imag")) {
//                        Style style = document.getStyle(StyleContext.DEFAULT_STYLE);
//                        StyleConstants.setForeground(style, Color.red);
//                        bag.addHighlight(is.getBodyStartPosition(), is.getBodyEndPosition(), style);
Style style2 = document.getStyle(StyleContext.DEFAULT_STYLE);
                        ImageIcon icon = new ImageIcon(ImageUtilities.loadImage("org/thespheres/betula/project/resources/betulaproject16.png"));
                        JLabel label = new JLabel(icon);
                        StyleConstants.setComponent(style2, label);
                        bag.addHighlight(is.getBodyStartPosition(), is.getBodyEndPosition(), style2);
                    }
                }
            }
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    @MimeRegistration(mimeType = "text/betula-journal-file-notes-editor", service = HighlightsLayerFactory.class)
    public static class Factory implements HighlightsLayerFactory {

        @Override
        public HighlightsLayer[] createLayers(Context context) {
            final PositionsBag bag = new PositionsBag(context.getDocument());
            final HighlightsLayer hl = HighlightsLayer.create(NotesImageHighlights.class.getName(), ZOrder.TOP_RACK, true, bag);
            NbEditorDocument nbDoc = (NbEditorDocument) context.getDocument();
            nbDoc.addDocumentListener(new NotesImageHighlights(bag, nbDoc));
            return new HighlightsLayer[]{hl};
        }

    }
}
