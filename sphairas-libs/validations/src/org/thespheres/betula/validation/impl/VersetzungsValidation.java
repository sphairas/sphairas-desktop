package org.thespheres.betula.validation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.Student;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.model.History;
import org.thespheres.betula.document.model.Subject;
import org.thespheres.betula.document.util.AbstractReportDocument;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.validation.ValidationResultSet;
import org.thespheres.betula.validation.impl.Policy.GroupingCondition;
import org.thespheres.betula.validation.impl.VersetzungsValidationConfiguration.Property;

/**
 *
 * @author boris.heithecker
 * @param <Report>
 * @param <H>
 * @param <R>
 */
//Einzelne, z.B. GY 5-9, GY 10 etc
@Messages({"VersetzungsValidation.emptyName=Versetzung",
    "VersetzungsValidation.name=Versetzung in {0}",
    "VersetzungsValidation.runSet.multipleExceptions=Bei Durchlaufen der Versetzungs-Valiedierung sind {0} Fehler aufgetreten."})
public abstract class VersetzungsValidation<Report extends AbstractReportDocument, H extends History<?, Report>, R extends VersetzungsResult> extends AbstractValidationSet<H, R, DocumentId, VersetzungsValidationConfiguration> implements ValidationResultSet<H, R> {

//    public static final String[] HAUPTFAECHER = {"deutsch", "englisch", "franzoesisch", "spanisch", "mathematik"};
    protected VersetzungsValidation(H model, VersetzungsValidationConfiguration config) {
        super(model, config);
    }

    @Override
    public String getDisplayName(String modelDisplayName) {
        if (StringUtils.isBlank(modelDisplayName)) {
            return NbBundle.getMessage(ZensurenschnittValidation.class, "VersetzungsValidation.emptyName");
        }
        return NbBundle.getMessage(ZensurenschnittValidation.class, "VersetzungsValidation.name", modelDisplayName);
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
            DocumentId d = it.next();
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
                String msg = NbBundle.getMessage(VersetzungsValidation.class, "VersetzungsValidation.runSet.multipleExceptions", ex.size());
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

        final List<PolicyLegalHint> hints = new ArrayList<>(1);

        final List<Policy> passed = Arrays.stream(config.getPolicies())
                .filter(p -> applyPolicy(r, p))
                .filter(p -> evaluatePolicy(r, p, hints))
                .collect(Collectors.toList());

        if (!passed.isEmpty()) {
            if (hints.isEmpty()) {
                //Versetzt
                addResult(r.getDocumentId(), createResult(r, null));
            } else {
                final R res = createResult(r, hints);
                //Versetzt mit Anmerkungen/Ausgleichsmöglichkeiten
//                String f = res.message();
//                Logger.getLogger(VersetzungsValidation.class.getName()).log(Level.INFO, r.getDisplayLabel() + ": " + f);
                addResult(r.getDocumentId(), res);

            }
        } else {
            //Nicht versetzt
            removeResults(r.getDocumentId());
        }
    }

    protected boolean applyPolicy(Report r, Policy p) {
        return true;
    }

    protected boolean evaluatePolicy(Report r, Policy policy, final List<PolicyLegalHint> hints) {
        final PolicyRun pc = createPolicyRun(policy);
        final List<Condition> passed = new ArrayList<>();
        final Condition[] cond = policy.getConditions();
        int num = cond.length;
        evaluateConditions(cond, r, null, pc, policy, passed);
        final GroupingCondition[] gcc = policy.getGroupingCondition();
        if (gcc != null) {
            for (GroupingCondition gc : gcc) {
                for (SubjectFilter sg : gc.getSubjectGroups()) {
                    final Condition[] cc = gc.getConditions();
                    if (cc != null) {
                        num += cc.length;
                        evaluateConditions(cc, r, sg, pc, policy, passed);
                    }
                }
            }
        }
        final boolean pass = passed.size() == num;
        if (pass && policy.getHint() != null) {
            hints.add(policy.getHint());
        }
        return pass;
    }

    protected PolicyRun createPolicyRun(Policy policy) {
        return new PolicyRun(policy, this);
    }

    protected void evaluateConditions(Condition[] cc, Report r, SubjectFilter sg, final PolicyRun props, final Policy policy, List<Condition> add) {
        Arrays.stream(cc)
                .filter(gc -> evaluate(r, gc, sg, props, policy))
                .forEach(add::add);
    }

    protected boolean evaluate(Report report, Condition c, SubjectFilter filter, final PolicyRun props, final Policy policy) {
        //TODO: very slow
        final Set<Subject> filtered = report.getSubjects().stream()
                .filter(s -> filter == null || filter.matches(s))
                .filter(s -> applicable(report, c, s, props, policy))
                //                .filter(s -> matcher.matches(report, s, report.select(s), c, props, policy))
                .collect(Collectors.toSet());

        return c.evaluate(filtered, report, props, policy);
    }

    protected boolean applicable(final Report report, final Condition gc, final Subject s, final PolicyRun props, final Policy policy) {
        //honour applicable= attribute
        return true;//default
    }

//    protected boolean matches(final Report report, final Subject s, Grade grade, final String[] pair, final PolicyRun props, final Policy policy) {
////        if (props.unbias && grade instanceof Grade.Biasable) {
////            grade = ((Grade.Biasable) grade).getUnbiased();
////        }
//        final GradeFilter[] gradeFilters = policy.getGradeFilters();
//        if (gradeFilters != null) {
//            for (GradeFilter gf : gradeFilters) {
//                grade = gf.filter(report, s, grade, policy);
//            }
//        }
//        final String match = props.matchShortLabel ? grade.getShortLabel() : grade.getId();
//        final String[] mv = parseMatches(report, s, pair, props);
//        return Arrays.stream(mv).anyMatch(match::equals);
//    }
    protected abstract R createResult(Report report, List<PolicyLegalHint> hints);

    protected boolean canEvaluate(Report r) {
        final Marker[] dm = config.getReportDistinguishingMarkers();
        return dm == null || Arrays.stream(dm).allMatch(Arrays.asList(r.markers())::contains);
    }

    protected String findProperty(String key, Policy policy) {
        String v = Arrays.stream(policy.getProperties())
                .filter(p -> p.getKey().equals(key))
                .collect(CollectionUtil.singleton())
                .map(Property::getValue)
                .orElse(null);
        if (v == null && config.getProperties() != null) {
            v = Arrays.stream(config.getProperties())
                    .filter(p -> p.getKey().equals(key))
                    .map(Property::getValue)
                    .collect(CollectionUtil.singleOrNull());
        }
        return v;
    }

    protected boolean findBooleanProperty(String key, Policy policy) {
        return Boolean.valueOf(findProperty(key, policy));
    }

}
