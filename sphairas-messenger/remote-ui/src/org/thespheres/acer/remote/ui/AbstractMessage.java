/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.thespheres.acer.remote.ui.editor.MessageEditorSupport;
import org.thespheres.betula.document.Signee;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractMessage implements Lookup.Provider {

    public static final String PROP_MODIFIED = "modified";
    protected String text;
    protected int priority = 0;
    protected LocalDateTime creation;
    protected Signee creator;
    private Node node;
    private boolean modified;
    protected final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    protected final InstanceContent ic = new InstanceContent();
    private Lookup lookup;
    private MessageEditorSupport support;
    protected boolean confidential = false;

    protected AbstractMessage() {
        support = new MessageEditorSupport(this);
        ic.add(support);
    }

    protected AbstractMessage(String text, int priority, LocalDateTime creation, Signee creator) {
        this();
        this.text = text;
        this.priority = priority;
        this.creation = creation;
        this.creator = creator;
    }

    public Node getNodeDelegate() {
        if (node == null) {
            node = createMessageNode();
        }
        return node;
    }

    @Override
    public synchronized Lookup getLookup() {
        if (lookup == null) {
            ic.add(this);
            lookup = new AbstractLookup(ic);
        }
        return lookup;
    }

    public abstract String getChannelName();

    protected abstract Node createMessageNode();

    public synchronized MessageEditorSupport getMessageEditorSupport() {
        return support;
    }

    public String getMessageText() {
        return text;
    }

    public int getPriority() {
        return priority;
    }

    public LocalDateTime getCreation() {
        return creation;
    }

    public Signee getCreator() {
        return creator;
    }

    public void setModified(boolean b) {
        boolean old = this.modified;
        this.modified = b;
        pSupport.firePropertyChange(PROP_MODIFIED, old, modified);
    }

    public boolean isModified() {
        return modified;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pSupport.removePropertyChangeListener(listener);
    }

    public abstract LocalDateTime getTime();

    public abstract void delete();

    public abstract void publish();

    public abstract OutputStream getOutputStream() throws IOException;

    public abstract InputStream getInputStream() throws IOException;

    public boolean isConfidential() {
        return confidential;
    }

    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    public interface Callback {

        public void publish();
    }
}
