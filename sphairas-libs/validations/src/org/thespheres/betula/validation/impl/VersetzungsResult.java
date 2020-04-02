/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.openide.util.NbBundle;
import org.thespheres.betula.Student;
import org.thespheres.betula.document.model.ReportDocument;
import org.thespheres.betula.validation.ValidationResult;

/**
 *
 * @author boris.heithecker
 * @param <S>
 * @param <R>
 */
public abstract class VersetzungsResult<S extends Student, R extends ReportDocument> extends ValidationResult {

    private final S student;
    private final R report;
    private final List<PolicyLegalHint> hints;

    public VersetzungsResult(S student, R report, List<PolicyLegalHint> hints, LocalDateTime time) {
        super(time);
        this.student = student;
        this.report = report;
        this.hints = hints;
    }

    public VersetzungsResult(S student, R report, List<PolicyLegalHint> hints) {
        this.student = student;
        this.report = report;
        this.hints = hints;
    }

    public S getStudent() {
        return student;
    }

    public R getDocument() {
        return report;
    }

    public List<PolicyLegalHint> getLegalHints() {
        return hints;
    }

    public String message() {
        if (hints != null) {
            return hints.stream()
                    .map(this::findMessage)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
        } else {
            return noHintsMessage();
        }
    }

    protected String findMessage(final PolicyLegalHint h) {
        final String v;
        final String bk;
        if ((v = h.getText()) != null && !v.isEmpty()) {
            return v;
        } else if ((bk = h.getBundleKey()) != null) {
            int pos = bk.indexOf("#");
            if (pos != -1 && bk.length() > pos) {
                String bundle = bk.substring(0, pos);
                String key = bk.substring(pos + 1, bk.length());
                try {
                    return NbBundle.getBundle(bundle, Locale.getDefault()).getString(key);
                } catch (MissingResourceException mrex) {
                    return key;
                }
            }
            return bk;
        }
        return null;
    }

    protected String noHintsMessage() {
        return null;
    }
}
