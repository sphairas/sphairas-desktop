/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import javax.swing.AbstractAction;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.admindocsrv.Encode;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;

/**
 *
 * @author boris.heithecker
 */
public class StudentsDBRemoveStudentAction extends AbstractAction implements Runnable {
    
    private final List<VCardStudent> context;
    private final VCardStudents collection;
    private final WebProvider service;
     final RequestProcessor.Task task;
    
    public StudentsDBRemoveStudentAction(List<VCardStudent> context, final VCardStudents coll) {
        this.context = context;
        this.collection = coll;
        this.service = WebProvider.find(coll.getProviderUrl(), WebProvider.class);
        task = service.getDefaultRequestProcessor().create(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        //Confirm dialog
        //
        task.schedule(0);
    }
    
    @Override
    public void run() {
//        ImportUtil.getIO()
        for (final VCardStudent s : context) {
            try {
                doDelete(s);
            } catch (IOException ex) {
                ex.printStackTrace(ImportUtil.getIO().getErr());
            }
        }
//        ImportUtil.getIO()
        collection.forceReload();
    }
    
    private void doDelete(final VCardStudent s) throws IOException {
        final StudentId student = s.getStudentId();
        final String href = collection.getStudentsUrl()
                + "?student.authority=" + Encode.getStudentAuthorityEncoded(student)
                + "&student.id=" + Encode.getStudentIdEncoded(student);
        final URI uri = URI.create(href);
        HttpUtilities.delete(service, uri);
    }
}
