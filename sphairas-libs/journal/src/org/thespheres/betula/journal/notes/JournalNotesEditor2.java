/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.notes;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Properties;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle;
import org.openide.util.io.NullInputStream;
import org.openide.windows.TopComponent;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"JournalNotesEditor2.openAction=Bemerkungen (Berichtshefte)",
    "JournalNotesEditor2.displayName=Berichtsheft-Bemerkungen",
    "JournalNotesEditor2.current.displayName=Berichtsheft-Bemerkungen ({0})"})
@ActionID(category = "Window", id = "org.thespheres.betula.journal.notes.JournalNotesEditor2")
@ActionReference(path = "Menu/Window/betula-project-local-windows", position = 210)
@ConvertAsProperties(dtd = "-//org.thespheres.betula.journal.notes//JournalNotesEditor2//EN",
        autostore = false)
@TopComponent.OpenActionRegistration(displayName = "#JournalNotesEditor2.openAction",
        preferredID = "JournalNotesEditor2")
@TopComponent.Description(preferredID = "JournalNotesEditor2",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "properties", openAtStartup = false)
public class JournalNotesEditor2 extends AbstractJournalNotesEditor {

    public JournalNotesEditor2() {
        super();
    }

    @Override
    protected synchronized void reload(EditorKit kit, StyledDocument doc) throws IOException {
        final NotesSectionsProvider2 nsp = new NotesSectionsProvider2(this, doc, current);
        try (Reader reader = nsp.createGuardedReader(new NullInputStream(), Charset.defaultCharset())) {
            kit.read(reader, doc, 0);
        } catch (BadLocationException ex) {
            throw new IOException(ex);
        }
    }

    void writeProperties(Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

}
