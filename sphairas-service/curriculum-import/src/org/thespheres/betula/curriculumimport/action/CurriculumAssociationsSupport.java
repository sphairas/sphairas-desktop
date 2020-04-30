/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.action;

import org.thespheres.betula.curriculumimport.xml.CurriculumAssoziationenCollection;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.netbeans.spi.project.LookupProvider;
import org.openide.loaders.DataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.thespheres.betula.curriculumimport.xml.CurriculumAssoziation;
import org.thespheres.betula.services.ui.xml.XMLUtilities;
import org.thespheres.betula.services.ui.xml.XmlBeforeSaveCallback;
import org.thespheres.betula.ui.swingx.BaseAbstractXmlSupport;
import org.thespheres.betula.ui.util.JAXBUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author boris.heithecker
 */
public class CurriculumAssociationsSupport extends BaseAbstractXmlSupport {

    public static final JAXBContext JAXB;
    public static final Class[] JAXB_TYPES;

    static {
        try {
            JAXB_TYPES = JAXBUtil.lookupJAXBTypes("CurriculumAssociationsSupport", CurriculumAssoziationenCollection.class, CurriculumAssoziation.class);
            JAXB = JAXBContext.newInstance(JAXB_TYPES);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private CurriculumAssociationsSupport(final XMLDataObject xmldo) {
        super(xmldo);
    }

    static Lookup create(final XMLDataObject xmldo) {
        final CurriculumAssociationsSupport cts = new CurriculumAssociationsSupport(xmldo);
        cts.reload();
        return cts.getLookup();
    }

    @Override
    protected void saveNode(final Lookup ctx, final Document doc) throws IOException {
        final DataObject dob = ctx.lookup(DataObject.class);
        if (dob == null || !dob.equals(xmlDataObject)) {
            return;
        }
        final CurriculumAssoziationenCollection report = getLookup().lookup(CurriculumAssoziationenCollection.class);
        XMLUtilities.removeElement(doc, "curriculum-associations", "http://www.thespheres.org/xsd/betula/curriculum-import.xsd");
        if (report == null || report.getItems().isEmpty()) {
            return;
        }
        try {
            JAXB.createMarshaller().marshal(report, doc.getDocumentElement());
        } catch (JAXBException ex) {
            throw new IOException(ex);
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
            e = findCurriculumAssociationsElement(doc);
        } catch (IllegalArgumentException ex) {
            throw new IOException(ex);
        }
        final CurriculumAssoziationenCollection node;
        try {
            final Unmarshaller um = JAXB.createUnmarshaller();
            synchronized (doc) {
                node = e == null ? null : (CurriculumAssoziationenCollection) um.unmarshal(e);
            }
            //TODO remove previous.......
            final CurriculumAssoziationenCollection beforeCurr = getLookup().lookup(CurriculumAssoziationenCollection.class);
            synchronized (this) {
                if (beforeCurr != null) {
                    ic.remove(beforeCurr);
                }
                if (node != null) {
                    ic.add(node);
                } else {
                    ic.add(new CurriculumAssoziationenCollection());
                }
                ic.add((XmlBeforeSaveCallback) (lkp, dc) -> saveNode(lkp, dc));
            }
        } catch (JAXBException ex) {
            throw new IOException(ex);
        } finally {
            getDataObject().setModified(false);
        }
    }

    private static Element findCurriculumAssociationsElement(final Document doc) throws IllegalArgumentException {
        //throws IllegalArgumentException if there is multiple elements of the same name
        synchronized (doc) {
            return XMLUtil.findElement(doc.getDocumentElement(), "curriculum-associations", "http://www.thespheres.org/xsd/betula/curriculum-import.xsd");
        }
    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/text/curriculum-file+xml/Lookup")
    public static class CurriculumLookupProvider implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            final XMLDataObject xmldo = base.lookup(XMLDataObject.class);
            return CurriculumAssociationsSupport.create(xmldo);
        }
    }

}
