/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.odftableimport;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle.Messages;

@Messages({"ODSFileDataObject.displayName=OpenOffice Calc-Dateien"})
@MIMEResolver.ExtensionRegistration(
        displayName = "#ODSFileDataObject.displayName",
        mimeType = "application/vnd.oasis.opendocument.spreadsheet",
        extension = {"ods"})
@DataObject.Registration(
        mimeType = "application/vnd.oasis.opendocument.spreadsheet",
//        iconBase = "SET/PATH/TO/ICON/HERE",
        displayName = "#ODSFileDataObject.displayName",
        position = 300)
public class ODSFileDataObject extends MultiDataObject {

    public static final String MIME = "application/vnd.oasis.opendocument.spreadsheet";

    public ODSFileDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

}
