/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal;

import org.openide.util.NbBundle;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeFactory;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"JournalFileConfiguration.datePickerFormat=E., d. M. yyyy"})
public class JournalConfiguration {

    private static final Grade TARGET_PENDING = GradeFactory.find("niedersachsen.ersatzeintrag", "pending");
    private static final JournalConfiguration INSTANCE = new JournalConfiguration();
    private static final Grade JOURNAL_UNDEF = GradeFactory.find("mitarbeit2", "undefined");
    private static final Grade JOURNAL_DEFAULT = GradeFactory.find("mitarbeit2", "x");

    public static JournalConfiguration getInstance() {
        return INSTANCE;
    }

    public String getJournalEntryPreferredConvention() {
        return "mitarbeit2";//TODO: from LocalFileProperties
    }

    public Grade getDefaultGrade() {
        return TARGET_PENDING;
    }

    public Grade getJournalUndefinedGrade() {
        return JOURNAL_UNDEF;
    }

    public Grade getJournalDefaultGrade() {
        return JOURNAL_DEFAULT;
    }

}
