/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.exceltableimport;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle.Messages;

@Messages({"MSExcelFileDataObject.displayName=Excel-Dateien"})
@MIMEResolver.ExtensionRegistration(
        displayName = "#MSExcelFileDataObject.displayName",
        mimeType = "application/vnd.ms-excel",
        extension = {"xls", "xlsx"})
@DataObject.Registration(
        mimeType = "application/vnd.ms-excel",
//        iconBase = "SET/PATH/TO/ICON/HERE",
        displayName = "#MSExcelFileDataObject.displayName",
        position = 300)
public class MSExcelFileDataObject extends MultiDataObject {

    public static final String MIME = "application/vnd.ms-excel";

    public MSExcelFileDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

}
