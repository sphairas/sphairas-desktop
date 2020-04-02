/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.target;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupProvider;
import org.openide.loaders.DataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.util.NbUtilities;
import org.thespheres.betula.services.ui.xml.XMLUtilities;
import org.thespheres.betula.services.ui.xml.XmlBeforeSaveCallback;
import org.thespheres.betula.util.XmlTargetAssessment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author boris.heithecker
 */
public class TargetAssessmentSupport implements Serializable, Lookup.Provider {

    private static final long serialVersionUID = 1L;
    private static JAXBContext jabxctx;
    private final XMLDataObject xmlDataObject;
    private final RequestProcessor RP = new RequestProcessor(TargetAssessmentSupport.class);
    private final InstanceContent ic = new InstanceContent(RP);
    private AbstractLookup lookup;
    private final TargetAssessmentTableModel model;

    static {
        try {
            jabxctx = JAXBContext.newInstance(XmlTargetAssessment.class);
        } catch (JAXBException ex) {
            Logger.getLogger(TargetAssessmentSupport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private TargetAssessmentSupport(XMLDataObject xmldo) {
        this.model = TargetAssessmentTableModel.create();
        this.xmlDataObject = xmldo;
        RP.post(this::load);
    }

    @Override
    public synchronized Lookup getLookup() {
        if (lookup == null) {
            lookup = new AbstractLookup(ic);
            ic.add(this);
            ic.add(model);
        }
        return lookup;
    }

    TargetAssessmentTableModel getModel() {
        return model;
    }

    private void load() {
        try {
            doLoad();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    protected void saveNode(Lookup ctx, final Document doc) throws IOException {
        DataObject dob = ctx.lookup(DataObject.class);
        if (dob == null || !dob.equals(xmlDataObject)) {
            return;
        }
        final XmlTargetAssessment xrset = xmlDataObject.getLookup().lookup(XmlTargetAssessment.class);
        if (xrset != null) {
            XMLUtilities.removeElement(doc, "target-assessment", "http://www.thespheres.org/xsd/betula/target-assessment.xsd");
            try {
                jabxctx.createMarshaller().marshal(xrset, doc.getDocumentElement());
            } catch (JAXBException ex) {
                throw new IOException(ex);
            }
        }
    }

    protected void doLoad() throws IOException {
        Document d;
        try {
            d = xmlDataObject.getDocument();
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
        final Document doc = d;
        Element e = null;
        try {
            e = findTargetAssessmentElement(doc);
        } catch (IllegalArgumentException ex) {
            throw new IOException(ex);
        }
        XmlTargetAssessment xmlta;
        if (e == null) {
            xmlta = new XmlTargetAssessment();
            final Project prj = FileOwnerQuery.getOwner(xmlDataObject.getPrimaryFile());
            if (prj != null) {
                final LocalFileProperties lfp = prj.getLookup().lookup(LocalFileProperties.class);
                if (lfp != null) {
                    String pc = lfp.getProperty("preferredConvention");
                    if (pc != null) {
                        xmlta.setPreferredConvention(pc);
                    }
                }
            }
        } else {
            try {
                Unmarshaller um = jabxctx.createUnmarshaller();
                synchronized (doc) {
                    xmlta = (XmlTargetAssessment) um.unmarshal(e);
                }
            } catch (JAXBException ex) {
                throw new IOException(ex);
            }
        }
        NbUtilities.waitAndThen(xmlDataObject.getLookup(), JournalEditor.class, c -> SyncTarget.start(xmlta, c));
//        SyncTarget.start(xmlta, ecal);
        ic.add(xmlta);
        ic.add((XmlBeforeSaveCallback) (lkp, dc) -> saveNode(lkp, dc));
    }

    private static Element findTargetAssessmentElement(final Document doc) throws IllegalArgumentException {
        //throws IllegalArgumentException if there is multiple elements of the same name
        synchronized (doc) {
            return XMLUtil.findElement(doc.getDocumentElement(), "target-assessment", "http://www.thespheres.org/xsd/betula/target-assessment.xsd");
        }
    }

    public Object writeReplace() throws ObjectStreamException {
        return new ResolvableHelper(xmlDataObject);
    }

    public static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;
        protected final DataObject serializableDOb;

        protected ResolvableHelper(DataObject dobj) {
            this.serializableDOb = dobj;
        }

        public Object readResolve() throws ObjectStreamException {
            return serializableDOb.getLookup().lookup(TargetAssessmentSupport.class);
        }
    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/text/betula-journal-file+xml/Lookup")
    public static class JournalTableLookupProvider implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            XMLDataObject xmldo = base.lookup(XMLDataObject.class);
            return new TargetAssessmentSupport(xmldo).getLookup();
        }
    }
}
