/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.thespheres.betula.admin.database.DbAdminServiceProvider;
import org.thespheres.betula.database.DBAdminTask;
import org.thespheres.betula.database.DBAdminTaskResult;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"TaskRunner.message.start.task=Starting task {0}",
    "TaskRunner.message.success=Task finished.",
    "TaskRunner.message.error=An error has occurred."})
class TaskRunner implements Runnable {

    private static InputOutput io;
    final DbAdminServiceProvider sp;
    private final DBAdminTask task;

    TaskRunner(DbAdminServiceProvider sp, DBAdminTask task) {
        this.sp = sp;
        this.task = task;
    }

    @NbBundle.Messages({"TaskRunner.ioTab.title=Datenbank-Administration"})
    public static InputOutput getIO() {
        if (io == null) {
//            final Action[] ac = new Action[]{new ImportUtil.ShowDetailsAction()};
            io = IOProvider.getDefault().getIO(NbBundle.getMessage(TaskRunner.class, "TaskRunner.ioTab.title"), null); //, ac);
        }
        return io;
    }

    @Override
    public void run() {
        try {
            getIO().select();
            getIO().getOut().reset();
            getIO().getOut().println(NbBundle.getMessage(TaskRunner.class, "TaskRunner.message.start.task", task.getName()));
            final DBAdminTaskResult result = sp.createDbAdminServicePort().submitTask(task);
            result.getMessage().lines()
                    .forEachOrdered(getIO().getOut()::println);
            if (result.isSuccess()) {
                getIO().getOut().println(NbBundle.getMessage(TaskRunner.class, "TaskRunner.message.success"));
            } else {
                getIO().getErr().println(NbBundle.getMessage(TaskRunner.class, "TaskRunner.message.error"));
            }
        } catch (Exception ex) {
            ex.printStackTrace(getIO().getErr());
        }
    }

}
