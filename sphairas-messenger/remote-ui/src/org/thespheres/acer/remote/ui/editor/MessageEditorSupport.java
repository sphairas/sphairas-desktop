/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.editor;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZoneId;
import java.util.Date;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;
import org.openide.xml.XMLUtil;
import org.thespheres.acer.remote.ui.AbstractMessage;

/**
 *
 * @author boris.heithecker
 */
public class MessageEditorSupport extends CloneableEditorSupport implements EditorCookie, OpenCookie, CloseCookie {

    public static final String MIME = "text/acer-edit-message";
    private final AbstractMessage message;

    public MessageEditorSupport(AbstractMessage msg) {
        super(new Environment(msg), msg.getLookup());
        this.message = msg;
    }

    @Override
    protected CloneableEditor createCloneableEditor() {
        MessagesEditor editor = new MessagesEditor(message);
        WindowManager.getDefault().findMode("output").dockInto(editor);
        return editor;
    }

    public AbstractMessage getMessage() {
        return message;
    }

    public void saveAndSubmit() throws IOException {
        saveDocument();
        message.publish();
        close();
    }

    @Override
    protected boolean canClose() {
        if (env.isModified()) {
            try {
                saveDocument();
            } catch (IOException ex) {
            }
        }
        return super.canClose();
    }

    @Override
    protected String messageSave() {
        return null;
    }

    @Override
    protected String messageName() {
        if (!((Environment) env).isValid()) {
            return ""; // NOI18N
        }
        return DataEditorSupport.annotateName(message.getNodeDelegate().getDisplayName(), false, isModified(), false);
    }

    @Override
    protected String messageHtmlName() {
        if (!((Environment) env).isValid()) {
            return null;
        }

        String name = message.getNodeDelegate().getHtmlDisplayName();
        if (name == null) {
            try {
                name = XMLUtil.toElementContent(message.getNodeDelegate().getDisplayName());
            } catch (CharConversionException ex) {
                return null;
            }
        }

        return DataEditorSupport.annotateName(name, true, isModified(), false);
    }

    @Override
    protected String messageToolTip() {
        // update tooltip
        return null;
    }

    @Messages("MessageEditorSupport.messageOpening=Mitteilung wird geladen.")
    @Override
    protected String messageOpening() {
        return NbBundle.getMessage(MessageEditorSupport.class, "MessageEditorSupport.messageOpening");
    }

    @Override
    protected String messageOpened() {
        return null;
    }

    static class Environment implements CloneableEditorSupport.Env {

        private final AbstractMessage message;
        private boolean valid = true;

        private Environment(AbstractMessage msg) {
            this.message = msg;
        }

        @Override
        public InputStream inputStream() throws IOException {
            return message.getInputStream();
        }

        @Override
        public OutputStream outputStream() throws IOException {
            return message.getOutputStream();
        }

        @Override
        public String getMimeType() {
            return MIME;
        }

        @Override
        public Date getTime() {
            return Date.from(message.getTime().atZone(ZoneId.systemDefault()).toInstant());
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public boolean isModified() {
            return message.isModified();
        }

        @Override
        public void markModified() throws IOException {
            message.setModified(true);
        }

        @Override
        public void unmarkModified() {
            message.setModified(false);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            message.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            message.removePropertyChangeListener(l);
        }

        @Override
        public void addVetoableChangeListener(VetoableChangeListener l) {
        }

        @Override
        public void removeVetoableChangeListener(VetoableChangeListener l) {
        }

        @Override
        public MessageEditorSupport findCloneableOpenSupport() {
            throw new UnsupportedOperationException("Not supported.");
        }

    }
}
