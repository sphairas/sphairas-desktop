/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.pdf;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.actions.Openable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@DataObject.Registration(displayName = "#CTL_PDFDataObject", iconBase = "org/thespheres/pdf/resources/file_extension_pdf.png", mimeType = "application/pdf")
@Messages({"CTL_PDFDataObject=PDFDataObject", "CTL_PDFDataObject.Registration=PDFDataObject registration"})
@MIMEResolver.ExtensionRegistration(displayName = "#CTL_PDFDataObject.Registration", mimeType = "application/pdf", extension = "pdf")
public class PDFDataObject extends MultiDataObject implements Openable {

    @SuppressWarnings("LeakingThisInConstructor")
    public PDFDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
//        getCookieSet().assign(PDFOpenSupport.class, PDFOpenSupport.create(this));
    }

    @Override
    protected int associateLookup() {
        return 1;//DataNode icon Mimefactory ---> getcookieset.assign...
    }

    @Override
    protected Node createNodeDelegate() {
        return new PDFDataNode(this, Children.LEAF, getLookup());
    }

    @Override
    public void open() {
        try {
            File file = FileUtil.toFile(getPrimaryFile());
            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
