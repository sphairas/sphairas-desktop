/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.notes;

import javax.swing.text.Document;
import org.netbeans.editor.EditorUI;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorKit;
import org.thespheres.betula.journal.module.Constants;

/**
 *
 * @author boris.heithecker
 */
public class Kit extends NbEditorKit {

    static final String DATAIMAGEJPEGBASE64 = "data:image/jpeg;base64,";

    @Override
    public String getContentType() {
        return Constants.JOURNAL_NOTES_EDITOR_MIME;
    }

    @Override
    protected EditorUI createEditorUI() {
        return new NotesEditorUI();
    }

    @Override
    public Document createDefaultDocument() {
        return new NotesDocument(getContentType());
    }

    static class NotesDocument extends NbEditorDocument {

        NotesDocument(String mimeType) {
            super(mimeType);
        }

//        @Override
//        public void insertString(int offset, String text, AttributeSet attrs) throws BadLocationException {
//            int from = 0;
//            int insert = 0;
//            int pos;
//            while ((pos = text.indexOf(DATAIMAGEJPEGBASE64, from)) != -1) {
//                int to = text.indexOf("/Z", from);
//                if (to != -1) {
//                    final String p = text.substring(from, pos);
//                    //
//                    super.insertString(insert, p, attrs);
//                    // Third style for icon/component
//                    final Style style = getStyle(StyleContext.DEFAULT_STYLE);
//                    final String b64 = text.substring(pos + DATAIMAGEJPEGBASE64.length(), to + 2);
//                    final byte[] img = Base64.getDecoder().decode(b64);
//                    Icon icon = new ImageIcon(img);
//                    JLabel label = new JLabel(icon);
//                    StyleConstants.setComponent(style, label);
//                    super.insertString(offset, "ignored", style);
//                    from = to + "/Z".length();
//                    insert = insert + p.length();
//                }
//            }
//        }

    }
}
