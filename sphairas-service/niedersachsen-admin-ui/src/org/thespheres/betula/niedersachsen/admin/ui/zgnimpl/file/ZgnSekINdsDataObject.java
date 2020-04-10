/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.file;

import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.spi.actions.AbstractSavable;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.thespheres.betula.niedersachsen.NdsZeugnisFormular;

@Messages({"ZgnSekINdsDataObject.name=Zeugnisdaten Niedersachsen",
    "ZgnSekINdsDataObject.editor.name=Quelltext"})
@MIMEResolver.NamespaceRegistration(
        displayName = "#ZgnSekINdsDataObject.name",
        mimeType = "text/zeugnis-sekundarstufe-niedersachsen+xml",
        elementNS = {"http://www.thespheres.org/xsd/niedersachsen/zeugnisse.xsd"}
)
@DataObject.Registration(
        mimeType = "text/zeugnis-sekundarstufe-niedersachsen+xml",
        iconBase = "org/thespheres/betula/niedersachsen/admin/ui/zgnimpl/file/rorange.png",
        displayName = "#ZgnSekINdsDataObject.name",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    )
})
public class ZgnSekINdsDataObject extends XMLDataObject {

    private final Saver saver = new Saver();
    private Lookup lookup;
    protected volatile boolean modif;

    public ZgnSekINdsDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor("text/zeugnis-sekundarstufe-niedersachsen+xml", true);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    public synchronized Lookup getLookup() {
        if (lookup == null) {
            final Lookup base = super.getLookup();
            lookup = LookupProviderSupport.createCompositeLookup(base, "Loaders/" + NdsZeugnisFormular.ZeugnisMappe.MAPPE_MIME + "/Lookup");
        }
        return lookup;
    }

    @Override
    public void setModified(boolean modif) {
        if (this.modif != modif) {
            this.modif = modif;
            if (this.modif) {
                saver.registerSavable();
            } else {
                saver.unregisterSavable();
            }
            firePropertyChange(DataObject.PROP_MODIFIED, !modif ? Boolean.TRUE : Boolean.FALSE, modif ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    @MultiViewElement.Registration(
            displayName = "#ZgnSekINdsDataObject.editor.name",
            iconBase = "org/thespheres/betula/niedersachsen/admin/ui/zgnimpl/file/rorange.png",
            mimeType = "text/zeugnis-sekundarstufe-niedersachsen+xml",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID = "ZgnSekINdsDataObject",
            position = 1000
    )
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }

    private class Saver extends AbstractSavable {

        @Override
        protected String findDisplayName() {
            return getDataObject().getNodeDelegate().getDisplayName();
        }

        private void registerSavable() {
            register();
            getCookieSet().assign(SaveCookie.class, this);
        }

        private void unregisterSavable() {
            unregister();
            getCookieSet().assign(SaveCookie.class);
        }

        @Override
        protected void handleSave() throws IOException {
            getDataObject().getLookup().lookup(ZgnSekINdsSupport.class).save();
            getDataObject().setModified(false);
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof Saver) {
                return ((Saver) other).getDataObject().equals(ZgnSekINdsDataObject.this);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getDataObject().hashCode();
        }

        private ZgnSekINdsDataObject getDataObject() {
            return ZgnSekINdsDataObject.this;
        }

    }

}
