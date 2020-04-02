/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.module;

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

@MIMEResolver.NamespaceRegistration(
        displayName = "#JournalDataObject.displayName",
        mimeType = "text/betula-journal-file+xml",
        elementNS = {"http://www.thespheres.org/xsd/betula-journal-file.xsd"})
@DataObject.Registration(
        mimeType = "text/betula-journal-file+xml",
        iconBase = "org/thespheres/betula/journal/resources/betulacal16.png",
        displayName = "#JournalDataObject.displayName",
        position = 300)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/betula-journal-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/betula-journal-file+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/betula-journal-file+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/betula-journal-file+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/betula-journal-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/betula-journal-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/text/betula-journal-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/text/betula-journal-file+xml/Actions",
            id = @ActionID(category = "Betula", id = "org.thespheres.betula.listprint.ui.PrintPDF"),
            position = 5100,
            separatorBefore = 5000
    ),
    @ActionReference(
            path = "Loaders/text/betula-journal-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 20000
    ),
    @ActionReference(
            path = "Loaders/text/betula-journal-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 30000
    )
})
@Messages({"JournalDataObject.displayName=Berichtsheft-Dateien"})
public class JournalDataObject extends AbstractXmlContainerDataObject {

    public static final String JOURNAL_MIME = "text/betula-journal-file+xml";
    private final Lookup[] lookup = new Lookup[]{null};

    public JournalDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader, false);
        registerEditor(JOURNAL_MIME, true);
//        initialize();
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    public Lookup getLookup() {
        synchronized (lookup) {
            if (lookup[0] == null) {
                final Lookup base = super.getLookup();
                lookup[0] = LookupProviderSupport.createCompositeLookup(base, "Loaders/" + JOURNAL_MIME + "/Lookup");
            }
        }
        return lookup[0];
    }

    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup());
    }

}
