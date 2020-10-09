/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import org.openide.util.NbBundle;
import org.thespheres.betula.*;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Description;
import org.thespheres.betula.document.DocumentEntry;
import org.thespheres.betula.document.Language;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermNotFoundException;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.util.SigneeEntitlement;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.xmlimport.ImportTargetsItem;

/**
 *
 * @author boris.heithecker
 */
public class TargetItemsUpdaterDescriptions {

    static final ResourceBundle BUNDLE = NbBundle.getBundle("org.thespheres.betula.xmlimport.utilities.TargetItemsUpdaterDescriptions");
    static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("d.M.yy HH:mm", Locale.getDefault());
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.yy", Locale.getDefault());
    protected final NamingResolver naming;
    protected final Term current;
    protected final Students students;
    protected final Signees signees;
    private final SchemeProvider schedule;

    public TargetItemsUpdaterDescriptions(Students students, Term current, NamingResolver naming, SchemeProvider tsp, Signees signees) {
        this.naming = naming;
        this.schedule = tsp;
        this.current = current;
        this.students = students;
        this.signees = signees;
    }

    protected Description createTargetMarkerDescription(final DocumentEntry<?> tae, final Marker[] markers) {
        final Language l = new Language(Language.IETF, "de");
        final String key = "targetMarkers";
        String docRes;
        try {
            docRes = naming.resolveDisplayNameResult(tae.getIdentity()).getResolvedName(current);
        } catch (IllegalAuthorityException ex) {
            docRes = tae.getIdentity().toString();
        }
        final String markerRes = Arrays.stream(markers)
                .map(this::markerToString)
                .collect(Collectors.joining(", "));
        final String value = MessageFormat.format(BUNDLE.getString(key), new Object[]{docRes, markerRes});
        return new Description(key, value, l);
    }

    protected String markerToString(final Marker m) {
        return m.getLongLabel() + "(" + m.getId() + ")";
    }

    Description createTargetSubjectAlternativeNameDescription(final DocumentEntry<?> tae, final String subjectAlternativeName) {
        final Language l = new Language(Language.IETF, "de");
        final String key = "targetSubjectAlternativeName";
        String docRes;
        try {
            docRes = naming.resolveDisplayNameResult(tae.getIdentity()).getResolvedName(current);
        } catch (IllegalAuthorityException ex) {
            docRes = tae.getIdentity().toString();
        }
        final String value = MessageFormat.format(BUNDLE.getString(key), new Object[]{docRes, subjectAlternativeName});
        return new Description(key, value, l);
    }

    Description createTargetDocumentValidityDescription(final DocumentEntry<?> tae, final LocalDate deleteDate) {
        final Language l = new Language(Language.IETF, "de");
        final String key = "targetValidity";
        String docRes;
        try {
            docRes = naming.resolveDisplayNameResult(tae.getIdentity()).getResolvedName(current);
        } catch (IllegalAuthorityException ex) {
            docRes = tae.getIdentity().toString();
        }
        final String dateRes = DATE_FORMATTER.format(deleteDate);
        final String value = MessageFormat.format(BUNDLE.getString(key), new Object[]{docRes, dateRes});
        return new Description(key, value, l);
    }

    Description createTargetSigneeInfoDescription(final DocumentEntry<?> tae, final String entitlement, final Signee signee) {
        final Language l = new Language(Language.IETF, "de");
        final String key = "targetSigneeInfo";
        String docRes;
        try {
            docRes = naming.resolveDisplayNameResult(tae.getIdentity()).getResolvedName(current);
        } catch (IllegalAuthorityException ex) {
            docRes = tae.getIdentity().toString();
        }
        final String signeeRes = signee != null ? signees.getSignee(signee) : "---";
        final String entitlementRes = SigneeEntitlement.find(entitlement).map(SigneeEntitlement::getDisplayName).orElse(entitlement);
        final String value = MessageFormat.format(BUNDLE.getString(key), new Object[]{docRes, signeeRes, entitlementRes});
        return new Description(key, value, l);
    }

    protected Description createTargetStudentEntryDescription(final StudentId student, final ImportTargetsItem.GradeEntry gradeEntry) {
        final TermId term = gradeEntry.getTerm();
        Term resolved;
        try {
            resolved = schedule.getScheme(TermSchedule.DEFAULT_SCHEME, TermSchedule.class).resolve(term);
        } catch (TermNotFoundException | IllegalAuthorityException ex) {
            throw new IllegalStateException(ex);
        }
        return createTargetStudentEntryDescription(student, resolved, gradeEntry.getGrade(), gradeEntry.getTimestamp());
    }

    protected Description createTargetStudentEntryDescription(final StudentId student, final Term term, final Grade grade, final Timestamp timestamp) {
        final Language l = new Language(Language.IETF, "de");
        final String key = "targetStudentEntry";
        final String stud = students.find(student).getFullName();
        final String termRes = term.getDisplayName();
        final String gradeRes = grade.getLongLabel();
        final String timestampRes = DATETIME_FORMATTER.format(timestamp.getDateTime());
        final String value = MessageFormat.format(BUNDLE.getString(key), new Object[]{stud, termRes, gradeRes, timestampRes});
        return new Description(key, value, l);
    }

    protected Description createTargetStudentTextEntryDescription(final StudentId student, final Term term, final String text, final Timestamp timestamp) {
        final Language l = new Language(Language.IETF, "de");
        final String key = "targetStudentEntry";
        final String stud = students.find(student).getFullName();
        final String termRes = term.getDisplayName();
        final String timestampRes = DATETIME_FORMATTER.format(timestamp.getDateTime());
        final String value = MessageFormat.format(BUNDLE.getString(key), new Object[]{stud, termRes, text, timestampRes});
        return new Description(key, value, l);
    }

}
