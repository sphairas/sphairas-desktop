/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig.impl;

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
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@Messages({"LBL_XmlMarkerConventionDefinition_LOADER=Files of XmlMarkerConventionDefinition"})
@MIMEResolver.NamespaceRegistration(
        displayName = "#LBL_XmlMarkerConventionDefinition_LOADER",
        mimeType = "text/marker-convention-definition+xml",
        elementNS = {"http://www.thespheres.org/xsd/betula/convention.xsd"},
        elementName = "marker-convention-definition"
)
@DataObject.Registration(
        mimeType = "text/marker-convention-definition+xml",
        iconBase = "org/thespheres/betula/adminconfig/impl/exclamation--frame.png",
        displayName = "#LBL_XmlMarkerConventionDefinition_LOADER",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/marker-convention-definition+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/marker-convention-definition+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/marker-convention-definition+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/marker-convention-definition+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/marker-convention-definition+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/marker-convention-definition+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/text/marker-convention-definition+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/text/marker-convention-definition+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/text/marker-convention-definition+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    )
})
public class XmlMarkerConventionDefinitionDataObject extends MultiDataObject {

    public XmlMarkerConventionDefinitionDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor("text/marker-convention-definition+xml", true);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @MultiViewElement.Registration(
            displayName = "#LBL_XmlMarkerConventionDefinition_EDITOR",
            iconBase = "org/thespheres/betula/adminconfig/impl/exclamation--frame.png",
            mimeType = "text/marker-convention-definition+xml",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID = "XmlMarkerConventionDefinition",
            position = 1000
    )
    @Messages("LBL_XmlMarkerConventionDefinition_EDITOR=Source")
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }

}
