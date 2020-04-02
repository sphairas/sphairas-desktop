/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.project;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.admin.units.MultiUnitOpenSupport;
import org.thespheres.betula.admin.units.xml.RemoteUnitDescriptor;

@Messages({
    "LBL_test_LOADER=Files of test"
})
@MIMEResolver.NamespaceRegistration(
        displayName = "#LBL_test_LOADER",
        mimeType = "text/remote-unit-descriptor+xml",
        elementNS = {"http://www.thespheres.org/xsd/betula/remote-unit-descriptor.xsd"}
)
@DataObject.Registration(
        mimeType = "text/remote-unit-descriptor+xml",
        displayName = "#LBL_test_LOADER",
        iconBase = "org/thespheres/betula/admin/units/resources/tables-stacks.png",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/remote-unit-descriptor+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    )
//    @ActionReference(
//            path = "Loaders/text/betula-reports+xml/Actions",
//            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
//            position = 300
//    ),
//    @ActionReference(
//            path = "Loaders/text/betula-reports+xml/Actions",
//            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
//            position = 400,
//            separatorAfter = 500
//    ),
//    @ActionReference(
//            path = "Loaders/text/betula-reports+xml/Actions",
//            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
//            position = 600
//    ),
//    @ActionReference(
//            path = "Loaders/text/betula-reports+xml/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
//            position = 700,
//            separatorAfter = 800
//    ),
//    @ActionReference(
//            path = "Loaders/text/betula-reports+xml/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
//            position = 900,
//            separatorAfter = 1000
//    ),
//    @ActionReference(
//            path = "Loaders/text/betula-reports+xml/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
//            position = 1100,
//            separatorAfter = 1200
//    ),
//    @ActionReference(
//            path = "Loaders/text/betula-reports+xml/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
//            position = 1300
//    ),
//    @ActionReference(
//            path = "Loaders/text/betula-reports+xml/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
//            position = 1400
//    )
})
public class RemoteUnitDescriptorDataObject extends MultiDataObject {

    private final static JAXBContext JAXB;
    public static final String MIME_TYPE = "text/remote-unit-descriptor+xml";

    static {
        try {
            JAXB = JAXBContext.newInstance(RemoteUnitDescriptor.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public RemoteUnitDescriptorDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        final InputStream is = getPrimaryFile().getInputStream();
        try {
            final RemoteUnitDescriptor mud = (RemoteUnitDescriptor) JAXB.createUnmarshaller().unmarshal(is);
            if (mud == null) {
                throw new IOException("MultiUnitDescriptor is null.");
            }
            final MultiUnitOpenSupport muos = MultiUnitOpenSupport.create(this, mud);
            muos.getLoadingProperties().put("by-primary-suffix", "true");
            getCookieSet().assign(MultiUnitOpenSupport.class, muos);
            getCookieSet().assign(RemoteUnitDescriptor.class, mud);

        } catch (JAXBException ex) {
            throw new IOException(ex);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    protected Node createNodeDelegate() {
        if (associateLookup() >= 1) {
            return new RemoteUnitDescriptorNode(this, getLookup());
        }
        return super.createNodeDelegate();
    }

    @NodeFactory.Registration(projectType = "org-thespheres-betula-project-local", position = 6000)
    public static class RemoteUnitDescriptorUnitNodeFactory implements NodeFactory {

        @Override
        public NodeList<DataObject> createNodes(Project p) {
            return new RemoteUnitNodeList(p);
        }
    }
}
