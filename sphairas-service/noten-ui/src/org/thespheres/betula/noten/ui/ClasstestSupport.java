/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.ui;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.netbeans.spi.project.LookupProvider;
import org.openide.loaders.DataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.AssessmentContext;
import org.thespheres.betula.classtest.model.ClassroomTestEditor2;
import org.thespheres.betula.noten.impl.MarginModel;
import org.thespheres.betula.noten.impl.NotenAssessment;
import org.thespheres.betula.noten.impl.NotenAssessmentXmlAdapter;
import org.thespheres.betula.noten.impl.NotenOSAssessment;
import org.thespheres.betula.noten.impl.NotenOSAssessmentContextXmlAdapter;
import org.thespheres.betula.services.ui.xml.XMLUtilities;
import org.thespheres.betula.services.ui.xml.XmlBeforeSaveCallback;
import org.thespheres.betula.services.util.NbUtilities;
import org.thespheres.betula.util.Int2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author boris.heithecker
 */
public class ClasstestSupport implements XmlBeforeSaveCallback {

    private static final long serialVersionUID = 1L;
    private static JAXBContext jabxctx;
    private final XMLDataObject xmlDataObject;
    private final RequestProcessor RP = new RequestProcessor(ClasstestSupport.class);
    private final InstanceContent ic = new InstanceContent();
    private final AbstractLookup lookup;

    static {
        try {
            jabxctx = JAXBContext.newInstance(NotenAssessmentXmlAdapter.class, NotenOSAssessmentContextXmlAdapter.class);
        } catch (JAXBException ex) {
            Logger.getLogger(ClasstestSupport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("LeakingThisInConstructor")
    private ClasstestSupport(XMLDataObject xmldo) {
        this.xmlDataObject = xmldo;
        ic.add(this);
        lookup = new AbstractLookup(ic);
    }

    public static Lookup create(XMLDataObject xmldo) {
        final ClasstestSupport cts = new ClasstestSupport(xmldo);
        cts.RP.post(() -> {
            try {
                cts.load();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        });
        return cts.lookup;
    }

    @Override
    public int position() {
        return 2000;
    }

    @Override
    public void run(Lookup ctx, Document doc) throws IOException {
        DataObject dob = ctx.lookup(DataObject.class);
        if (dob == null || !dob.equals(xmlDataObject)) {
            return;
        }
        final ClassroomTestEditor2 editor;
        if ((editor = ctx.lookup(ClassroomTestEditor2.class)) != null) {
            AssessmentContext assess = editor.getAssessmentContext();
            if (assess != null) {
                if (assess instanceof NotenAssessment) {
                    writeNoten((NotenAssessment) assess, doc);
                } else if (assess instanceof NotenOSAssessment) {
                    writeNotenOS((NotenOSAssessment) assess, doc);
                }
            }
        }
    }

    private void writeNoten(NotenAssessment assess, Document doc) throws IOException {
        removeElements(doc);
        NotenAssessmentXmlAdapter adapter = assess.createAdapter();
        try {
            jabxctx.createMarshaller().marshal(adapter, doc.getDocumentElement());
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    private void writeNotenOS(NotenOSAssessment assess, Document doc) throws IOException {
        removeElements(doc);
        NotenOSAssessmentContextXmlAdapter adapter = assess.createAdapter();
        try {
            jabxctx.createMarshaller().marshal(adapter, doc.getDocumentElement());
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    private void removeElements(Document doc) throws IOException {
        XMLUtilities.removeElement(doc, "NotenAssessmentContext", null);
        XMLUtilities.removeElement(doc, "NotenOSAssessmentContext", null);
    }

    protected void load() throws IOException {
        Document doc;
        try {
            doc = getXmlDataObject().getDocument();
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
        try {
            Unmarshaller um = jabxctx.createUnmarshaller();
            AssessmentContext<?, Int2> noten = readNoten(doc, um);
            AssessmentContext<?, Int2> notenos = readNotenOS(doc, um);
            final AssessmentContext attach;
            if (noten != null && notenos == null) {
                attach = noten;
            } else if (noten == null && notenos != null) {
                attach = notenos;
            } else {
                attach = null;
            }
            if (attach != null) {
                attachContext(attach);
            }
        } catch (JAXBException ex) {
            Logger.getLogger(ClasstestSupport.class.getCanonicalName()).log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
    }

    public void attachContext(final AssessmentContext attach) {
        ic.add(attach);
        NbUtilities.waitAndThen(getXmlDataObject().getLookup(), ClassroomTestEditor2.class, editor -> {
//                    editor.attachContext(attach);
            SyncContext.attach(attach, editor);
//attachContext sets DOB modified to true
            getXmlDataObject().setModified(false);
        });
    }

    public void switchAssessmentContext(AssessmentContext attach) {
        final XMLDataObject dob = getXmlDataObject();
        final ClassroomTestEditor2 editor = dob.getLookup().lookup(ClassroomTestEditor2.class);
        if (editor != null) {
            AssessmentContext old = lookup.lookup(AssessmentContext.class);
            if (old != null) {
                ic.remove(old);
            }
            ic.add(attach);
//            editor.attachContext(attach);
            SyncContext.attach(attach, editor);
            dob.setModified(true);
        }
    }

    private AssessmentContext<StudentId, Int2> readNoten(Document doc, Unmarshaller um) throws IOException, JAXBException {
        NotenAssessmentXmlAdapter adapter;
        Element noten;
        synchronized (doc) {
            noten = XMLUtil.findElement(doc.getDocumentElement(), "NotenAssessmentContext", null);
        }
        if (noten == null) {
            return null;
        }
        synchronized (doc) {
            adapter = (NotenAssessmentXmlAdapter) um.unmarshal(noten);
        }
        MarginModel.Model m = MarginModel.Model.valueOf(adapter.getMarginModel());
        Int2 marginValue = adapter.getMarginValue();
        Int2[] floorValues = adapter.getFloorValues();
        Int2 max = adapter.getRangeMaximum();
        String defDist = adapter.getDefaultDistribtution();
        return new NotenAssessment(max, floorValues, m, marginValue, defDist);
    }

    private AssessmentContext<StudentId, Int2> readNotenOS(final Document doc, Unmarshaller um) throws IOException, JAXBException {
        NotenOSAssessmentContextXmlAdapter adapter;
        Element notenos;
        synchronized (doc) {
            notenos = XMLUtil.findElement(doc.getDocumentElement(), "NotenOSAssessmentContext", null);
        }
        if (notenos == null) {
            return null;
        }
        synchronized (doc) {
            adapter = (NotenOSAssessmentContextXmlAdapter) um.unmarshal(notenos);
        }
        Int2[] floorValues = adapter.getFloorValues();
        Int2 max = adapter.getRangeMaximum();
        String defDist = adapter.getDefaultDistribtution();
        return new NotenOSAssessment(max, floorValues, defDist);
    }

    private XMLDataObject getXmlDataObject() {
        return xmlDataObject;
    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/text/betula-classtest-file+xml/Lookup")
    public static class ClasstestTableLookupProvider implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            XMLDataObject xmldo = base.lookup(XMLDataObject.class);
            return ClasstestSupport.create(xmldo);
        }
    }
}
