/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admincontainer.util.ActionImportTargetsItem;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;

@ActionID(
        category = "Betula",
        id = "org.thespheres.betula.admin.container.action.ClearUnitStudentsAction")
@ActionRegistration(
        displayName = "#ClearUnitStudentsAction.title",
        surviveFocusChange = true,
        asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-unit-data/Actions", position = 2300)})
@NbBundle.Messages({"ClearUnitStudentsAction.title=Klassenliste leeren",
    "ClearUnitStudentsAction.message.start=Klassenliste leeren ..."})
public final class ClearUnitStudentsAction implements ActionListener {

    private final List<PrimaryUnitOpenSupport> context;

    public ClearUnitStudentsAction(List<PrimaryUnitOpenSupport> uos) throws IOException {
        this.context = uos;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        notifyStart();
//        final Map<WebServiceProvider, Set<UnitId>> map = new HashMap<>();
        Map<WebServiceProvider, Set<UnitId>> collect = context.stream()
                .collect(Collectors.groupingBy(puos -> {
                    try {
                        return puos.findWebServiceProvider();
                    } catch (IOException ex) {
                        ex.printStackTrace(ImportUtil.getIO().getErr());
                        return null;
                    }
                },
                HashMap::new,//This will accept null values
                Collectors.mapping(PrimaryUnitOpenSupport::getUnitId,
                        Collectors.toSet())));
//        context.stream()
//                .forEach(puos -> {
//                    try {
//                        final WebServiceProvider wsp = puos.findWebServiceProvider();
//                        map.computeIfAbsent(wsp, p -> new HashSet<>())
//                        .add(puos.getUnitId());
//                    } catch (IOException ex) {
//                        ex.printStackTrace(ImportUtil.getIO().getErr());
//                    }
//                });
//        map.forEach((wsp, s) -> updateSGLPU(wsp, s));
        collect.forEach((wsp, s) -> processSelection(wsp, s));
    }

//    private void updateSGLPU(WebServiceProvider wsp, Set<UnitId> units) {
//        ContainerBuilder builder = new ContainerBuilder();
//        int numImport = 0;
//        long timeStart = System.currentTimeMillis();
//
//        //kurszuordnungen, termtargets
//        String[] partPath = new String[]{"units", "participants"};
//        for (UnitId unitId : units) {
//            boolean fileUnit = true; //!
//            UnitEntry uEntry = builder.updateUnitAction(unitId, new StudentId[0], partPath, null, Action.FILE, fileUnit, true);
////            uEntry.getValue().getMarkerSet().add(new AbstractMarker("betula-db", "primary-unit", null));
////            uEntry.setDocumentValidity(sibankkurs.getDeleteDate());
////            uEntry.setPreferredTermSchedule(LSchB.PROVIDER_INFO.getURL());
////            uEntry.setDocumentValidity(sibankkurs.getDeleteDate());
//            ++numImport;
//        }
//
////        Template t = builder.createTemplate(null, KGS.STUDENT_BILDUNGSGANG_DOCID, null, ImportStudentsTableModel.STUDENTS_MARKER_PATH, null, null);
////        sglMap.entrySet().stream().map(entry -> new Entry(Action.FILE, entry.getKey(), new MarkerAdapter(entry.getValue()))).forEach(ch -> t.getChildren().add(ch));
//        BetulaWebService service;
//        try {
//            service = wsp.createServicePort();
//        } catch (IOException ex) {
//            ex.printStackTrace(ImportUtil.getIO().getErr());
//            return;
//        }
//        try {
//            String msg2 = NbBundle.getMessage(ClearUnitStudentsAction.class, "ClearUnitStudentsAction.message.network");
//            ImportUtil.getIO().getOut().println(msg2);
//            service.solicit(builder.getContainer());
//            long dur = System.currentTimeMillis() - timeStart;
//            msg2 = NbBundle.getMessage(ClearUnitStudentsAction.class, "ClearUnitStudentsAction.message.finish", numImport, dur);
//            ImportUtil.getIO().getOut().println(msg2);
//        } catch (NotFoundException | PreexistingException | UnauthorizedException ex) {
//            ImportUtil.getIO().getErr().println(ex);
//        }
//    }
    private void notifyStart() {
        ImportUtil.getIO().select();
        String msg = NbBundle.getMessage(ClearUnitStudentsAction.class, "ClearUnitStudentsAction.message.start");
        ImportUtil.getIO().getOut().println(msg);
    }

    private void processSelection(final WebServiceProvider wsp, final Set<UnitId> units) {
        if (wsp == null) {
            return;
        }
        final ActionImportTargetsItem[] items = units.stream()
                .map(u -> rtadToTargetDoc(u))
                .toArray(ActionImportTargetsItem[]::new);
        final TargetItemsUpdater update = new TargetItemsUpdater(items, wsp, null, null);
        wsp.getDefaultRequestProcessor().post(update);
    }

    private ActionImportTargetsItem rtadToTargetDoc(final UnitId toClear) {

        return new ActionImportTargetsItem(toClear.toString(), null, true) {
            @Override
            public TargetDocumentProperties[] getImportTargets() {
                return new TargetDocumentProperties[]{};
            }

            @Override
            public UnitId getUnitId() {
                return toClear;
            }

            @Override
            public StudentId[] getUnitStudents() {
                return new StudentId[0];
            }

            @Override
            public boolean fileUnitParticipants() {
                return true;
            }

        };
    }
}
