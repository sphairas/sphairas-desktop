/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.docfile;

import java.io.IOException;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.ui.xml.AbstractXmlContainerDataObject;

@Messages({"ContainerDataObject.displayName=Austauschdaten-Datei"})
@MIMEResolver.NamespaceRegistration(
        displayName = "#ContainerDataObject.displayName",
        mimeType = "text/betula-document-container+xml",
        elementNS = {"http://www.thespheres.org/xsd/betula/container.xsd"})
@DataObject.Registration(mimeType = "text/betula-document-container+xml",
        iconBase = "org/thespheres/betula/services/ui/resources/arrow-continue-180-top.png",
        displayName = "#ContainerDataObject.displayName",
        position = 300)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/betula-document-container+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200),
    @ActionReference(
            path = "Loaders/text/betula-document-container+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300),
    @ActionReference(
            path = "Loaders/text/betula-document-container+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500),
    @ActionReference(
            path = "Loaders/text/betula-document-container+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600),
    @ActionReference(
            path = "Loaders/text/betula-document-container+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800),
    @ActionReference(
            path = "Loaders/text/betula-document-container+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000),
    @ActionReference(
            path = "Loaders/text/betula-document-container+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200),
    @ActionReference(
            path = "Loaders/text/betula-document-container+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300),
    @ActionReference(
            path = "Loaders/text/betula-document-container+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400)
})
public class ContainerDataObject extends AbstractXmlContainerDataObject {

    public static final String CONTAINER_MIME = "text/betula-document-container+xml";
    private Lookup lookup;

    public ContainerDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor(CONTAINER_MIME, true);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    public synchronized Lookup getLookup() {
        if (lookup == null) {
            Lookup base = super.getLookup();
            lookup = LookupProviderSupport.createCompositeLookup(base, "Loaders/" + CONTAINER_MIME + "/Lookup");
        }
        return lookup;
    }

    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup());
    }
//    @MultiViewElement.Registration(
//            displayName = "#LBL_Container_EDITOR",
//            iconBase = "org/thespheres/betula/docfile/module/crown.png",
//            mimeType = "text/betula-document-container+xml",
//            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
//            preferredID = "Container",
//            position = 1000)
//    @Messages("LBL_Container_EDITOR=Source")
//    public static MultiViewEditorElement createEditor(Lookup lkp) {
//        return new MultiViewEditorElement(lkp);
//    }
}
