/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.remoteunits;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.thespheres.acer.remote.ui.util.IOUtil;
import org.thespheres.acer.remote.ui.util.JaxRSUtil;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;

@ActionID(
        category = "Betula",
        id = "org.thespheres.acer.remote.ui.remoteunits.CreateStudentsChannelAction"
)
@ActionRegistration(
        displayName = "#CTL_CreateStudentsChannelAction",
        iconBase = "org/thespheres/acer/remote/ui/resources/category.png",
        asynchronous = true
)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-remote-students/Actions", position = 9100, separatorBefore = 9000)
})
@Messages({"CTL_CreateStudentsChannelAction=Neue Adressatenliste",
    "CreateStudentsChannelAction.success=Eine Adressatenlist für die ausgewählten {0} Schülerinnen/Schüler wurde erfolgreich erstellt."})
public final class CreateStudentsChannelAction implements ActionListener {

    private final List<RemoteStudent> context;

    public CreateStudentsChannelAction(List<RemoteStudent> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        final Map<String, List<RemoteStudent>> map = context.stream()
                .collect(Collectors.groupingBy(s -> s.getWebServiceProvider()));
        for (Map.Entry<String, List<RemoteStudent>> e : map.entrySet()) {
            StudentId[] sid = e.getValue().stream()
                    .map(RemoteStudent::getStudentId)
                    .distinct()
                    .toArray(StudentId[]::new);
            String cName;
            String dName;
            switch (sid.length) {
                case 0:
                    return;
                case 1:
                    cName = e.getValue().get(0).getDirectoryName().replace(",", "").replace(" ", "-").toLowerCase();
                    dName = e.getValue().get(0).getDirectoryName();
                    break;
                default:
                    throw new UnsupportedOperationException("Not supported yet.");
            }
            final ProviderInfo pi = ProviderRegistry.getDefault().get(e.getKey());
            updateStudentsChannel(pi, sid, cName, dName);
        }
    }

    public static void updateStudentsChannel(ProviderInfo provider, StudentId[] sid, String cName, String dName) throws MissingResourceException {
        IOUtil.getIO().select();
        final WebTarget target = JaxRSUtil.create(provider.getURL());

        JsonBuilderFactory fac = Json.createBuilderFactory(null);
        JsonArrayBuilder builder = fac.createArrayBuilder();
        JsonObjectBuilder value = fac.createObjectBuilder();
        if (dName != null) {
            value.add("display-name", dName);
        }
        Arrays.stream(sid)
                .map(s -> {
                    final JsonObjectBuilder ret = fac.createObjectBuilder();
                    ret.add("authority", s.getAuthority());
                    ret.add("id", s.getId());
                    return ret;
                })
                .forEach(builder::add);
        value.add("students", builder);

        final Response resp = target.path("channel")
                .path(cName)
                .request()
                .put(Entity.entity(value.build(), MediaType.APPLICATION_JSON), Response.class);

        JaxRSUtil.checkResponse(provider, resp);
    }
}
