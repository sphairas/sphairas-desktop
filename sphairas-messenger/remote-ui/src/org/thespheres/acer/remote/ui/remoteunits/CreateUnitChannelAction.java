/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.remoteunits;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.acer.remote.ui.util.IOUtil;
import org.thespheres.acer.remote.ui.util.JaxRSUtil;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.services.ProviderInfo;

@ActionID(
        category = "Betula",
        id = "org.thespheres.acer.remote.ui.remoteunits.CreateUnitChannelAction"
)
@ActionRegistration(
        displayName = "#CTL_CreateUnitChannelAction",
        iconBase = "org/thespheres/acer/remote/ui/resources/category.png",
        asynchronous = true
)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-unit-data/Actions", position = 9100, separatorBefore = 9000)
})
@Messages({"CTL_CreateUnitChannelAction=Neue Adressatenliste",
    "CreateUnitChannelAction.success=Eine Adressatenlist für die Gruppe {0} wurde erfolgreich erstellt.",
    "CreateUnitChannelAction.success.pu=Eine Adressatenlist für die Klasse {0} wurde erfolgreich erstellt."})
public final class CreateUnitChannelAction implements ActionListener {

    private final PrimaryUnitOpenSupport context;

    public CreateUnitChannelAction(PrimaryUnitOpenSupport context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        IOUtil.getIO().select();
        UnitId uid = context.getUnitId();
        try {
            boolean pu = context.getRemoteUnitsModel().getUnitOpenSupport() instanceof PrimaryUnitOpenSupport;

            final ProviderInfo pi = context.findWebServiceProvider().getInfo();
            final WebTarget target = JaxRSUtil.create(pi.getURL());

            final Response resp = target.path("channel")
                    .queryParam("unit-authority", uid.getAuthority())
                    .queryParam("unit-id", uid.getId())
                    .queryParam("primary", pu)
                    .request()
                    .put(Entity.entity("", MediaType.APPLICATION_JSON), Response.class);

            JaxRSUtil.checkResponse(pi, resp);

            String success;
            if (pu) {
                success = NbBundle.getMessage(CreateUnitChannelAction.class, "CreateUnitChannelAction.success.pu", uid.getId());
            } else {
                success = NbBundle.getMessage(CreateUnitChannelAction.class, "CreateUnitChannelAction.success", uid.getId());
            }
            IOUtil.getIO().getOut().println(success);
        } catch (IOException ex) {
            IOUtil.getIO().getErr().println(ex);
        }

    }
}
