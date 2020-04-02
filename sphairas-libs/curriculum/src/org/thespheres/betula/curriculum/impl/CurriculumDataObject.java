/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.impl;

import java.io.IOException;
import java.util.Optional;
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
import org.thespheres.betula.services.ui.util.WriteLockCapability;
import org.thespheres.betula.services.ui.util.WriteLockCapabilitySupport;
import org.thespheres.betula.services.ui.xml.AbstractXmlContainerDataObject;
import org.thespheres.betula.ui.FileInfo;

@Messages({"CurriculumDataObject.label=Curriculum-Dateien"})
@MIMEResolver.NamespaceRegistration(
        displayName = "#CurriculumDataObject.label",
        mimeType = "text/curriculum-file+xml",
        elementNS = {"http://www.thespheres.org/xsd/betula/curriculum-file.xsd"})
@DataObject.Registration(
        mimeType = "text/curriculum-file+xml",
        iconBase = "org/thespheres/betula/curriculum/resources/table-draw.png",
        displayName = "#CurriculumDataObject.label",
        position = 300)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/curriculum-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/curriculum-file+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/curriculum-file+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/curriculum-file+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/curriculum-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/curriculum-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/text/curriculum-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/text/curriculum-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/text/curriculum-file+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    )
})
public class CurriculumDataObject extends AbstractXmlContainerDataObject {

    public static final String CURRICULUM_FILE_MIME = "text/curriculum-file+xml";
    private Lookup lookup;

    public CurriculumDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader, false);
        getCookieSet().assign(WriteLockCapability.class, new WriteLockCapabilitySupport(this));
        registerEditor(CURRICULUM_FILE_MIME, true);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    public synchronized Lookup getLookup() {
        if (lookup == null) {
            final Lookup base = super.getLookup();
            lookup = LookupProviderSupport.createCompositeLookup(base, "Loaders/" + CURRICULUM_FILE_MIME + "/Lookup");
        }
        return lookup;
    }

    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup()) {
            @Override
            public String getDisplayName() {
                return Optional.ofNullable(getLookup().lookup(FileInfo.class))
                        .map(FileInfo::getFileDisplayName)
                        .orElse(super.getDisplayName());
            }
        };
    }

}
