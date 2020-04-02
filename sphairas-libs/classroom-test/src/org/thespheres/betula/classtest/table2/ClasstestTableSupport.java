/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.table2;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.netbeans.spi.project.LookupProvider;
import org.openide.loaders.DataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.thespheres.betula.Unit;
import org.thespheres.betula.classtest.Assessable;
import org.thespheres.betula.classtest.StudentScores;
import org.thespheres.betula.classtest.model.ClassroomTestEditor2;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.module2.ClasstestDataObject;
import org.thespheres.betula.classtest.printing.CSVExportOption;
import org.thespheres.betula.classtest.printing.PDFPrintProvider;
import org.thespheres.betula.classtest.xml.StudentScoresImpl;
import org.thespheres.betula.util.StudentAdapter;
import org.thespheres.betula.classtest.xml.XmlClasstest;
import org.thespheres.betula.classtest.xml.XmlProblem;
import org.thespheres.betula.services.ui.xml.XMLUtilities;
import org.thespheres.betula.services.ui.xml.XmlBeforeSaveCallback;
import org.thespheres.betula.util.XmlStudents;
import org.thespheres.betula.util.StudentUnmarshallAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author boris.heithecker
 */
public class ClasstestTableSupport implements Serializable, NavigatorLookupHint, Lookup.Provider {

    private static final long serialVersionUID = 1L;
    private static JAXBContext jabxctx;
    private final XMLDataObject xmlDataObject;
    private final RequestProcessor RP = new RequestProcessor(ClasstestTableSupport.class);
    private final InstanceContent ic = new InstanceContent(RP);
    private AbstractLookup lookup;
    private ClasstestTableModel2 model;

    static {
        try {
            jabxctx = JAXBContext.newInstance(XmlClasstest.class);
        } catch (JAXBException ex) {
            Logger.getLogger(ClasstestTableSupport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private ClasstestTableSupport(XMLDataObject xmldo) {
        this.xmlDataObject = xmldo;
    }

    public static Lookup create(XMLDataObject xmldo) {
        final ClasstestTableSupport cts = new ClasstestTableSupport(xmldo);
        cts.RP.post(() -> {
            try {
                cts.load();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        });
        return cts.getLookup();
    }

    protected void saveNode(Lookup ctx, final Document doc) throws IOException {
        DataObject dob = ctx.lookup(DataObject.class);
        if (dob == null || !dob.equals(xmlDataObject)) {
            return;
        }
        final XmlClasstest test = (XmlClasstest) model.getItemsModel().getTest();
        XMLUtilities.removeElement(doc, "classtest", "http://www.thespheres.org/xsd/betula/classtest.xsd");
        try {
            jabxctx.createMarshaller().marshal(test, doc.getDocumentElement());
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    protected void load() throws IOException {
        Document d;
        try {
            d = getDataObject().getDocument();
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
        final Document doc = d;
        Element e = null;
        try {
            e = findClasstestElement(doc);
        } catch (IllegalArgumentException ex) {
            throw new IOException(ex);
        }
        if (e == null) {
            throw new IOException("no element found.");
        }
        boolean template = xmlDataObject.isTemplate();
        final Unit unit = xmlDataObject.getLookup().lookup(Unit.class);
        if (unit == null && !template) {//Template don't have units in their project lookup
            throw new IOException();
        }
        XmlStudents xs = null;
        StudentUnmarshallAdapter umAdapter = new StudentUnmarshallAdapter(unit, xs);
        XmlClasstest xtest;
        try {
            Unmarshaller um = jabxctx.createUnmarshaller();
            um.setAdapter(StudentAdapter.class, umAdapter);
            synchronized (doc) {
                //If not wait ---- lock contention, deadlock.... 
                xtest = (XmlClasstest) um.unmarshal(e);
            }
            final EditableClassroomTest etest = new EditableClassroomTest(xtest) {
                @Override
                protected Assessable.Problem createAssessableProblem(String id, int index) {
                    return new XmlProblem(id, index);
                }

                @Override
                protected StudentScores createStudentScores() {
                    return new StudentScoresImpl();
                }
            };
            ic.add(etest);
            ClassroomTestEditor2 editor = new ClassroomTestEditor2(etest, xmlDataObject.getLookup(), this);
            ic.add(editor);
//            getModel().setEditableTest(etest, editor);
            getModel().initialize(etest, xmlDataObject.getLookup());
            ic.add((XmlBeforeSaveCallback) (lkp, dc) -> saveNode(lkp, dc));
            ic.add(new PDFPrintProvider(this));
            ic.add(new CSVExportOption(this));
        } catch (JAXBException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            getDataObject().setModified(false);
        }
    }

    @Override
    public String getContentType() {
        return ClasstestDataObject.CLASSTEST_MIME;
    }

    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = new AbstractLookup(ic);
            ic.add(this);
        }
        return lookup;
    }

    public ClasstestTableModel2 getModel() {
        if (model == null) {
            model = ClasstestTableModel2.create();
        }
        return model;
    }

    public XMLDataObject getDataObject() {
        return this.xmlDataObject;
    }

    private static Element findClasstestElement(final Document doc) throws IllegalArgumentException {
        //throws IllegalArgumentException if there is multiple elements of the same name
        synchronized (doc) {
            return XMLUtil.findElement(doc.getDocumentElement(), "classtest", "http://www.thespheres.org/xsd/betula/classtest.xsd");
        }
    }

    public Object writeReplace() throws ObjectStreamException {
        return new ResolvableHelper(getDataObject());
    }

    public static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;
        protected final DataObject serializableDOb;

        protected ResolvableHelper(DataObject dobj) {
            this.serializableDOb = dobj;
        }

        public Object readResolve() throws ObjectStreamException {
            return serializableDOb.getLookup().lookup(ClasstestTableSupport.class);
        }
    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/text/betula-classtest-file+xml/Lookup")
    public static class ClasstestTableLookupProvider implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            try {
                final XMLDataObject xmldo = base.lookup(XMLDataObject.class);
                final Document doc = xmldo.getDocument();
                Element e = findClasstestElement(doc);
                if (e != null) {
                    try {
                        return ClasstestTableSupport.create(xmldo);
                    } catch (IllegalArgumentException ex) {
                    }
                }
                return Lookup.EMPTY;
            } catch (IOException | SAXException ex) {
                Logger.getLogger(getClass().getCanonicalName()).log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return Lookup.EMPTY;
        }
    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/text/betula-classtest-file+xml/Lookup")
    public static class UnitIntegrationLookup implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            DataObject dob = base.lookup(DataObject.class);
            Project project = FileOwnerQuery.getOwner(dob.getPrimaryFile());
            if (project != null) {
                Unit unit = project.getLookup().lookup(Unit.class);
                if (unit != null) {
                    return Lookups.fixed(project, unit);
                } else {
                    return Lookups.fixed(project);
                }
            }
            return Lookup.EMPTY;
        }
    }
}
