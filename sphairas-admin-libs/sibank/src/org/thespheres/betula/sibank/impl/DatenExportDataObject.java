/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.impl;

import org.thespheres.betula.sibank.DatenExportXml;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle.Messages;

@Messages({"LBL_DatenExport_LOADER=Files of DatenExport"})
@MIMEResolver.NamespaceRegistration(
        displayName = "#LBL_DatenExport_LOADER",
        mimeType = "text/sibank-export+xml",
        elementName = "DatenExport")
@DataObject.Registration(
        mimeType = "text/sibank-export+xml",
        //        iconBase = "SET/PATH/TO/ICON/HERE",
        displayName = "#LBL_DatenExport_LOADER",
        position = 300)
public class DatenExportDataObject extends MultiDataObject {

    public static final String MIME = "text/sibank-export+xml";
    private static final JAXBContext JAXB;

    static {
        try {
            JAXB = JAXBContext.newInstance(DatenExportXml.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public DatenExportDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        try {
            loadData();
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    private void loadData() throws Exception {
        DatenExportXml data = (DatenExportXml) JAXB.createUnmarshaller().unmarshal(getPrimaryFile().getInputStream());
        getCookieSet().assign(DatenExportXml.class, data);
    }
}
