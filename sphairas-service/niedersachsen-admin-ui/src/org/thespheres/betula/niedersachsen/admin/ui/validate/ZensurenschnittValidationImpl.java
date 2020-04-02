/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.validate;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.niedersachsen.admin.ui.RemoteReportsModel2;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.ui.util.JAXBUtil;
import org.thespheres.betula.validation.impl.CareerAwareGradeToDoubleConverter;
import org.thespheres.betula.validation.impl.ZensurenschnittValidation;
import org.thespheres.betula.validation.impl.ZensurenschnittValidationConfiguration;

/**
 *
 * @author boris.heithecker
 */
public class ZensurenschnittValidationImpl extends ZensurenschnittValidation<ReportData2, RemoteReportsModel2, ZensurenschnittResultImpl> implements RunReport {

    private final ReportsValidationDelegate delegate;

    private ZensurenschnittValidationImpl(RemoteReportsModel2 model, ZensurenschnittValidationConfiguration config) throws IOException {
        super(model, config);
        this.delegate = new ReportsValidationDelegate(this, model.support.getNodeDelegate());
    }

    public static ZensurenschnittValidationImpl create(RemoteReportsModel2 model) {
        final FileObject configFo = ValidationConfigUtilities.findLastConfigFile("/ValidationEngine/Configuration/org-thespheres-betula-validation-impl-ZensurenschnittValidation/");
        if (configFo != null) {
            final Class[] cl = JAXBUtil.lookupJAXBTypes("ZensurenschnittValidationConfiguration", ZensurenschnittValidationConfiguration.class, CareerAwareGradeToDoubleConverter.class);
            try {
                final JAXBContext ctx = JAXBContext.newInstance(cl);
                final ZensurenschnittValidationConfiguration unmarshal = (ZensurenschnittValidationConfiguration) ctx.createUnmarshaller().unmarshal(configFo.getInputStream());
                return new ZensurenschnittValidationImpl(model, unmarshal);
            } catch (JAXBException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    @Override
    protected boolean cancel(ValidationListener<ZensurenschnittResultImpl> cancelledBy) {
        return delegate.task.cancel();
    }

    @Override
    protected ZensurenschnittResultImpl createResult(ReportData2 report, ZensurenschnittValidationConfiguration config) {
        return new ZensurenschnittResultImpl(report.getRemoteStudent(), report, config);
    }

    @Override
    public void runOneDocument(TermId term, StudentId stud) {
        fireStart(1);
        try {
            model.getReportDocuments(stud).stream()
                    .map(model::getReportDocument)
                    .filter(r -> r.getTerm().equals(term))
                    .forEach(this::processOneDocument);
        } finally {
            fireStop();
        }
    }

    @Override
    public void runOneTerm(final TermId term) {
        model.getReportDocuments(term);
        final List<ReportData2> set = model.getReportDocuments(term).stream()
                .map(model::getReportDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        try {
            fireStart(set.size());
            set.stream()
                    .forEach(this::processOneDocument);
        } finally {
            fireStop();
        }
    }
}
