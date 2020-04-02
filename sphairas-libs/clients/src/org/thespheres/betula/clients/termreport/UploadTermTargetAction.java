package org.thespheres.betula.clients.termreport;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ws.ServiceException;
import org.thespheres.betula.services.ws.WebServiceProvider;

/**
 *
 * @author boris.heithecker
 */
@ActionID(category = "Betula",
        id = "oorg.thespheres.betula.clients.termreport.UploadTermTargetAction")
@ActionRegistration(
        displayName = "#UploadTermTargetAction.displayName")
@ActionReferences({
    @ActionReference(path = "Loaders/text/betula-term-report-target-assessment-context/Actions", position = 600000, separatorBefore = 100000, separatorAfter = 1000000)})
@NbBundle.Messages("UploadTermTargetAction.displayName=Zensuren hochladen")
public class UploadTermTargetAction implements ActionListener {

    private final List<XmlRemoteTargetAssessmentProvider> context;

    public UploadTermTargetAction(List<XmlRemoteTargetAssessmentProvider> p) {
        this.context = p;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        context.stream()
                .forEach(ul -> {
                    final String provider = ul.env.getServiceProvider();
                    final Container container = ul.createContainer();
                    ul.RP.post(() -> submitContainer(provider, container));
                });
    }

    private void submitContainer(String provider, Container container) {
        try {
            doSubmitContainter(provider, container);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void doSubmitContainter(String provider, Container container) throws IOException {
        WebServiceProvider wsp;
        try {
            wsp = WebProvider.find(provider, WebServiceProvider.class);
        } catch (NoProviderException e) {
            throw new IOException(e);
        }
        try {
            wsp.createServicePort().solicit(container);
        } catch (ServiceException ex) {
            throw new IOException(ex);
        }
    }
}
