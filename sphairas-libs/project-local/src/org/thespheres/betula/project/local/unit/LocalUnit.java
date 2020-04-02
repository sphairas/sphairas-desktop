/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.local.unit;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.spi.project.*;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Unit;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.project.*;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.util.XmlStudents;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"LocalUnit.loadInitial.error.title=Fehler beim Laden der lokalen Kursteilnehmer-Datei",
    "LocalUnit.loadInitial.error.message=Es ist ein Fehler beim Laden der lokalen Kursteilnehmer-Datei aufgetreten (Type: {0})",
    "LocalUnit.saveStudents.error.title=Fehler beim Speichern der lokalen Kursteilnehmer-Datei",
    "LocalUnit.saveStudents.error.message=Es ist ein Fehler beim Speichern der lokalen Kursteilnehmer-Datei aufgetreten (Type: {0})"})
public class LocalUnit implements Unit {

    public static final String STUDENTS_FILE = "local-students.xml";
    private final XmlStudents[] students = new XmlStudents[]{null};
    private final String display;
    private final UnitId uid;
    private final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    final RequestProcessor RP = new RequestProcessor(LocalUnit.class);
    private final RequestProcessor.Task load;
    private final URI configPath;
    static JAXBContext jaxb;

    private LocalUnit(UnitId uid, String display, URI configPath) {
        this.uid = uid;
        this.display = display;
        this.configPath = configPath;
        this.load = RP.post(this::loadInitial);
    }

    @Override
    public UnitId getUnitId() {
        return uid;
    }

    @Override
    public String getDisplayName() {
        return display;
    }

    @Override
    public Set<Student> getStudents() {
        load.waitFinished();
        synchronized (students) {
            return NbCollections.checkedSetByFilter(students[0].getStudents(), Student.class, true);
        }
    }

    @Override
    public Student findStudent(StudentId id) {
        load.waitFinished();
        synchronized (students) {
            return students[0].find(id);
        }
    }

    void setStudents(XmlStudents xmlstuds, boolean save) {
        synchronized (students) {
            students[0] = xmlstuds;
        }
        pSupport.firePropertyChange(Unit.PROP_STUDENTS, null, null);
        if (save) {
            RP.post(this::saveStudents);
        }
    }

    private void loadInitial() {
        Path p = Paths.get(configPath).resolve(STUDENTS_FILE);
        if (!Files.exists(p)) {
            setStudents(new XmlStudents(), false);
            return;
        }
        JAXBContext ctx = getJAXBContext();
        try {
            XmlStudents s = (XmlStudents) ctx.createUnmarshaller().unmarshal(Files.newInputStream(p));
            setStudents(s, false);
        } catch (JAXBException | IOException | ClassCastException ex) {
            Logger.getLogger(LocalUnit.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage());
            Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
            String title = NbBundle.getMessage(LocalUnit.class, "LocalUnit.loadInitial.error.title");
            String message = NbBundle.getMessage(LocalUnit.class, "LocalUnit.loadInitial.error.message", ex.getClass().getName());
            NotificationDisplayer.getDefault()
                    .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
        }
    }

    private void saveStudents() {
        Path p = Paths.get(configPath).resolve(STUDENTS_FILE);
        Path backup = null;
        if (Files.exists(p)) {
            Path ba = p.resolveSibling(STUDENTS_FILE + ".bak");
            try {
                backup = Files.copy(p, ba, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                Logger.getLogger(LocalUnit.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage());
            }
        }
        JAXBContext ctx = getJAXBContext();
        XmlStudents save;
        synchronized (students) {
            save = new XmlStudents(getStudents());
        }
        try (OutputStream os = Files.newOutputStream(p)) {
            final Marshaller m = ctx.createMarshaller();
            m.setProperty("jaxb.formatted.output", Boolean.TRUE);
            m.marshal(save, os);
            if (backup != null) {
                try {
                    Files.deleteIfExists(backup);
                } catch (IOException ex) {
                    Logger.getLogger(LocalUnit.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage());
                }
            }
        } catch (IOException | JAXBException ex) {
            Logger.getLogger(LocalUnit.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage());
            Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
            String title = NbBundle.getMessage(LocalUnit.class, "LocalUnit.saveStudents.error.title");
            String message = NbBundle.getMessage(LocalUnit.class, "LocalUnit.saveStudents.error.message", ex.getClass().getName());
            NotificationDisplayer.getDefault()
                    .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
        }
    }

    static JAXBContext getJAXBContext() {
        synchronized (LocalUnit.class) {
            if (jaxb == null) {
                try {
                    jaxb = JAXBContext.newInstance(XmlStudents.class);
                } catch (JAXBException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return jaxb;
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pSupport.removePropertyChangeListener(l);
    }

    @LookupProvider.Registration(projectType = {"org-thespheres-betula-project-local"})
    public static class UnitLkpRegistration implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup baseContext) {
            LocalFileProperties prop = baseContext.lookup(LocalFileProperties.class);
            BetulaProject prj = baseContext.lookup(BetulaProject.class);
            if (prop.getProperty("providerURL") == null) {
                String id = prop.getProperty("unit.id");
                String authority = prop.getProperty("unit.authority", "local");
                String display = prop.getProperty("unit.displayName", prj.getProjectDirectory().getName());
                if (id != null && !StringUtils.isEmpty(id) && prj != null) {
                    UnitId uid = new UnitId(authority, id);
                    LocalUnit ret = new LocalUnit(uid, display, prj.getConfigurationsPath());
                    return Lookups.singleton(ret);
                }
            }
            return Lookup.EMPTY;
        }
    }
}
