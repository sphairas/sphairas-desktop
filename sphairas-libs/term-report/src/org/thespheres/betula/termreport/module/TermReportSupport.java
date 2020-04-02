/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.module;

import java.io.IOException;
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
import org.thespheres.betula.services.ui.xml.XMLUtilities;
import org.thespheres.betula.services.ui.xml.XmlBeforeSaveCallback;
import org.thespheres.betula.termreport.xml.XmlTermReport;
import org.thespheres.betula.termreport.model.XmlTermReportImpl;
import org.thespheres.betula.termreport.print.PDFPrintProvider;
import org.thespheres.betula.termreport.xml.XmlNote;
import org.thespheres.betula.ui.swingx.AbstractXmlSupport;
import org.thespheres.betula.ui.util.JAXBUtil;
import org.thespheres.betula.util.StudentAdapter;
import org.thespheres.betula.util.StudentUnmarshallAdapter;
import org.thespheres.betula.util.XmlStudents;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author boris.heithecker
 */
public class TermReportSupport extends AbstractXmlSupport<TermReportTableModel2> {

    private static JAXBContext jabxctx;

    static {
        try {
            Class[] il = JAXBUtil.lookupJAXBTypes("XmlTermReport", XmlTermReport.class, XmlNote.class);
            jabxctx = JAXBContext.newInstance(il);
        } catch (JAXBException ex) {
            Logger.getLogger(TermReportSupport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private TermReportSupport(XMLDataObject xmldo) {
        super(xmldo);
    }

    public static Lookup create(XMLDataObject xmldo) {
        final TermReportSupport cts = new TermReportSupport(xmldo);
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
    protected void saveNode(Lookup ctx, Document doc) throws IOException {
        DataObject dob = ctx.lookup(DataObject.class);
        if (dob == null || !dob.equals(xmlDataObject)) {
            return;
        }
        final XmlTermReportImpl report = (XmlTermReportImpl) model.getItemsModel(); //.getTermReport();
        XMLUtilities.removeElement(doc, "term-report", "http://www.thespheres.org/xsd/betula/term-report.xsd");
        try {
            jabxctx.createMarshaller().marshal(report.getXmlTermReport(), doc.getDocumentElement());
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    @Override
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
            e = findTermReportElement(doc);
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
        XmlTermReport report;
        try {
            Unmarshaller um = jabxctx.createUnmarshaller();
            um.setAdapter(StudentAdapter.class, umAdapter);
            synchronized (doc) {
//                try {
//                    Thread.sleep(1500);
//                } catch (InterruptedException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
                report = (XmlTermReport) um.unmarshal(e);
            }
            final XmlTermReportImpl impl = new XmlTermReportImpl(report, xmlDataObject);
//            getModel().setTermReport(impl, unit);
            getModel().initialize(impl, getDataObject().getLookup());
            ic.add(impl);
            ic.add(getModel());
            ic.add(new TermReportActionsImpl(impl, getDataObject().getLookup()));
            ic.add((XmlBeforeSaveCallback) (lkp, dc) -> saveNode(lkp, dc));
            ic.add(new PDFPrintProvider(this));

//            ic.add(new TermPrintProvider(impl, getDataObject().getLookup()));
//        for (AssessmentProvider p : report.getProviders()) {
//            p.addPropertyChangeListener(l);
//        }
        } catch (JAXBException ex) {
            throw new IOException(ex);
        } finally {
            getDataObject().setModified(false);
        }
    }

    @Override
    public String getContentType() {
        return TermReportDataObject.TERMREPORT_MIME;
    }

    @Override
    public TermReportTableModel2 getModel() {
        if (model == null) {
            model = TermReportTableModel2.create();
        }
        return model;
    }

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

    private static Element findTermReportElement(final Document doc) throws IllegalArgumentException {
        //throws IllegalArgumentException if there is multiple elements of the same name
        synchronized (doc) {
            return XMLUtil.findElement(doc.getDocumentElement(), "term-report", "http://www.thespheres.org/xsd/betula/term-report.xsd");
        }
    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/text/term-report-file+xml/Lookup")
    public static class TermReportSupportLookupProvider implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            try {
                final XMLDataObject xmldo = base.lookup(XMLDataObject.class);
                final Document doc = xmldo.getDocument();
                final Element e = findTermReportElement(doc);
                if (e != null) {
                    try {
                        return TermReportSupport.create(xmldo);
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

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/text/term-report-file+xml/Lookup")
    public static class DatabaseIntegrationLkp implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            final DataObject dob = base.lookup(DataObject.class);
            final Project project = FileOwnerQuery.getOwner(dob.getPrimaryFile());
            if (project != null) {
                final Unit unit = project.getLookup().lookup(Unit.class);
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
