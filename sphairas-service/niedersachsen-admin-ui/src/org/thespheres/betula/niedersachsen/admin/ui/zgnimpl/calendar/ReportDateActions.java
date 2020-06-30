/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.calendar;

import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.niedersachsen.admin.ui.RemoteReportsModel2;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.ui.util.MultiContextAction;
import org.thespheres.betula.ui.util.MultiContextSensitiveAction;

/**
 *
 * @author boris.heithecker
 */
@Messages({"ReportDateActions.display=Zeugnisausgabedatum setzen",
    "ReportDateActions.display.context.student=Zeugnisausgabedatum setzen für {0}",
    "ReportDateActions.display.context.multiple=Zeugnisausgabedatum setzen für {0} Zeugnisse",
    "ReportDateActions.display.context.unitTerm=Zeugnisausgabedatum setzen für das aktuelle Halbjahr",
    "ReportDateActions.display.context.unitTermAbschluss=Zeugnisausgabedatum setzen für das aktuelle Halbjahr (Abschlusszeugnisse)",
    "ReportDateActions.empty.value=leer",
    "ReportDateActions.title=Zeugnisausgabedatum"})
public class ReportDateActions extends MultiContextAction {

    private boolean abschlussZeugnis = false;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    private ReportDateActions(final Class... types) {
        super(types);
    }

    @ActionID(category = "Betula", id = "org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.calendar.ReportDateActions.createStudentAction")
    @ActionRegistration(displayName = "#ReportDateActions.display", asynchronous = true, lazy = false)
    @ActionReferences({
        @ActionReference(path = "Loaders/application/betula-unit-context/Actions", position = 7110, separatorBefore = 7000)})
    public static Action createStudentAction() {
        final ReportDateActions ret = new ReportDateActions(RemoteReportsModel2.class);
        ret.multiTypes.add(ReportData2.class);
        ret.putValue("iconBase", "org/thespheres/betula/niedersachsen/berichte/ui/resources/layer-shape-text.png");
        return ret;
    }

    @ActionID(category = "Betula", id = "org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.calendar.ReportDateActions.createUnitAction")
    @ActionRegistration(displayName = "#ReportDateActions.display", asynchronous = true, lazy = false)
    @ActionReferences({
        @ActionReference(path = "Loaders/application/betula-unit-data/Actions", position = 6100)})
    public static Action createUnitAction() {
        final ReportDateActions ret = new ReportDateActions();
        ret.multiTypes.add(PrimaryUnitOpenSupport.class);
        ret.putValue("iconBase", "org/thespheres/betula/niedersachsen/berichte/ui/resources/layer-shape-text.png");
        return ret;
    }

    @ActionID(category = "Betula", id = "org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.calendar.ReportDateActions.createUnitAbschlussAction")
    @ActionRegistration(displayName = "#ReportDateActions.display", asynchronous = true, lazy = false)
    @ActionReferences({
        @ActionReference(path = "Loaders/application/betula-unit-data/Actions", position = 6200)})
    public static Action createUnitAbschlussAction() {
        final ReportDateActions ret = new ReportDateActions();
        ret.abschlussZeugnis = true;
        ret.multiTypes.add(PrimaryUnitOpenSupport.class);
        ret.putValue("iconBase", "org/thespheres/betula/niedersachsen/berichte/ui/resources/layer-shape-text.png");
        return ret;
    }

    @Override
    protected MultiContextSensitiveAction createMultiContextSensitiveAction() {
        return new ReportDateAction(abschlussZeugnis);
    }

}
