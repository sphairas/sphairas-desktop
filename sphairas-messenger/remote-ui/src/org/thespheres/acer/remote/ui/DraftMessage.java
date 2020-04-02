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
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author boris.heithecker
 */
class DraftMessage extends AbstractMessage {

    private static int count = 1;
    private final int id;
    private final RemoteChannel rChannel;
    LocalDateTime time;
    private ByteArrayOutputStream outputStream;
    private boolean locked = false;
    private Callback callback;

    DraftMessage(RemoteChannel rc) {
        synchronized (getClass()) {
            this.id = count++;
        }
        this.text = NbBundle.getMessage(DraftMessage.class, "DraftMessage.text.empty");
        this.rChannel = rc;
        this.time = LocalDateTime.now();
    }

    void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public LocalDateTime getTime() {
        return this.time;
    }

    @Override
    public String getChannelName() {
        return rChannel.getChannelName();
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
            return new ByteArrayInputStream(text.getBytes());
        }
    }

    @Override
    protected Node createMessageNode() {
        return new DraftNode();
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void setCallback(Callback cb) {
        this.callback = cb;
    }

    @Override
    public void publish() {
        if (callback == null) {
            throw new IllegalStateException("No callback set.");
        }
        callback.publish();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.id;
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
        final DraftMessage other = (DraftMessage) obj;
        return this.id == other.id;
    }

    private class Output extends ByteArrayOutputStream {

        private Output(int i) {
            super(i);
        }

        @Override
        public void close() throws IOException {
            synchronized (this) {
                DraftMessage.this.text = this.toString();
                DraftMessage.this.time = LocalDateTime.now();
                DraftMessage.this.locked = false;
            }
        }

    }

    class DraftNode extends AbstractNode {

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        private DraftNode() {
            super(Children.LEAF, Lookups.singleton(DraftMessage.this));
            setName("draft-" + Integer.toString(id));
            setIconBaseWithExtension("org/thespheres/acer/remote/ui/resources/mail--pencil.png");
        }

    }
}
