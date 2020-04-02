/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import org.thespheres.betula.services.ui.util.dav.VCardStudents;
import java.io.IOException;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportTargetsItem;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
public abstract class AbstractDelayedStudents<I extends ImportTargetsItem> implements TaskListener {

    protected final I target;
    private Object collection;

    public AbstractDelayedStudents(I target) {
        this.target = target;
    }

    public void setConfiguration(ImportTarget config) {
        try {
            collection = VCardStudentsUtil.findFromConfiguration(config);
        } catch (IOException ex) {
            collection = ex;
            return;
        }
        VCardStudents students = (VCardStudents) collection;
        if (!students.getLoadTask().isFinished()) {
            students.getLoadTask().addTaskListener(this);
        }
        taskFinished(null);
    }

    @Override
    public void taskFinished(Task task) {
        final VCardStudents students = (VCardStudents) collection;
        if (!students.getLoadTask().isFinished()) {
            return;
        }
        students.getLoadTask().removeTaskListener(this);
        onLoad();
    }

    protected VCardStudents getVCardStudents() throws IOException {
        if (collection instanceof IOException) {
            throw (IOException) collection;
        }
        return (VCardStudents) collection;
    }

    protected abstract void onLoad();
}
