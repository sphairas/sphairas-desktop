/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import java.awt.EventQueue;
import org.thespheres.betula.ui.swingx.AbstractXmlSupport;
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
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.thespheres.betula.Unit;
import org.thespheres.betula.journal.Journal;
import org.thespheres.betula.journal.JournalRecord;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.module.JournalDataObject;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.journal.module.EditableJournalImpl;
import org.thespheres.betula.journal.pdf.PDFPrintProvider;
import org.thespheres.betula.journal.xml.XmlJournal;
import org.thespheres.betula.journal.xml.XmlJournalRecord;
import org.thespheres.betula.journal.xml.XmlRecordNote;
import org.thespheres.betula.services.ui.xml.XMLUtilities;
import org.thespheres.betula.services.ui.xml.XmlBeforeSaveCallback;
import org.thespheres.betula.ui.util.JAXBUtil;
import org.thespheres.betula.util.StudentAdapter;
import org.thespheres.betula.util.XmlStudents;
import org.thespheres.betula.util.StudentUnmarshallAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author boris.heithecker
 */
public class JournalTableSupport extends AbstractXmlSupport<JournalTableModel> {

    private static JAXBContext jabxctx;

    static {
        try {
            final Class[] tl = JAXBUtil.lookupJAXBTypes("JournalTableSupport", XmlJournal.class, XmlRecordNote.class);
            jabxctx = JAXBContext.newInstance(tl);
        } catch (JAXBException ex) {
            Logger.getLogger(JournalTableSupport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private JournalTableSupport(XMLDataObject xmldo) {
        super(xmldo);
    }

    public static Lookup create(XMLDataObject xmldo) {
        final JournalTableSupport cts = new JournalTableSupport(xmldo);
        cts.RP.post(() -> {
            try {
                cts.load();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        });
        return cts.getLookup();
    }

    @Override
    protected void saveNode(Lookup ctx, final Document doc) throws IOException {
        final DataObject dob = ctx.lookup(DataObject.class);
        if (dob == null || !dob.equals(xmlDataObject)) {
            return;
        }
        final EditableJournal journal = dob.getLookup().lookup(EditableJournal.class);
        if (journal == null) {
            throw new IOException("No EditableJournal found.");
        }
        final Journal xrset = journal.getRecordSet();
        XMLUtilities.removeElement(doc, "journal", "http://www.thespheres.org/xsd/betula/journal.xsd");
        try {
            jabxctx.createMarshaller().marshal(xrset, doc.getDocumentElement());
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected void load() throws IOException {
        setTime(getDataObject().getPrimaryFile().lastModified().getTime());
        Document d;
        try {
            d = getDataObject().getDocument();
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
        final Document doc = d;
        Element e = null;
        try {
            e = findRecordSetElement(doc);
        } catch (IllegalArgumentException ex) {
            throw new IOException(ex);
        }
        if (e == null) {
            throw new IOException("No element found.");
        }
        boolean template = xmlDataObject.isTemplate();
        final Unit unit = xmlDataObject.getLookup().lookup(Unit.class);
        if (unit == null && !template) {//Template don't have units in their project lookup
            throw new IOException();
        }
        XmlStudents xs = null;
        StudentUnmarshallAdapter umAdapter = new StudentUnmarshallAdapter(unit, xs);
        XmlJournal xrset;
        try {
            Unmarshaller um = jabxctx.createUnmarshaller();
            um.setAdapter(StudentAdapter.class, umAdapter);
            synchronized (doc) {
//                try {
//                    Thread.sleep(1500);
//                } catch (InterruptedException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
                xrset = (XmlJournal) um.unmarshal(e);
            }
            final EditableJournalImpl ecal = new EditableJournalImpl(xrset, xmlDataObject.getLookup()) {

                @Override
                protected JournalRecord createJournalRecord() {
                    return new XmlJournalRecord();
                }

            }; //TODO: verbessern
            final JournalEditor editor = new JournalEditor(ecal, xmlDataObject.getLookup(), this);
            final EditableJournal beforeEJ = getLookup().lookup(EditableJournal.class);
            ic.add(ecal);
            if (beforeEJ != null) {
                ic.remove(beforeEJ);
            }
            final JournalEditor beforeEditor = getLookup().lookup(JournalEditor.class);
            ic.add(editor);
            if (beforeEditor != null) {
                ic.remove(beforeEditor);
            }
            EventQueue.invokeLater(() -> getModel().initialize(ecal, xmlDataObject.getLookup()));
            ic.add((XmlBeforeSaveCallback) (lkp, dc) -> saveNode(lkp, dc));
//            ic.add(new CalendarRecordsPrintProvider(editor));
            ic.add(new PDFPrintProvider(this));
        } catch (JAXBException ex) {
            throw new IOException(ex);
        } finally {
            getDataObject().setModified(false);
        }
    }

    @Override
    public String getContentType() {
        return JournalDataObject.JOURNAL_MIME;
    }

    @Override
    public synchronized JournalTableModel getModel() {
        if (model == null) {
            model = JournalTableModel.create();
        }
        return model;
    }

    private static Element findRecordSetElement(final Document doc) throws IllegalArgumentException {
        //throws IllegalArgumentException if there is multiple elements of the same name
        synchronized (doc) {
            return XMLUtil.findElement(doc.getDocumentElement(), "journal", "http://www.thespheres.org/xsd/betula/journal.xsd");
        }
    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/text/betula-journal-file+xml/Lookup")
    public static class JournalTableLookupProvider implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            try {
                XMLDataObject xmldo = base.lookup(XMLDataObject.class);
                Document doc = xmldo.getDocument();
                Element e = findRecordSetElement(doc);
                if (e != null) {
                    try {
                        return JournalTableSupport.create(xmldo);
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

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/text/betula-journal-file+xml/Lookup")
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
