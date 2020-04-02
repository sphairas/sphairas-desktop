/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.impl;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.jdesktop.swingx.JXDatePicker;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakSet;
import org.openide.util.actions.CallableSystemAction;
import org.thespheres.betula.services.WorkingDate;

/**
 *
 * @author boris.heithecker
 */
@ActionID(id = "org.thespheres.betula.local.ui.WorkingDateAction", category = "Betula")
@ActionRegistration(displayName = "#WorkingDateAction.displayName", lazy = false)
@ActionReference(path = "Toolbars/Settings", position = 1000, separatorBefore = 900) //, separatorBefore = 8999, separatorAfter = 9001
//@ActionReference(name = "D-I", path = "Shortcuts")
@NbBundle.Messages({"WorkingDateAction.displayName=Arbeitsdatum",
    "WorkingDateAction.displayLabel=Arbeitsdatum:",
    "WorkingDateAction.dateFormat=d. M. yy"})
public class WorkingDateAction extends CallableSystemAction {

    private JPanel panel;
    private final JXDatePicker datePicker = new JXDatePicker(Locale.getDefault());
    private Date date = datePicker.getLinkDay();
    public final ChangeSupport cSupport = new ChangeSupport(this);
    private final WeakSet<UpdaterImpl> updater = new WeakSet<>(1000);
    private final static AtomicLong ID = new AtomicLong(1l);

    @Override
    public void performAction() {
        if (datePicker != null) {
            Date newDate = datePicker.getDate();
            if (!date.equals(newDate)) {
                this.date = newDate;
                cSupport.fireChange();
                updateUI();
            }
        }
    }

    public Date getCurrentWorkingDate() {
        return date;
    }

    public boolean isLinkDay() {
        return getCurrentWorkingDate().equals(datePicker.getLinkDay());
    }

    public WorkingDate.Updater markUpdating() {
        final UpdaterImpl ret = new UpdaterImpl();
        synchronized (updater) {
            updater.add(ret);
            updateUI();
        }
        return ret;
    }

    private void updateUI() {
        Mutex.EVENT.writeAccess(() -> {
            if (!isLinkDay()) {
                datePicker.setBorder(new LineBorder(Color.RED, 4, true));
            } else {
                datePicker.setBorder(null);
            }
            if (!updater.isEmpty()) {
                datePicker.getEditor().setEnabled(false);
                datePicker.getEditor().setBackground(Color.PINK);
            } else {
                datePicker.getEditor().setEnabled(true);
                datePicker.getEditor().setBackground(null);
            }
        });
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(WorkingDateAction.class, "WorkingDateAction.displayName");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public java.awt.Component getToolbarPresenter() {
        if (panel == null) {
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(false); // don't interrupt JToolBar background
//            panel.setMaximumSize(new Dimension(350, 80));
//            panel.setMinimumSize(new Dimension(150, 0));
//            panel.setPreferredSize(new Dimension(350, 23));
            JLabel label = new JLabel(NbBundle.getMessage(WorkingDateAction.class, "WorkingDateAction.displayLabel"));
            label.setBorder(new EmptyBorder(0, 2, 0, 10));
//            panel.add(label, BorderLayout.WEST);
            String df = NbBundle.getMessage(WorkingDateAction.class, "WorkingDateAction.dateFormat");
            datePicker.setFormats(df);
            datePicker.setDate(getCurrentWorkingDate());
            datePicker.addActionListener(this);
//            panel.add(datePicker, BorderLayout.EAST);
            label.setLabelFor(datePicker);
            panel.add(label, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(1, 6, 1, 0), 0, 0));
            panel.add(datePicker, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(1, 0, 1, 5), 0, 0));
        }
        return panel;
    }

    class UpdaterImpl extends WorkingDate.Updater {

        UpdaterImpl() {
            super(ID.getAndIncrement());
        }

        @Override
        public void unmarkUpdating() {
            synchronized (updater) {
                updater.remove(this);
                updateUI();
            }
        }

    }
}
