/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.windows.FoldHandle;
import org.thespheres.betula.admin.units.AdminUnit;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportWizard;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula",
        id = "org.thespheres.betula.admin.database.action.DestroyUnitsAction")
@ActionRegistration(
        displayName = "#DestroyUnitsAction.action.name",
        lazy = true,
        surviveFocusChange = true,
        asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-remote-unit-context/Actions", position = 12000, separatorBefore = 10000)})
@NbBundle.Messages({
    "DestroyUnitsAction.action.name=Gruppe(n) vernichten"})
public class DestroyUnitsAction implements ActionListener {

    private final List<AdminUnit> context;

    public DestroyUnitsAction(final List<AdminUnit> ctx) {
        this.context = ctx;
    }

    private String getName() {
        return NbBundle.getMessage(DestroyUnitsAction.class, "DestroyUnitsAction.action.name");
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        FoldHandle fold;
        try {
            fold = AbstractFileImportAction.messageActionStart(getName());
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(DestroyUnitsAction.class).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            return;
        }

        final Map<String, List<AdminUnit>> m = context.stream()
                .collect(Collectors.groupingBy(AdminUnit::getProvider));

        m.entrySet().forEach(e -> {
            final WebServiceProvider s = WebProvider.find(e.getKey(), WebServiceProvider.class);
            processSelection(s, e.getValue());
        });

        fold.silentFinish();
    }

    private void processSelection(final WebServiceProvider service, final List<AdminUnit> l) {

        final Set<DestroyUnitsImportItem> items = l.stream()
                .map(entry -> DestroyUnitsImportItem.create(entry, service))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final DestroyUnitsListVisualPanel.DestroyUnitsListDescriptor edit = new DestroyUnitsListVisualPanel.DestroyUnitsListDescriptor(items);
        class DestroyActionWizardIterator extends AbstractFileImportWizard<DestroyUnitsListVisualPanel.DestroyUnitsListDescriptor> {

            @Override
            protected List<WizardDescriptor.Panel<DestroyUnitsListVisualPanel.DestroyUnitsListDescriptor>> createPanels() {
                return Collections.singletonList(new DestroyUnitsListVisualPanel.DestroyUnitsListPanel());
            }

        }
        final WizardDescriptor wd = new WizardDescriptor(new DestroyActionWizardIterator(), edit);
//            d.putProperty(AbstractFileImportAction.DATA, xml);
//            d.putProperty(AbstractFileImportAction.SELECTED_NODES, new ChangeSet<>(new HashSet<>()));
//            d.putProperty(AbstractFileImportAction.CLONED_NODES, new HashMap<>());
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
//            wd.setTitleFormat(new MessageFormat("{0} ({1})"));
        wd.setTitle(getName());
        if (DialogDisplayer.getDefault().notify(wd) == WizardDescriptor.FINISH_OPTION) {
            final DestroyUnitsUpdater update = new DestroyUnitsUpdater(items.stream().toArray(DestroyUnitsImportItem[]::new), service);
            service.getDefaultRequestProcessor().post(() -> update.run());
        }
    }

}
