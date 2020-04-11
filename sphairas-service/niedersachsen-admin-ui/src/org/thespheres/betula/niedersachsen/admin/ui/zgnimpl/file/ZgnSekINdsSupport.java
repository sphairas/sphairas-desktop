/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.file;

import java.io.IOException;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.netbeans.spi.project.LookupProvider;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.Unit;
import org.thespheres.betula.niedersachsen.NdsZeugnisFormular;
import org.thespheres.betula.ui.swingx.BaseAbstractXmlSupport;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author boris
 */
public class ZgnSekINdsSupport extends BaseAbstractXmlSupport {

    public static final JAXBContext JAXB;

    static {
        try {
            JAXB = JAXBContext.newInstance(NdsZeugnisFormular.class, NdsZeugnisFormular.ZeugnisMappe.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private ZgnSekINdsSupport(final XMLDataObject xmldo) {
        super(xmldo);
    }

    static Lookup create(final XMLDataObject xmldo) {
        final ZgnSekINdsSupport ret = new ZgnSekINdsSupport(xmldo);
        ret.reload();
        return ret.getLookup();
    }

    @Override
    protected void saveNode(Lookup ctx, Document doc) throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    public void save() throws IOException {
        Object node = getLookup().lookup(NdsZeugnisFormular.class);
        if (node == null) {
            node = getLookup().lookup(NdsZeugnisFormular.ZeugnisMappe.class);
        }
        if (node == null) {
            throw new IOException("No report entry found.");
        }
        try {
            final Marshaller m = JAXB.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            try (final OutputStream os = getDataObject().getPrimaryFile().getOutputStream()) {
                m.marshal(node, os);
            }
        } catch (JAXBException | IOException ex) {
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
        final Object node;
        try {
            final Unmarshaller um = JAXB.createUnmarshaller();
            synchronized (doc) {
                node = um.unmarshal(doc);
            }
            //TODO remove previous.......
//            final XmlCurriculum beforeCurr = getLookup().lookup(XmlCurriculum.class);
            synchronized (this) {
//                if (beforeCurr != null) {
//                    ic.remove(beforeCurr);
//                }
                ic.add(node);
//                if (actions != null) {
//                    actions.setXmlCurriculum(node);
//                } else {
//                    final LocalProperties localProps = findProperties(doc);
//                    if (localProps != null) {
//                        ic.add(localProps);
//                    }
//                    ic.add((XmlBeforeSaveCallback) (lkp, dc) -> saveNode(lkp, dc));
//                }
            }
        } catch (JAXBException ex) {
            throw new IOException(ex);
        } finally {
            getDataObject().setModified(false);
        }
    }

//    protected LocalProperties findProperties(final Document doc) {
//        LocalProperties localProps = null;
//        final Project project = FileOwnerQuery.getOwner(getDataObject().getPrimaryFile());
//        if (project != null) {
//            localProps = project.getLookup().lookup(LocalProperties.class);
//        }
//        if (localProps == null) {
//            final File jf = FileUtil.toFile(getDataObject().getPrimaryFile());
//            if (jf != null) {
//                final String name = ServiceConstants.findProviderName(jf.toPath());
//                if (name != null) {
//                    localProps = LocalProperties.find(name);
//                }
//            }
//        }
//        if (localProps == null) {
//            final String defaultProvider = doc.getDocumentElement().getAttribute("default-provider");
//            if (!StringUtils.isBlank(defaultProvider)) {
//                localProps = LocalProperties.find(defaultProvider);
//            }
//        }
//        return localProps;
//    }
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

//    private static Element findCurriculumElement(final Document doc) throws IllegalArgumentException {
//        //throws IllegalArgumentException if there is multiple elements of the same name
//        synchronized (doc) {
//            return XMLUtil.findElement(doc.getDocumentElement(), "curriculum", "http://www.thespheres.org/xsd/betula/curriculum.xsd");
//        }
//    }
//    @ServiceProvider(service = LookupProvider.class, path = "Loaders/" + CurriculumDataObject.CURRICULUM_FILE_MIME + "/Lookup")
//    public static class CurriculumFileInfoLookupProvider implements LookupProvider {
//
//        @Override
//        public Lookup createAdditionalLookup(Lookup base) {
//            final XMLDataObject xmldo = base.lookup(XMLDataObject.class);
//            return FileInfoSupport.create(xmldo);
//        }
//    }
//
    @ServiceProvider(service = LookupProvider.class, path = "Loaders/" + NdsZeugnisFormular.FORMULAR_MIME + "/Lookup")
    public static class ZeugnisLookupProvider implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            final XMLDataObject xmldo = base.lookup(XMLDataObject.class);
            return ZgnSekINdsSupport.create(xmldo);
        }
    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/" + NdsZeugnisFormular.ZeugnisMappe.MAPPE_MIME + "/Lookup")
    public static class MappeLookupProvider implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            final XMLDataObject xmldo = base.lookup(XMLDataObject.class);
            return ZgnSekINdsSupport.create(xmldo);
        }
    }
}
