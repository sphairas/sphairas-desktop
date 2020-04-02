/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.reports.module;

import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@Messages({
    "LBL_Reports_LOADER=Files of Reports"
})
@MIMEResolver.NamespaceRegistration(
        displayName = "#LBL_Reports_LOADER",
        mimeType = "text/betula-reports+xml",
        elementNS = {"http://www.thespheres.org/xsd/betula/reports.xsd"}
)
@DataObject.Registration(
        mimeType = "text/betula-reports+xml",
        iconBase = "org/thespheres/betula/reports/module/reports-stack.png",
        displayName = "#LBL_Reports_LOADER",
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
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/betula-reports+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/betula-reports+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/betula-reports+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/betula-reports+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/text/betula-reports+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/text/betula-reports+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/text/betula-reports+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    )
})
public class ReportsDataObject extends MultiDataObject {

    public static final String MIME = "text/betula-reports+xml";

    public ReportsDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        addEditor();
        //See sources on how to implement multiview
//        registerEditor("text/betula-reports+xml", true);
    }

    private void addEditor() {
        class Factory implements CookieSet.Factory {

            private Object support;

            @Override
            public <T extends Node.Cookie> T createCookie(Class<T> klass) {
                if (klass.isAssignableFrom(ReportsEditorSupport.class)) {
                    synchronized (this) {
                        if (support == null) {
                            support = new ReportsEditorSupport(
                                    ReportsDataObject.this, getPrimaryEntry(),
                                    getCookieSet());
                        }
                    }
                    return klass.cast(support);
                }
                return null;
            }
        }
        getCookieSet().add(ReportsEditorSupport.class, new Factory());
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @MultiViewElement.Registration(
            displayName = "#LBL_Reports_EDITOR",
            iconBase = "org/thespheres/betula/reports/module/reports-stack.png",
            mimeType = "text/betula-reports+xml",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID = "Reports",
            position = 1000
    )
    @Messages("LBL_Reports_EDITOR=Source")
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }

}
