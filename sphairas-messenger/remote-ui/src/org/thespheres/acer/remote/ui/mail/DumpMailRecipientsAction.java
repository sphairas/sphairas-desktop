/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.mail;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.ContainerBuilder;

@ActionID(
        category = "Betula",
        id = "org.thespheres.acer.remote.ui.mail.DumpMailRecipientsAction")
@ActionRegistration(
        displayName = "#DumpMailRecipientsAction.displayName")
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-remote-students/Actions", position = 9120, separatorBefore = 9000)
})
@Messages({"DumpMailRecipientsAction.displayName=E-Mail-Adressaten",
    "DumpMailRecipientsAction.message=Die Liste wurde in die Zwischenablage geladen."})
public final class DumpMailRecipientsAction implements ActionListener {

    private final RemoteStudent student;

    public DumpMailRecipientsAction(RemoteStudent context) {
        this.student = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        try {
            final Signee[] result = Util.RP(student.getWebServiceProvider()).submit(this::fetchAddresses).get(7l, TimeUnit.SECONDS);
            final StringJoiner sj = new StringJoiner(System.getProperty("line.separator"));
            Arrays.stream(result)
                    .map(Signee::toString)
                    .forEach(sj::add);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sj.toString()), null);
            String title = NbBundle.getMessage(DumpMailRecipientsAction.class, "DumpMailRecipientsAction.displayName");
            String message = NbBundle.getMessage(DumpMailRecipientsAction.class, "DumpMailRecipientsAction.message");
            NotifyDescriptor nd = new NotifyDescriptor(message, title, NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.INFORMATION_MESSAGE, null, null);
            DialogDisplayer.getDefault().notify(nd);
        } catch (InterruptedException | ExecutionException | TimeoutException | HeadlessException ex) {
        }
    }

    private Signee[] fetchAddresses() {
//        remote.login();
//        TermGradeTargetAssessmentDocumentBean bean = remote.lookup(TermGradeTargetAssessmentDocumentBean.class);
//        return bean.getSignees(student.getStudentId(), new String[]{"entitled.signee"});

        final ContainerBuilder builder = new ContainerBuilder();
        final String[] path = Paths.STUDENTS_SIGNEES_PATH;
        final StudentId sid = student.getStudentId();
        final Entry<StudentId, ?> node = (Entry<StudentId, ?>) builder.createTemplate(null, sid, null, path, null, null);
        node.getChildren().add(new Template<>(Action.REQUEST_COMPLETION, "entitled.signee"));

        final Container response;
        try {
            response = WebProvider.find(student.getWebServiceProvider(), WebServiceProvider.class).createServicePort().solicit(builder.getContainer());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return new Signee[]{};
        }

        return DocumentUtilities.findEnvelope(response, path).stream()
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .filter(e -> Objects.equals(e.getIdentity(), sid))
                .flatMap(e -> e.getChildren().stream())
                .filter(t -> Objects.equals(t.getValue(), "entitled.signee"))
                .flatMap(t -> t.getChildren().stream())
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .filter(e -> e.getIdentity() instanceof Signee)
                .map(e -> (Signee) e.getIdentity())
                .toArray(Signee[]::new);
    }
}
