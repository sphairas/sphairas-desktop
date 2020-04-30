/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Comparator;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.XMLDataObject;
import org.openide.xml.XMLUtil;
import org.thespheres.betula.ui.swingx.BaseAbstractXmlSupport;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author boris.heithecker
 */
public class AbstractXmlContainerDataObject extends XMLDataObject {

    private final FileSaver saver = new FileSaver();
    protected volatile boolean modif;

    protected AbstractXmlContainerDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super(fo, loader);
    }

    protected AbstractXmlContainerDataObject(FileObject fo, MultiFileLoader loader, boolean registerEditor) throws DataObjectExistsException {
        super(fo, loader, registerEditor);
    }

    @Override
    public boolean isModified() {
        return modif;
    }

    protected FileSaver getSaver() {
        return saver;
    }

    @Override
    public void setModified(boolean modif) {
        if (this.modif != modif) {
            this.modif = modif;
            if (this.modif) {
                getSaver().registerSavable();
            } else {
                getSaver().unregisterSavable();
            }
            firePropertyChange(DataObject.PROP_MODIFIED, !modif ? Boolean.TRUE : Boolean.FALSE, modif ? Boolean.TRUE : Boolean.FALSE);
        }
    }

//    public void setLastModified()
    protected void save() throws IOException {
        Document d;
        try {
            d = getDocument();
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
        final Document doc = d;
        final XmlBeforeSaveCallback[] cb = getLookup().lookupAll(XmlBeforeSaveCallback.class).stream()
                .sorted(Comparator.comparingInt(XmlBeforeSaveCallback::position))
                .toArray(XmlBeforeSaveCallback[]::new);
        for (XmlBeforeSaveCallback xbsc : cb) {
            xbsc.run(getLookup(), doc);
        }
        final FileObject file = getPrimaryFile();
        final Collection<? extends BaseAbstractXmlSupport> supports = getLookup().lookupAll(BaseAbstractXmlSupport.class);
        file.getFileSystem().runAtomicAction(() -> {
            final FileLock l = file.lock();
            try (final OutputStream os = file.getOutputStream(l)) {
                XMLUtil.write(doc, os, "utf-8");
                supports.forEach(s -> s.setTime(file.lastModified().getTime()));
                setModified(false);
            } finally {
                l.releaseLock();
            }
        });

    }

    protected class FileSaver extends AbstractSavable implements SaveCookie {

        @Override
        protected String findDisplayName() {
            return AbstractXmlContainerDataObject.this.getPrimaryFile().getNameExt();
        }

        private void registerSavable() {
            register();
            getCookieSet().assign(SaveCookie.class, this);
        }

        private void unregisterSavable() {
            unregister();
            getCookieSet().assign(SaveCookie.class);
        }

        @Override
        protected void handleSave() throws IOException {
            AbstractXmlContainerDataObject.this.save();
        }

        private DataObject getDataObject() {
            return AbstractXmlContainerDataObject.this;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof FileSaver) {
                FileSaver dos = (FileSaver) other;
                return getDataObject().equals(dos.getDataObject());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getDataObject().hashCode();
        }
    }
}
