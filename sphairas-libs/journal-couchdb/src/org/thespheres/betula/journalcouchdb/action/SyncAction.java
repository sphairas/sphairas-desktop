/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journalcouchdb.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.Unit;
import org.thespheres.betula.couchdb.CouchDBProvider;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.scheme.spi.LessonId;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.ical.ICalendar;

@ActionID(category = "Betula", id = "org.thespheres.betula.calendar.events.couchconn.SyncAction")
@ActionRegistration(displayName = "#SyncAction.displayName",
        asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Menu/betula-project-local", position = 70000),
    @ActionReference(path = "Loaders/text/betula-journal-file+xml/Actions", position = 3800, separatorBefore = 3000)
})
@Messages("SyncAction.displayName=Berichtsheft(e) synchronisieren")
public final class SyncAction implements ActionListener {

    private final List<JournalEditor> context;

    public SyncAction(final List<JournalEditor> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        for (final JournalEditor ed : context) {
            final Project prj = ed.getLookup().lookup(Project.class);
            if (prj != null) {
                final Unit unit = ed.getLookup().lookup(Unit.class);
                final ICalendar ical = prj.getLookup().lookup(ICalendar.class);
                final LocalProperties lp = prj.getLookup().lookup(LocalProperties.class);
                final String p = lp.getProperty("providerURL");
                LessonId targetBase = null;
                final String btid = lp.getProperty("baseTarget.documentId");
                final String auth = lp.getProperty("authority");
                final String btauth = lp.getProperty("baseTarget.authority", auth);
                if (btid != null && btauth != null) {
                    targetBase = new LessonId(btauth, btid);
                }
                final CouchDBProvider c = Lookup.getDefault().lookupAll(CouchDBProvider.class).stream()
                        .filter(cp -> cp.getInfo().getURL().equals(p))
                        .collect(CollectionUtil.singleOrNull());
                if (unit != null && ical != null && c != null) {
                    c.getDefaultRequestProcessor().post(new SyncTask2(unit, targetBase, ed, ical, c));
                }
            }
        }
    }
}
