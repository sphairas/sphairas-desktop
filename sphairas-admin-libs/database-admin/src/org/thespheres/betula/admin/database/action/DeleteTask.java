/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.admin.database.DbAdminServiceProvider;
import org.thespheres.betula.admin.database.action.DeleteTaskVisualPanel.DeleteTaskPanel;
import org.thespheres.betula.database.DBAdminTask;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportWizard;

@ActionID(
        category = "Betula",
        id = "org.thespheres.betula.admin.database.action.DeleteTask"
)
@ActionRegistration(
        displayName = "#DeleteTask.displayName"
)
@ActionReference(path = "Menu/Tools/DbAdmin", position = 2000)
@Messages({"DeleteTask.displayName=Einträge löschen",
    "DeleteTask.wizard.title=Einträge löschen"})
public final class DeleteTask implements ActionListener {

    static final String PROP_TASK = "task";
    static final String PROP_PROVIDER = "provider";

    @Override
    public void actionPerformed(ActionEvent e) {
        final DBAdminTask task = new DBAdminTask("clean-up");
        class WizardIterator extends AbstractFileImportWizard<WizardDescriptor> {

            @Override
            protected ArrayList<WizardDescriptor.Panel<WizardDescriptor>> createPanels() {
                final ArrayList<WizardDescriptor.Panel<WizardDescriptor>> ret = new ArrayList<>();
                ret.add(new DeleteTaskPanel());
                return ret;
            }

        }
        final WizardDescriptor wd = new WizardDescriptor(new WizardIterator());
        wd.putProperty(PROP_TASK, task);
        wd.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        wd.setTitleFormat(new MessageFormat("{0} ({1})"));
        wd.setTitle(NbBundle.getMessage(DeleteTask.class, "DeleteTask.wizard.title"));
        if (DialogDisplayer.getDefault().notify(wd) == WizardDescriptor.FINISH_OPTION) {
            final ProviderInfo info = (ProviderInfo) wd.getProperty(DeleteTask.PROP_PROVIDER);
            if (info != null) {
                final DbAdminServiceProvider provider = DbAdminServiceProvider.create(info);
                final TaskRunner tr = new TaskRunner(provider, task);
                tr.sp.getDefaultRequestProcessor().post(tr);
            }
        }
    }

}
