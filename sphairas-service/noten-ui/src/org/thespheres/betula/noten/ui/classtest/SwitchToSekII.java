/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.ui.classtest;

import org.thespheres.betula.noten.ui.ClasstestSupport;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.noten.impl.NotenOSAssessment;
import org.thespheres.betula.util.Int2;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.noten.ui.classtest.SwitchToSekII")
@ActionRegistration(displayName = "#SwitchToSekII.displayName")
@ActionReference(path = "Loaders/text/betula-classtest-file+xml/Actions", position = 4000)
@Messages("SwitchToSekII.displayName=Bewertung in SEK II ändern")
public final class SwitchToSekII implements ActionListener {

    private final ClasstestSupport context;

    public SwitchToSekII(ClasstestSupport context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        final NotenOSAssessment newCtx = new NotenOSAssessment(Int2.fromInternalValue(20));
        context.switchAssessmentContext(newCtx);
    }
}
