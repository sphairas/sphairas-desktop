/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.netbeans.api.actions.Savable;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle.Messages;

@Messages({
    "LBL_RemoteReportsDescriptorFile_LOADER=Files of RemoteReportsDescriptorFile"
})
@MIMEResolver.NamespaceRegistration(
        displayName = "#LBL_RemoteReportsDescriptorFile_LOADER",
        mimeType = "text/betula-remote-reports+xml",
        elementName = "remote-reports-descriptor"
)
@DataObject.Registration(
        mimeType = "text/betula-remote-reports+xml",
        displayName = "#LBL_RemoteReportsDescriptorFile_LOADER",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/betula-reports+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/betula-reports+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    )
})
public class RemoteReportsDescriptorFileDataObject extends MultiDataObject implements PropertyChangeListener {

    private final static JAXBContext JAXB;
    public static final String FILE_MIME = "text/betula-remote-reports+xml";
    private RemoteReportsDescriptor descriptor;
    private final Saver saver = new Saver();

    static {
        try {
            JAXB = JAXBContext.newInstance(RemoteReportsDescriptor.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }
    private RemoteReportsSupport support;

    @SuppressWarnings("LeakingThisInConstructor")
    public RemoteReportsDescriptorFileDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        InputStream is = getPrimaryFile().getInputStream();
        try {
            descriptor = (RemoteReportsDescriptor) JAXB.createUnmarshaller().unmarshal(is);
            if (descriptor == null) {
                throw new IOException("RemoteReportsDescriptor is null.");
            }
            support = RemoteReportsSupport.find(this);
            getCookieSet().assign(RemoteReportsSupport.class, support);
            getCookieSet().assign(RemoteReportsModel.class, support.getRemoteReportsModel());
            support.getRemoteReportsModel().addPropertyChangeListener(this);

        } catch (JAXBException ex) {
            throw new IOException(ex);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public RemoteReportsDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RemoteReportsModel.PROP_MODIFIED)) {
            boolean modif = (boolean) evt.getNewValue();
            if (modif) {
                saver.add();
            } else {
                saver.remove();
            }
        }
    }

    private class Saver extends AbstractSavable {

        @Override
        protected String findDisplayName() {
            return support.messageName();
        }

        private void add() {
            getDataObject().getCookieSet().assign(Savable.class, this);
            super.register();
        }

        private void remove() {
            getDataObject().getCookieSet().assign(Savable.class);
            super.unregister();
        }

        @Override
        protected void handleSave() throws IOException {
            support.saveDocument();
        }

        private RemoteReportsDescriptorFileDataObject getDataObject() {
            return RemoteReportsDescriptorFileDataObject.this;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Saver other = (Saver) obj;
            return Objects.equals(getDataObject(), other.getDataObject());
        }

        @Override
        public int hashCode() {
            return getDataObject().hashCode();
        }
    }
}
