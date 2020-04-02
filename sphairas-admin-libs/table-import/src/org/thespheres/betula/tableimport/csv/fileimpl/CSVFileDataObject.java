/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.csv.fileimpl;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle.Messages;

@Messages({"CSVFileDataObject.displayName=csv-Dateien"})
@MIMEResolver.ExtensionRegistration(
        displayName = "#CSVFileDataObject.displayName",
        mimeType = "text/csv",
        extension = {"csv"})
@DataObject.Registration(
        mimeType = "text/csv",
//        iconBase = "SET/PATH/TO/ICON/HERE",
        displayName = "#CSVFileDataObject.displayName",
        position = 300)
public class CSVFileDataObject extends MultiDataObject {

    public static final String MIME = "text/csv";

    public CSVFileDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

}
