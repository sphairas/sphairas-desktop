/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.openide.loaders.DataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Lookup;
import org.openide.xml.XMLUtil;
import org.thespheres.betula.services.ui.xml.XMLUtilities;
import org.thespheres.betula.services.ui.xml.XmlBeforeSaveCallback;
import org.thespheres.betula.services.ui.xml.fi.XmlFileInfo;
import org.thespheres.betula.ui.swingx.BaseAbstractXmlSupport;
import org.thespheres.betula.ui.util.JAXBUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author boris.heithecker
 */
public class FileInfoSupport extends BaseAbstractXmlSupport {

    protected static final long serialVersionUID = 1L;
    public static final JAXBContext JAXB;
    public static final Class[] JAXB_TYPES;

    static {
        try {
            JAXB_TYPES = JAXBUtil.lookupJAXBTypes("XmlFileInfo", XmlFileInfo.class);
            JAXB = JAXBContext.newInstance(JAXB_TYPES);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private FileInfoSupport(final XMLDataObject xmldo) {
        super(xmldo);
    }

    public static Lookup create(XMLDataObject xmldo) {
        final FileInfoSupport cts = new FileInfoSupport(xmldo);
        cts.reload();
        return cts.getLookup();
    }

    @Override
    protected void saveNode(Lookup ctx, Document doc) throws IOException {
        final DataObject dob = ctx.lookup(DataObject.class);
        if (dob == null || !dob.equals(xmlDataObject)) {
            return;
        }
        final XmlFileInfo report = getLookup().lookup(XmlFileInfo.class);
        if (report != null) {
            XMLUtilities.removeElement(doc, "file-info", "http://www.thespheres.org/xsd/betula/file-info.xsd");
            try {
                JAXB.createMarshaller().marshal(report, doc.getDocumentElement());
            } catch (JAXBException ex) {
                throw new IOException(ex);
            }
        }
    }

    @Override
    protected void load() throws IOException {
        final Document doc;
        try {
            doc = getDataObject().getDocument();
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
        final Element e;
        try {
            e = findFileInfoElement(doc);
        } catch (IllegalArgumentException ex) {
            throw new IOException(ex);
        }
        final XmlFileInfo node;
        if (e != null) {
            try {
                final Unmarshaller um = JAXB.createUnmarshaller();
                synchronized (doc) {
                    node = (XmlFileInfo) um.unmarshal(e);
                }
                //TODO remove previous.......
                synchronized (this) {
                    ic.add(node);
                    ic.add((XmlBeforeSaveCallback) (lkp, dc) -> saveNode(lkp, dc));
                }
            } catch (JAXBException ex) {
                throw new IOException(ex);
            } finally {
                getDataObject().setModified(false);
            }
        }
    }

    private static Element findFileInfoElement(final Document doc) throws IllegalArgumentException {
        //throws IllegalArgumentException if there is multiple elements of the same name
        synchronized (doc) {
            return XMLUtil.findElement(doc.getDocumentElement(), "file-info", "http://www.thespheres.org/xsd/betula/file-info.xsd");
        }
    }

}
