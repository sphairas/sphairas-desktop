package org.thespheres.betula.validation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.Student;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.History;
import org.thespheres.betula.document.model.Subject;
import org.thespheres.betula.document.util.AbstractReportDocument;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.validation.ValidationResultSet;
import org.thespheres.betula.validation.impl.ZensurenschnittValidationConfiguration.Property;

/**
 *
 * @author boris.heithecker
 * @param <Report>
 * @param <H>
 * @param <R>
 */
//Einzelne, z.B. GY 5-9, GY 10 etc
@Messages({"ZensurenschnittValidation.emptyName=Zensurenschnitt",
    "ZensurenschnittValidation.name=Zensurenschnitt in {0}",
    "ZensurenschnittValidation.runSet.multipleExceptions=Bei Durchlaufen der Zensurenschnitt-Valiedierung sind {0} Fehler aufgetreten."})
public abstract class ZensurenschnittValidation<Report extends AbstractReportDocument, H extends History<?, Report>, R extends ZensurenschnittResult> extends AbstractValidationSet<H, R, DocumentId, ZensurenschnittValidationConfiguration> implements ValidationResultSet<H, R> {

    protected ZensurenschnittValidation(H model, ZensurenschnittValidationConfiguration config) {
        super(model, config);
    }

    @Override
    public String getDisplayName(String modelDisplayName) {
        if (StringUtils.isBlank(modelDisplayName)) {
            return NbBundle.getMessage(ZensurenschnittValidation.class, "ZensurenschnittValidation.emptyName");
        }
        return NbBundle.getMessage(ZensurenschnittValidation.class, "ZensurenschnittValidation.name", modelDisplayName);
    }

    @Override
    public void run() {
        final Set<DocumentId> set = model.getStudents().stream()
                .map(Student::getStudentId)
                .map(model::getReportDocuments)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        fireStart(set.size());
        try {
            runSet(set, 0);
        } finally {
            fireStop();
        }
    }

    private void runSet(final Set<DocumentId> set, final int count) {
        final Iterator<DocumentId> it = set.iterator();
        final List<Exception> ex = new ArrayList<>();
        while (it.hasNext()) {
            final DocumentId d = it.next();
            try {
                processOneDocument(model.getReportDocument(d));
                it.remove();
            } catch (Exception e) {
                Logger.getLogger(ZensurenschnittValidation.class.getCanonicalName()).log(Level.INFO, e.getMessage(), e);
                ex.add(e);
            }
        }
        if (!set.isEmpty()) {
            if (count < 3) {
                runSet(set, count + 1);
            } else {
                final String msg = NbBundle.getMessage(ZensurenschnittValidation.class, "ZensurenschnittValidation.runSet.multipleExceptions", ex.size());
                final ValidationRunException vre = new ValidationRunException(msg);
                vre.setCount(count);
                throw vre;
            }
        }
    }

    protected void processOneDocument(final Report r) {
        if (!canEvaluate(r)) {
            return;
        }
        final String gradeConvention = config.getGradeConvention();
        final SubjectFilter[] sg = config.getSubjectGroups();
        final Double av = getAverage(r, null, gradeConvention);
        removeResults(r.getDocumentId());
        if (av != null) {
            final R result = createResult(r, config);
            result.setAverage(av);
            if (sg != null) {
                for (SubjectFilter key : sg) {
                    final Double d = getAverage(r, key, gradeConvention);
                    double avs = d == null ? 0.0 : d;
                    result.getFiltered().put(key.getName(), avs);
                }
            }
            addResult(r.getDocumentId(), result);
        }
    }

    protected Double getAverage(final Report r, SubjectFilter key, final String gradeConvention) {
        final DoubleSummaryStatistics dss = r.getSubjects().stream()
                .filter(s -> key == null || key.matches(s))
                .map(s -> select(r, s))
                .filter(Objects::nonNull)
                .mapToDouble(d -> (double) d)
                .summaryStatistics();
        return dss.getCount() != 0d ? dss.getAverage() : null;
    }

    protected boolean canEvaluate(Report r) {
        return true;
    }

    protected Double select(Report r, Subject s) {
        Grade grade = r.select(s);
        if (grade != null) {
            grade = adjustGrade(grade);
        }
        return config.getGradeDoubleConverter().toDouble(r, s, grade);
    }

    protected Grade adjustGrade(Grade grade) {
        if (!grade.getConvention().equals(config.getGradeConvention())) {
            return null;
        }
        boolean unbias = findBooleanProperty("unbias");
        if (unbias && grade instanceof Grade.Biasable) {
            grade = ((Grade.Biasable) grade).getUnbiased();
        }
        return grade;
    }

    protected abstract R createResult(Report report, ZensurenschnittValidationConfiguration config);

    protected String findProperty(String key) {
        if (config.getProperties() != null) {
            return Arrays.stream(config.getProperties())
                    .filter(p -> p.getKey().equals(key))
                    .map(Property::getValue)
                    .collect(CollectionUtil.singleOrNull());
        }
        return null;
    }

    protected boolean findBooleanProperty(String key) {
        return Boolean.valueOf(findProperty(key));
    }

}
