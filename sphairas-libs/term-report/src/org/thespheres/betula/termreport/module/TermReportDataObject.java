/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.module;

import java.io.IOException;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.ui.xml.AbstractXmlContainerDataObject;

@Messages({"TermReportDataObject.displayName=TermReport Files"})
@MIMEResolver.NamespaceRegistration(
        displayName = "#TermReportDataObject.displayName",
        mimeType = "text/term-report-file+xml",
        elementNS = {"http://www.thespheres.org/xsd/betula/term-report-file.xsd"})
@DataObject.Registration(displayName = "#TermReportDataObject.displayName",
        iconBase = "org/thespheres/betula/termreport/resources/betulatrep2_16.png",
        mimeType = "text/term-report-file+xml")
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/term-report-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200),
    @ActionReference(
            path = "Loaders/text/term-report-file+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300),
    @ActionReference(
            path = "Loaders/text/term-report-file+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500),
    @ActionReference(
            path = "Loaders/text/term-report-file+xml/Actions",
            id
            = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600),
    @ActionReference(
            path = "Loaders/text/term-report-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800),
//    @ActionReference(
//            path = "Loaders/text/term-report-file+xml/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
//            position = 900,
//            separatorAfter = 1000),
    @ActionReference(
            path = "Loaders/text/term-report-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200),
    @ActionReference(
            path = "Loaders/text/term-report-file+xml/Actions",
            id = @ActionID(category = "Betula", id = "org.thespheres.betula.listprint.ui.PrintPDF"),
            position = 5100,
            separatorBefore = 5000
    ),
    @ActionReference(
            path = "Loaders/text/term-report-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 20000),
    @ActionReference(
            path = "Loaders/text/term-report-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 30000)
})
public class TermReportDataObject extends AbstractXmlContainerDataObject {

    public static final String TERMREPORT_MIME = "text/term-report-file+xml";
    private Lookup lookup;

    @SuppressWarnings("LeakingThisInConstructor")
    public TermReportDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader, false);
        registerEditor(TERMREPORT_MIME, true);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    protected Node createNodeDelegate() {
        Node ret = new TermReportDataNode(this, getLookup());
        return ret;
    }

    @Override
    public synchronized Lookup getLookup() {
        if (lookup == null) {
            Lookup base = super.getLookup();
            lookup = LookupProviderSupport.createCompositeLookup(base, "Loaders/text/term-report-file+xml/Lookup");
        }
        return lookup;
    }

}
