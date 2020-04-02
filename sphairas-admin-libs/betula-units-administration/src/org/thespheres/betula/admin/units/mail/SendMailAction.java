/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.mail;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.net.smtp.AuthenticatingSMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.TargetAssessmentSelectionProvider;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.document.model.UnitsModel;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula", id = "org.thespheres.betula.admin.units.mail.SendMailAction")
@ActionRegistration(displayName = "#CTL_SendMailAction", surviveFocusChange = true, asynchronous = true)
//@ActionReferences({
//    @ActionReference(path = "Loaders/application/betula-unit-context/Actions", position = 51000), //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)
//})
@NbBundle.Messages({
    "CTL_SendMailAction=Mail verschicken..."
})
public class SendMailAction implements ActionListener {

    private final RemoteTargetAssessmentDocument rtad;
    private final PrimaryUnitOpenSupport uos;

    public SendMailAction(TargetAssessmentSelectionProvider sp) {
        this.rtad = sp.getLookup().lookup(RemoteTargetAssessmentDocument.class);
        this.uos = sp.getLookup().lookup(PrimaryUnitOpenSupport.class);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            UnitsModel<?, ?> rModel;
            if (rtad != null && uos != null && (rModel = uos.getRemoteUnitsModel()) != null) {
                AuthenticatingSMTPClient client = new AuthenticatingSMTPClient();
                client.connect("xxxxxxxxxxxxxxxxxxxx.de");

                if (!SMTPReply.isPositiveCompletion(client.getReplyCode())) {
                    client.disconnect();
                    System.err.println("SMTP server refused connection.");
                    System.exit(1);
                }

                client.login();

                client.setSender("xxxxxxxxxxxxxxxxxxxxx@xxxxxxxxxxxxxx.de");
                client.addRecipient(rtad.getSignee("entitled.signee").toString());

                Writer writer = client.sendMessageData();

//                if (writer != null) {
//                    writer.write(SimpleSMTPHeader(from, to, subject));
//                    Util.copyReader(fileReader, writer);
//                    writer.close();
//                    client.completePendingCommand();
//                }
//
//                if (fileReader != null) {
//                    fileReader.close();
//                }

                client.logout();

                client.disconnect();
            }
        } catch (IOException ex) {
        } catch (NoSuchAlgorithmException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
