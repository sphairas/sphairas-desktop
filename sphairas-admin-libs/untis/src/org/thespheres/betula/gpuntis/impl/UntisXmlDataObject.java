/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.gpuntis.xml.Document;

@Messages({
    "LBL_GPUntisXml_LOADER=Files of GPUntisXml"
})
@MIMEResolver.NamespaceRegistration(
        displayName = "#LBL_GPUntisXml_LOADER",
        mimeType = "text/gpuntis-export+xml",
        //        elementName = "document"
        elementNS = {"https://untis.at/untis/XmlInterface"}
)
@DataObject.Registration(
        mimeType = "text/gpuntis-export+xml",
        //        iconBase = "SET/PATH/TO/ICON/HERE",
        displayName = "#LBL_GPUntisXml_LOADER",
        position = 300
)
//@ActionReferences({
//    @ActionReference(
//            path = "Loaders/text/sibank-export+xml/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
//            position = 100,
//            separatorAfter = 200
//    ),
//    @ActionReference(
//            path = "Loaders/text/sibank-export+xml/Actions",
//            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
//            position = 300
//    ),
//    @ActionReference(
//            path = "Loaders/text/sibank-export+xml/Actions",
//            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
//            position = 400,
//            separatorAfter = 500
//    ),
//    @ActionReference(
//            path = "Loaders/text/sibank-export+xml/Actions",
//            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
//            position = 600
//    ),
//    @ActionReference(
//            path = "Loaders/text/sibank-export+xml/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
//            position = 700,
//            separatorAfter = 800
//    ),
//    @ActionReference(
//            path = "Loaders/text/sibank-export+xml/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
//            position = 900,
//            separatorAfter = 1000
//    ),
//    @ActionReference(
//            path = "Loaders/text/sibank-export+xml/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
//            position = 1100,
//            separatorAfter = 1200
//    ),
//    @ActionReference(
//            path = "Loaders/text/sibank-export+xml/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
//            position = 1300
//    ),
//    @ActionReference(
//            path = "Loaders/text/sibank-export+xml/Actions",
//            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
//            position = 1400
//    )
//})
public class UntisXmlDataObject extends MultiDataObject {

    public static final String MIME = "text/gpuntis-export+xml";
    static final JAXBContext JAXB;

    static {
        try {
            JAXB = JAXBContext.newInstance(Document.class, org.thespheres.betula.gpuntis.xml.Class.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public UntisXmlDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        try {
            loadData();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            throw new IOException(ex);
        }
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    public static byte[] saveData(final Document xml) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            final Marshaller m = JAXB.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            m.marshal(xml, baos);
        } catch (final JAXBException ex) {
            throw new IOException(ex);
        }
        return baos.toByteArray();
    }

    private void loadData() throws Exception {
        final Document data = (Document) JAXB.createUnmarshaller().unmarshal(getPrimaryFile().getInputStream());
        getCookieSet().assign(Document.class, data);
    }
}
