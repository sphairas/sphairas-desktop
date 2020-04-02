/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.project;

import org.thespheres.betula.admin.units.AdminUnits;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.Icon;
import org.netbeans.spi.project.LookupProvider;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.thespheres.betula.Student;
import org.thespheres.betula.Unit;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.AdminUnit;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteStudents;
import org.thespheres.betula.admin.units.util.Util;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
@LookupProvider.Registration(projectType = {"org-thespheres-betula-project-local"})
public class RemoteUnitsLkpRegistration implements LookupProvider {

    @Override
    public Lookup createAdditionalLookup(Lookup baseContext) {
        final LocalFileProperties prop = baseContext.lookup(LocalFileProperties.class);
        final String pt = prop.getProperty("unitIdPattern");
        final String p = prop.getProperty("providerURL");
        if (pt != null && p != null) {
            try {
                WebServiceProvider provider = WebProvider.find(p, WebServiceProvider.class);
                try {
                    final Pattern pattern = Pattern.compile(pt, 0);
                    final InstanceContent ic = new InstanceContent();
                    Util.RP(p).post(new PatternUnitLookup(prop, provider, pattern, ic));
                    return new AbstractLookup(ic);
                } catch (PatternSyntaxException e) {
                }
            } catch (NoProviderException npex) {
                Logger.getLogger(getClass().getCanonicalName()).log(Level.WARNING, npex.getMessage());
            }
        }
        return Lookup.EMPTY;
    }

    static class PatternUnitLookup implements Runnable {

        private final static Map<String, Notification> NOTIFICATIONS = new HashMap<>();
        private final static Map<String, String> NOTIFICATION_MESSAGES = new HashMap<>();
        private final InstanceContent content;
        private final Pattern pattern;
        private final WebServiceProvider provider;
        private final LocalFileProperties prop;

        PatternUnitLookup(LocalFileProperties prop, WebServiceProvider provider, Pattern pattern, InstanceContent ic) {
            this.pattern = pattern;
            this.content = ic;
            this.provider = provider;
            this.prop = prop;
        }

        @Override
        public void run() {
            final Set<UnitId> l = Units.get(provider.getInfo().getURL())
                    .map(Units::getUnits)
                    .orElse(Collections.EMPTY_SET);
            l.stream()
                    .filter(u -> (pattern.matcher(u.getId()).matches()))
                    .map(AdminUnits.get(provider.getInfo().getURL())::getUnit)
                    .peek(u -> Util.RP(provider.getInfo().getURL()).post(() -> setRemoteStudentsPrimaryUnit(u)))
                    .forEach(content::add);
        }

        private void setRemoteStudentsPrimaryUnit(final AdminUnit unit) {
            if (unit.isPrimaryUnit()) {
                unit.getStudents().stream()
                        .map(Student::getStudentId)
                        .map(s -> RemoteStudents.find(provider.getInfo().getURL(), s))
                        .forEach(rs -> setPrimaryUnit(rs, unit));
            }
        }

        private void setPrimaryUnit(final RemoteStudent rs, final AdminUnit unit) {
            final Unit before = rs.getClientProperty(RemoteStudents.PROP_PRIMARY_UNIT, Unit.class);
            if (before != null) {
                notifyNotSet(rs, before, unit);
            }
            rs.putClientProperty(RemoteStudents.PROP_PRIMARY_UNIT, unit);
        }

        @Messages({"PatternUnitLookup.notifyUnset.title=Für einzelne Schüler/Schülerinnen aus {0} ist mehr als eine Klasse eingetragen.",
            "PatternUnitLookup.notifyUnset.message={0}: {1} und {2}"})
        private static void notifyNotSet(RemoteStudent rs, Unit before, AdminUnit unit) {
            final String msg = "Setting primary-unit property of student " + rs.getFullName() + " with id " + rs.getStudentId().toString() + " to " + unit.getDisplayName() + " (" + unit.getUnitId().toString() + "); overwriting previous value " + before.getUnitId().toString() + ". Please fix database!";
            PlatformUtil.getCodeNameBaseLogger(RemoteUnitsLkpRegistration.class).log(LogLevel.INFO_WARNING, msg);
            final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation--frame.png", true);
            final String title = NbBundle.getMessage(PatternUnitLookup.class, "PatternUnitLookup.notifyUnset.title", ProviderRegistry.getDefault().get(unit.getProvider()).getDisplayName());
            String message = NbBundle.getMessage(PatternUnitLookup.class, "PatternUnitLookup.notifyUnset.message", new Object[]{rs.getFullName(), before.getDisplayName(), unit.getDisplayName()});
            synchronized (NOTIFICATIONS) {
                final Notification old = NOTIFICATIONS.get(unit.getProvider());
                if (old != null) {
                    final String msgBefore = NOTIFICATION_MESSAGES.get(unit.getProvider());
                    message = String.join("; ", msgBefore, message);
                    old.clear();
                }
                Notification n = NotificationDisplayer.getDefault()
                        .notify(title, ic, message, null, NotificationDisplayer.Priority.NORMAL, NotificationDisplayer.Category.WARNING);
                NOTIFICATION_MESSAGES.put(unit.getProvider(), message);
                NOTIFICATIONS.put(unit.getProvider(), n);
            }
        }
    }
}
