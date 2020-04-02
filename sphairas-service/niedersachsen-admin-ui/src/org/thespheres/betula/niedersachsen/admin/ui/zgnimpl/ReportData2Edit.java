/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl;

import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.xml.bind.JAXBException;
import javax.xml.transform.dom.DOMResult;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.ExceptionMessage;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.XmlDocumentEntry;
import org.thespheres.betula.niedersachsen.xml.NdsZeugnisAngaben;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.ServiceException;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.ContainerBuilder;
import org.w3c.dom.Document;

/**
 *
 * @author boris.heithecker
 */
public class ReportData2Edit<V> extends AbstractUndoableEdit {

    private final NdsZeugnisAngaben update;
    private final NdsZeugnisAngaben undo;
    private final ReportData2 report;
    private final Consumer< V> setter;
    private final V beforeValue;
    private final V newValue;
    private Exception exception;
    private final WebServiceProvider service;
    private final RequestProcessor rp;

    public ReportData2Edit(NdsZeugnisAngaben update, V newValue, NdsZeugnisAngaben undo, V oldValue, ReportData2 report, Consumer<V> setter) throws IOException {
        this.update = update;
        this.undo = undo;
        this.report = report;
        this.beforeValue = oldValue;
        this.newValue = newValue;
        this.setter = setter;
        this.service = ((RemoteReportsModel2Impl) report.getHistory()).getService();
        rp = ((RemoteReportsModel2Impl) report.getHistory()).executor();
    }

    public void post() {
        doImpl(service);
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        doImpl(service);
    }

    public void doImpl(final WebServiceProvider service) {
        setter.accept(newValue);
        rp.post(() -> run(false));
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        setter.accept(beforeValue);
        rp.post(() -> run(true));
    }

    @Override
    public boolean canUndo() {
        return super.canUndo() && exception == null;
    }

    @Override
    public boolean canRedo() {
        return super.canRedo() && exception == null;
    }

    private void silentUndo(Exception ex) {
        this.exception = ex;
        setter.accept(newValue);
    }

    public void run(boolean u) {
        final ContainerBuilder builder = new ContainerBuilder();
        final DocumentId doc = report.getDocumentId();
        final XmlDocumentEntry re = new XmlDocumentEntry(doc, null, true);
        final DOMResult result = new DOMResult();
        final NdsZeugnisAngaben angaben = u ? undo : update;
        try {
            RemoteReportsModel2Impl.getZeungnisAngabenJAXB().createMarshaller().marshal(angaben, result);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
        final Document d = (Document) result.getNode();
        re.setReportDataElement(d.getDocumentElement());
        builder.add(re, Paths.REPORTS_PATH);
        //
        Container ret;
        try {
            ret = service.createServicePort().solicit(builder.getContainer());
        } catch (ServiceException | IOException ex) {
            silentUndo(ex);
            RemoteReportsModel2Impl.notifyError(ex, ex.getLocalizedMessage());
            return;
        }
        final List<Envelope> l = DocumentUtilities.findEnvelope(ret, Paths.REPORTS_PATH).stream()
                .peek(e -> {
                    final ExceptionMessage em;
                    if ((em = e.getException()) != null) {
                        RemoteReportsModel2Impl.notifyError(em);
                    }
                })
                .filter(e -> e.getException() == null)
                .collect(Collectors.toList());
        final XmlDocumentEntry xml = findXmlDocument(l);
        if (xml != null) {
            org.w3c.dom.Element el = xml.getReportDataElement();
            if (el != null) {
                NdsZeugnisAngaben newData;
                try {
                    newData = (NdsZeugnisAngaben) RemoteReportsModel2Impl.getZeungnisAngabenJAXB().createUnmarshaller().unmarshal(el);
                } catch (JAXBException | ClassCastException ex) {
                    throw new IllegalStateException(ex);
                }
                if (newData != null) {
                    report.initializeData(newData);
                }
            }
        }

    }

    private XmlDocumentEntry findXmlDocument(final List<Envelope> l) {
        return l.stream()
                .filter(XmlDocumentEntry.class::isInstance)
                .map(XmlDocumentEntry.class::cast)
                .filter(e -> e.getIdentity().equals(report.getDocumentId()))
                .collect(CollectionUtil.singleOrNull());
    }
}
