/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.docsrv;

import java.io.IOException;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.XMLDataObject;
import org.openide.util.NbBundle.Messages;

@Messages({
    "LBL_ZeugnisSekINiedersachsenMappe_LOADER=Files of ZeugnisSekINiedersachsenMappe"
})
@MIMEResolver.NamespaceRegistration(
        displayName = "#LBL_ZeugnisSekINiedersachsenMappe_LOADER",
        mimeType = "text/zeugnis-sekundarstufe-niedersachsen-mappe+xml",
        elementName = "NdsZeugnismappeSekundarstufe"
//        elementNS = {"zeugnis-sekundarstufe-niedersachsen-mappe"}
)
@DataObject.Registration(
        mimeType = "text/zeugnis-sekundarstufe-niedersachsen-mappe+xml",
        iconBase = "org/openide/loaders/xmlObject.gif",
        displayName = "#LBL_ZeugnisSekINiedersachsenMappe_LOADER",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen-mappe+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen-mappe+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen-mappe+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen-mappe+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen-mappe+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen-mappe+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen-mappe+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen-mappe+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen-mappe+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    )
})
public class ZgnSekINdsMappeDataObject extends XMLDataObject {

    public ZgnSekINdsMappeDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
//        registerEditor("text/zeugnis-sekundarstufe-niedersachsen-mappe+xml", true);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }
//
//    @MultiViewElement.Registration(
//            displayName = "#LBL_ZeugnisSekINiedersachsenMappe_LOADER",
//            //            iconBase = "path/to/some-icon.png",
//            mimeType = "text/zeugnis-sekundarstufe-niedersachsen-mappe+xml",
//            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
//            preferredID = "ZgnSekINdsMappeDataObject",
//            position = 1000)
//    public static MultiViewEditorElement createEditor(Lookup lkp) {
//        return new MultiViewEditorElement(lkp);
//    }


}
