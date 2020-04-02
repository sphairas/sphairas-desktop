/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.module2;

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

@Messages({"LBL_Classtest_LOADER=Files of Classtest"})
@MIMEResolver.NamespaceRegistration(
        displayName = "#LBL_Classtest_LOADER",
        mimeType = "text/betula-classtest-file+xml",
        elementNS = {"http://www.thespheres.org/xsd/betula-classtest-file"}
)
@DataObject.Registration(
        mimeType = "text/betula-classtest-file+xml",
        iconBase = "org/thespheres/betula/classtest/resources/betulact16.png",
        displayName = "#LBL_Classtest_LOADER",
        position = 300)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/betula-classtest-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/betula-classtest-file+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/betula-classtest-file+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/betula-classtest-file+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/betula-classtest-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/betula-classtest-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/text/betula-classtest-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/text/betula-classtest-file+xml/Actions",
            id = @ActionID(category = "Betula", id = "org.thespheres.betula.listprint.ui.PrintPDF"),
            position = 5000
    ),
    @ActionReference(
            path = "Loaders/text/text/betula-journal-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 20000
    ),
    @ActionReference(
            path = "Loaders/text/betula-classtest-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 30000
    )
})
public class ClasstestDataObject extends AbstractXmlContainerDataObject {

    public static final String CLASSTEST_MIME = "text/betula-classtest-file+xml";
    private Lookup lookup;

    public ClasstestDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader, false);
        registerEditor(CLASSTEST_MIME, true);
//        initialize();
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    public synchronized Lookup getLookup() {
        if (lookup == null) {
            Lookup base = super.getLookup();
            lookup = LookupProviderSupport.createCompositeLookup(base, "Loaders/" + CLASSTEST_MIME + "/Lookup");
        }
        return lookup;
    }

    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup());
    }

}
