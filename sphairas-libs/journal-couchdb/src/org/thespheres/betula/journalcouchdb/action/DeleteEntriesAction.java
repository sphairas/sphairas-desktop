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
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.ical.ICalendar;

@ActionID(category = "Betula", id = "org.thespheres.betula.calendar.events.couchconn.DeleteEntriesAction")
@ActionRegistration(displayName = "#DeleteEntriesAction.displayName",
        asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Menu/betula-project-local", position = 70100),
    @ActionReference(path = "Loaders/text/betula-journal-file+xml/Actions", position = 3810, separatorBefore = 3000)
})
@Messages("DeleteEntriesAction.displayName=Alle Einträge auf dem Server löschen")
public final class DeleteEntriesAction implements ActionListener {

    private final List<JournalEditor> context;

    public DeleteEntriesAction(List<JournalEditor> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        for (JournalEditor ed : context) {
            final Project prj = ed.getLookup().lookup(Project.class);
            if (prj != null) {
                final Unit unit = ed.getLookup().lookup(Unit.class);
                final ICalendar ical = prj.getLookup().lookup(ICalendar.class);
                final LocalProperties lp = prj.getLookup().lookup(LocalProperties.class);
                final String p = lp.getProperty("providerURL");
                CouchDBProvider c = Lookup.getDefault().lookupAll(CouchDBProvider.class).stream()
                        .filter(cp -> cp.getInfo().getURL().equals(p))
                        .collect(CollectionUtil.singleOrNull());
                if (unit != null && ical != null && c != null) {
                    c.getDefaultRequestProcessor().post(new DeleteTask(unit, ed, ical, c));
                }
            }
        }
    }
}
