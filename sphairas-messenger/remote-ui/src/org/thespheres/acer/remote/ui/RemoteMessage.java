/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.StringJoiner;
import javax.swing.Action;
import org.openide.actions.OpenAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.io.NullInputStream;
import org.thespheres.acer.MessageId;
import org.thespheres.betula.document.Signee;

/**
 *
 * @author boris.heithecker
 */
@ActionReferences({
    @ActionReference(
            path = "Editors/text/acer-edit-message/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600),
    @ActionReference(
            path = "Editors/text/acer-edit-message/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300),
    @ActionReference(
            path = "Editors/text/acer-edit-message/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400)
})
public class RemoteMessage extends AbstractMessage {

    private static final DateTimeFormatter CREATION_FORMAT = DateTimeFormatter.ofPattern("dd.MM.YY HH:mm");
    private final RemoteMessagesModel model;
    private final MessageId message;
    private final String channel;
    private boolean locked;
    private ByteArrayOutputStream outputStream;

    RemoteMessage(MessageId message, RemoteMessagesModel model, String text, int priority, LocalDateTime creation, Signee creator, String channel) {
        super(text, priority, creation, creator);
        this.model = model;
        this.message = message;
        this.channel = channel;
    }

    void update(String text, int priority) {
        String textOld = this.text;
        this.text = text;
        boolean modif = !Objects.equals(textOld, this.text);
        int prioOld = this.priority;
        this.priority = priority;
        modif = modif | !Objects.equals(prioOld, this.priority);
        setModified(modif);
    }

    public MessageId getMessageId() {
        return message;
    }

    @Override
    public String getChannelName() {
        return channel;
    }

    public RemoteMessagesModel getRemoteMessagesModel() {
        return model;
    }

    @Override
    protected Node createMessageNode() {
        return new MessageNode();
    }

    @Override
    public LocalDateTime getTime() {
        return getCreation();
    }

    @Override
    public void delete() {
        model.delete(getMessageId());
    }

    @Override
    public void publish() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        synchronized (this) {
            locked = true;
            outputStream = new Output(1024);
            return outputStream;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        synchronized (this) {
            return text != null ? new ByteArrayInputStream(text.getBytes()) : new NullInputStream();
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.message);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteMessage other = (RemoteMessage) obj;
        return Objects.equals(this.message, other.message);
    }

    private class Output extends ByteArrayOutputStream {

        private Output(int i) {
            super(i);
        }

        @Override
        public void close() throws IOException {
            synchronized (this) {
                RemoteMessage.this.text = this.toString();
                RemoteMessage.this.locked = false;
            }
        }

    }

    @ActionReferences({
        @ActionReference(path = "Editors/text/acer-edit-message/Actions", id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"), position = 10000)})
    class MessageNode extends AbstractNode {

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        MessageNode() {
            super(Children.LEAF, RemoteMessage.this.getLookup());
            setName(Long.toString(getMessageId().getId()));
            setIconBaseWithExtension("org/thespheres/acer/remote/ui/resources/mail.png");
        }

        @Override
        public String getHtmlDisplayName() {
            StringJoiner sj = new StringJoiner(" ", "<html>", "</html>");
            sj.add("<font color='0000FF'>" + model.getSigneeFullname(getCreator()) + "</font>");
            sj.add("<font color='AAAAAA'><i>" + CREATION_FORMAT.format(getCreation()) + "</i></font>");
            return sj.toString();
        }

        @Override
        public Action[] getActions(boolean context) {
            return Utilities.actionsForPath("Editors/text/acer-edit-message/Actions").stream()
                    .toArray(Action[]::new);
        }

        @Override
        public boolean canDestroy() {
            return true;
        }

        @Override
        public void destroy() throws IOException {
            delete();
        }

        @Override
        public Action getPreferredAction() {
            return OpenAction.get(OpenAction.class);
        }
    }
}
