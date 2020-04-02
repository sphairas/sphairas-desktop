/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.impl;

import org.thespheres.betula.services.ui.util.FileInfoSupport;
import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupProvider;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.thespheres.betula.Unit;
import org.thespheres.betula.curriculum.xml.XmlCurriculum;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ServiceConstants;
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
public class CurriculumSupport extends BaseAbstractXmlSupport {

    public static final JAXBContext JAXB;
    public static final Class[] JAXB_TYPES;

    static {
        try {
            JAXB_TYPES = JAXBUtil.lookupJAXBTypes("CurriculumSupport", XmlCurriculum.class);
            JAXB = JAXBContext.newInstance(JAXB_TYPES);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private CurriculumSupport(final XMLDataObject xmldo) {
        super(xmldo);
    }

    static Lookup create(final XMLDataObject xmldo) {
        final CurriculumSupport cts = new CurriculumSupport(xmldo);
        cts.reload();
        return cts.getLookup();
    }

    @Override
    protected void saveNode(Lookup ctx, Document doc) throws IOException {
        DataObject dob = ctx.lookup(DataObject.class);
        if (dob == null || !dob.equals(xmlDataObject)) {
            return;
        }
        final XmlCurriculum report = getLookup().lookup(XmlCurriculum.class);  //(XmlCurriculum) model.getItemsModel();
        XMLUtilities.removeElement(doc, "curriculum", "http://www.thespheres.org/xsd/betula/curriculum.xsd");
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
            e = findCurriculumElement(doc);
        } catch (IllegalArgumentException ex) {
            throw new IOException(ex);
        }
        if (e == null) {
            throw new IOException("No element found.");
        }
        final XmlCurriculum node;
        try {
            final Unmarshaller um = JAXB.createUnmarshaller();
            synchronized (doc) {
                node = (XmlCurriculum) um.unmarshal(e);
            }
            //TODO remove previous.......
            final XmlCurriculum beforeCurr = getLookup().lookup(XmlCurriculum.class);
            final CurriculumTableActionsImpl actions = getLookup().lookup(CurriculumTableActionsImpl.class);
            synchronized (this) {
                if (beforeCurr != null) {
                    ic.remove(beforeCurr);
                }
                ic.add(node);
                if (actions != null) {
                    actions.setXmlCurriculum(node);
                } else {
                    final LocalProperties localProps = findProperties(doc);
                    if (localProps != null) {
                        ic.add(localProps);
                    }
                    ic.add(new CurriculumTableActionsImpl(node, xmlDataObject.getLookup()));
                    ic.add((XmlBeforeSaveCallback) (lkp, dc) -> saveNode(lkp, dc));
                    //            ic.add(new PDFPrintProvider(this));
                }
            }
//            EventQueue.invokeLater(() -> getModel().initialize(node, xmlDataObject.getLookup()));
        } catch (JAXBException ex) {
            throw new IOException(ex);
        } finally {
            getDataObject().setModified(false);
        }
    }

    protected LocalProperties findProperties(final Document doc) {
        LocalProperties localProps = null;
        final Project project = FileOwnerQuery.getOwner(getDataObject().getPrimaryFile());
        if (project != null) {
            localProps = project.getLookup().lookup(LocalProperties.class);
        }
        if (localProps == null) {
            final File jf = FileUtil.toFile(getDataObject().getPrimaryFile());
            if (jf != null) {
                final String name = ServiceConstants.findProviderName(jf.toPath());
                if (name != null) {
                    localProps = LocalProperties.find(name);
                }
            }
        }
        if (localProps == null) {
            final String defaultProvider = doc.getDocumentElement().getAttribute("default-provider");
            if (!StringUtils.isBlank(defaultProvider)) {
                localProps = LocalProperties.find(defaultProvider);
            }
        }
        return localProps;
    }
//
//    @Override
//    public String getContentType() {
//        return CurriculumDataObject.CURRICULUM_FILE_MIME;
//    }
//
//    @Override
//    public CurriculumTableModel getModel() {
//        if (model == null) {
//            model = CurriculumTableModel.create();
//        }
//        return model;
//    }

    private Unit getUnit() {
        return getDataObject().getLookup().lookup(Unit.class);
    }

    public String getDisplayName() {
        String ret = getDataObject().getNodeDelegate().getDisplayName();
        if (getUnit() != null) {
            ret = getUnit().getDisplayName() + ": " + ret;
        }
        return ret;
    }

    private static Element findCurriculumElement(final Document doc) throws IllegalArgumentException {
        //throws IllegalArgumentException if there is multiple elements of the same name
        synchronized (doc) {
            return XMLUtil.findElement(doc.getDocumentElement(), "curriculum", "http://www.thespheres.org/xsd/betula/curriculum.xsd");
        }
    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/" + CurriculumDataObject.CURRICULUM_FILE_MIME + "/Lookup")
    public static class CurriculumFileInfoLookupProvider implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            final XMLDataObject xmldo = base.lookup(XMLDataObject.class);
            return FileInfoSupport.create(xmldo);
        }
    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/" + CurriculumDataObject.CURRICULUM_FILE_MIME + "/Lookup")
    public static class CurriculumLookupProvider implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            final XMLDataObject xmldo = base.lookup(XMLDataObject.class);
            return CurriculumSupport.create(xmldo);
        }
    }

}
