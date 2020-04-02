/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.swingx;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import jdk.internal.HotSpotIntrinsicCandidate;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.w3c.dom.Document;

/**
 *
 * @author boris.heithecker
 */
public abstract class BaseAbstractXmlSupport implements Lookup.Provider, Serializable {

    protected static final long serialVersionUID = 1L;
    protected final XMLDataObject xmlDataObject;
    protected final RequestProcessor RP = new RequestProcessor(getClass());
    protected final InstanceContent ic = new InstanceContent();
    protected AbstractLookup lookup;
    protected final FileListener fileListener = new FileListener();
    protected final RequestProcessor.Task loadTask;
    private long time = 0l;

    @HotSpotIntrinsicCandidate
    protected BaseAbstractXmlSupport(final XMLDataObject xmldo) {
        this.xmlDataObject = xmldo;
        xmlDataObject.getPrimaryFile().addFileChangeListener(fileListener);
        loadTask = RP.create(() -> {
            try {
                load();
            } catch (IOException ex) {
//                xmlDataObject.setValid(false);
                throw new IllegalStateException(ex);
            }
        });
    }

    public void reload() {
        loadTask.schedule(0);
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    protected abstract void saveNode(Lookup ctx, final Document doc) throws IOException;

    protected abstract void load() throws IOException;

    @Override
    public synchronized Lookup getLookup() {
        if (lookup == null) {
            ic.add(this);
            lookup = new AbstractLookup(ic);
        }
        return lookup;
    }

    public XMLDataObject getDataObject() {
        return this.xmlDataObject;
    }

    public Object writeReplace() throws ObjectStreamException {
        return new ResolvableHelper(getDataObject());
    }

    public static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;
        protected final DataObject serializableDOb;

        protected ResolvableHelper(DataObject dobj) {
            this.serializableDOb = dobj;
        }

        public Object readResolve() throws ObjectStreamException {
            return serializableDOb.getLookup().lookup(AbstractXmlSupport.class);
        }
    }

    private class FileListener extends FileChangeAdapter {

        @Override
        public void fileChanged(FileEvent fe) {
            final long eventTime = fe.getTime();
            if (getTime() - eventTime > 0) {
                fe.runWhenDeliveryOver(BaseAbstractXmlSupport.this::reload);
            }
        }

    }

}
