/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.actions;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ClipboardEvent;
import org.openide.util.datatransfer.ClipboardListener;
import org.openide.util.datatransfer.ExClipboard;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.TargetAssessmentSelectionProvider;
import org.thespheres.betula.admin.units.MoveStudentsToTargetDropSupport;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula", id = "org.thespheres.betula.admin.units.actions.InsertStudentsFromClipboardAction")
@ActionRegistration(displayName = "#InsertStudentsFromClipboardAction.displayName", lazy = false)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-unit-context/Actions", position = 6000), //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)
})
@NbBundle.Messages({"InsertStudentsFromClipboardAction.displayName=Eintrag für {0} anlegen",
    "InsertStudentsFromClipboardAction.displayName.disabled=Eintrag anlegen",
    "InsertStudentsFromClipboardAction.displayName.multiple={0} Einträge anlegen"})
public class InsertStudentsFromClipboardAction extends AbstractAction implements ContextAwareAction {

    private Listener listener;

    public InsertStudentsFromClipboardAction() {
    }

    private InsertStudentsFromClipboardAction(Lookup context) {
        listener = new Listener(this, context.lookupResult(TargetAssessmentSelectionProvider.class));
        updateEnabled();
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new InsertStudentsFromClipboardAction(context);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RemoteStudent[] studs = extractRemoteStudent().stream()
                .toArray(RemoteStudent[]::new);
        MoveStudentsToTargetDropSupport ds = null;
        TargetAssessmentSelectionProvider tasp = listener.getInstance();
        if (tasp != null) {
            AbstractUnitOpenSupport uos = tasp.getLookup().lookup(AbstractUnitOpenSupport.class);
            if (uos != null) {
                ds = uos.findDropSupport();
            }
        }
        if (ds != null && studs.length != 0) {
            ds.moveStudentsToTarget(tasp, studs);
        }
    }

    private void updateEnabled() {
        List<RemoteStudent> data = extractRemoteStudent();
        MoveStudentsToTargetDropSupport ds = null;
        TargetAssessmentSelectionProvider tasp = listener.getInstance();
        if (tasp != null) {
            AbstractUnitOpenSupport uos = tasp.getLookup().lookup(AbstractUnitOpenSupport.class);
            if (uos != null) {
                ds = uos.findDropSupport();
            }
        }
        boolean ena = !data.isEmpty() && ds != null;
        setEnabled(ena);
        putValue(Action.NAME, getName(data));
    }

    private String getName(List<RemoteStudent> data) {
        if (data.isEmpty()) {
            return NbBundle.getMessage(InsertStudentsFromClipboardAction.class, "InsertStudentsFromClipboardAction.displayName.disabled");
        } else if (data.size() == 1) {
            String name = data.get(0).getFullName();
            return NbBundle.getMessage(InsertStudentsFromClipboardAction.class, "InsertStudentsFromClipboardAction.displayName", name);
        } else {
            return NbBundle.getMessage(InsertStudentsFromClipboardAction.class, "InsertStudentsFromClipboardAction.displayName.multiple", data.size());
        }
    }

    private static List<RemoteStudent> extractRemoteStudent() {
        ExClipboard clip = Lookup.getDefault().lookup(ExClipboard.class);
        Transferable cont = clip.getContents(null);
        if (cont != null) {
            Node[] n = NodeTransfer.nodes(cont, NodeTransfer.CLIPBOARD_COPY);
            if (n != null) {
                return Arrays.stream(n)
                        .map(Node::getLookup)
                        .map(lkp -> lkp.lookupAll(RemoteStudent.class))
                        .flatMap(Collection::stream)
                        .map(RemoteStudent.class::cast)
                        .collect(Collectors.toList());
            }
//            try {
//                return (List<RemoteStudent>) cont.getTransferData(CopyStudentsToClipboardAction.createDndFlavor(NodeTransfer.CLIPBOARD_COPY));
//            } catch (UnsupportedFlavorException | IOException ex) {
//            }
        }
        return Collections.EMPTY_LIST;
    }

    private final static class Listener<C> implements LookupListener, ClipboardListener {

        private final WeakReference<InsertStudentsFromClipboardAction> reference;
        private final Lookup.Result<TargetAssessmentSelectionProvider> result;

        @SuppressWarnings("LeakingThisInConstructor")
        private Listener(InsertStudentsFromClipboardAction ref, Lookup.Result<TargetAssessmentSelectionProvider> result) {
            this.reference = new WeakReference<>(ref);
            this.result = result;
            this.result.addLookupListener(this);
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            change();
        }

        @Override
        public void clipboardChanged(ClipboardEvent ev) {
            change();
        }

        private TargetAssessmentSelectionProvider getInstance() {
            return result.allInstances().size() == 1 ? result.allInstances().iterator().next() : null;
        }

        private void change() {
            InsertStudentsFromClipboardAction action = reference.get();
            if (action != null) {
                action.updateEnabled();
            } else {
                result.removeLookupListener(this);
            }
        }
    }
}
