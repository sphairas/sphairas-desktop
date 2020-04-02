/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.ws;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.dom.DOMResult;
import org.netbeans.api.project.Project;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.xml.XMLUtil;
import org.thespheres.betula.Identity;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Students;
import org.thespheres.betula.Unit;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.UnitEntry;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.project.BetulaProject;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.ui.BackupUtil;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.services.util.UnitInfo;
import org.thespheres.betula.services.util.UnitTarget;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.util.XmlStudents;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"UnitImp2.error.title=Verbindungs-Fehler",
    "UnitImp2.error.message=Die Gruppe „{0}“ konnte nicht vom Datenanbieter „{1}“ geladen werden.",
    "UnitImp2.message.noBackupEmptyStudents=Eine leere Gruppe wird nicht im Backup gespeichert.",
    "UnitImp2.message.backupDirNotWritable=Das Backup-Verzeichnis {0} kann nicht geschrieben werden"})
class UnitImp2 implements Unit, ChangeListener {

    private final UnitId id;
    private String displayName = null;
    private final Lookup context;
    private final boolean resolveName;
    private Students students;
    private static final RequestProcessor RP = new RequestProcessor(UnitImp2.class);
    public static final String STUDENTS_FILE = "students.xml";
//    public static final String STUDENTS_BACKUP_PATH = BetulaProject.BACKUP_PATH + "/" + STUDENTS_FILE + ".xml";
    private static JAXBContext studsctx;
    private final Task initTask;
    private NamingResolver namingProvider;
    private TermSchedule termSchem;
    private final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    private static final String[] PRIMPATH = new String[]{"primary-units", "participants"};
    private final String studentsUrl;
    private boolean warned;
    private DocumentId targetBase;

    static {
        try {
            studsctx = JAXBContext.newInstance(XmlStudents.class);
        } catch (JAXBException ex) {
            Logger.getLogger(UnitImp2.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
    }

    UnitImp2(UnitId id, Lookup context) {
        this.id = id;
        this.context = context;
        final LocalFileProperties prop = context.lookup(LocalFileProperties.class);
        resolveName = Boolean.valueOf(prop.getProperty("resolveName", Boolean.TRUE.toString()));
        studentsUrl = URLs.students(prop);
        initTask = RP.post(this::loadInitial);
    }

    @Override
    public UnitId getUnitId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        if (displayName == null) {
            if (resolveName) {
                Lookup.getDefault().lookup(WorkingDate.class).addChangeListener(this);
                initName();
            } else {
                displayName = context.lookup(LocalFileProperties.class).getName();
            }
        }
        return displayName;
    }

    private void setDisplayName(String dName) {
        String old = displayName;
        if (!dName.equals(old)) {
            displayName = dName;
            pSupport.firePropertyChange(Unit.PROP_DISPLAYNAME, old, dName);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        String dName = null;
        Date workingDate = Lookup.getDefault().lookup(WorkingDate.class).getCurrentWorkingDate();
        if (termSchem != null && namingProvider != null) {
            Term currentTerm = termSchem.getTerm(workingDate);
            Identity i = targetBase != null ? targetBase : id;
            try {
                dName = namingProvider.resolveDisplayNameResult(i).getResolvedName(currentTerm);
            } catch (IllegalAuthorityException ex) {
            }
        }
        if (dName == null) {
            dName = id.getId();
        }
        setDisplayName(dName);
    }

    public void initName() {
        final LocalFileProperties prop = context.lookup(LocalFileProperties.class);
        final String termProvider = prop.getProperty("termSchedule.providerURL");
        if (termProvider != null) {
            final SchemeProvider p = SchemeProvider.find(termProvider);
            termSchem = p.getScheme(prop.getProperty("termSchemeId", TermSchedule.DEFAULT_SCHEME), TermSchedule.class);
        }
        final String provider = prop.getProperty("providerURL");
        if (provider != null) {
            final String np = prop.getProperty("naming.providerURL", provider);
            namingProvider = NamingResolver.find(np);
        }
        targetBase = UnitTarget.parseTargetBase(prop);
        stateChanged(null);
    }

    @Override
    public Set<Student> getStudents() {
        return Collections.unmodifiableSet(students().getStudents());
    }

    @Override
    public Student findStudent(StudentId studId) {
        return students().find(studId);
    }

    private Students students() {
        if (!initTask.isFinished()) {
            if (EventQueue.isDispatchThread()) {
                Logger.getLogger(UnitImp2.class.getName()).log(Level.WARNING, "UnitImp2.students() should not be called from AWT while students are still being loaded.");
            }
            initTask.waitFinished();
        }
        if (students == null) {
            if (!warned) {
                Logger.getLogger(UnitImp2.class.getName()).log(Level.INFO, "No students available in unit {0}. Returning empty set.", getUnitId().getId());
                warned = true;
            }
            return Students.EMPTY;
        }
        return students;
    }

    private Students fetch(String p) throws IOException {
        if (p != null) {
            Units units = Units.get(p).orElseThrow(IOException::new);
            UnitStudents ret = new UnitStudents(units.getWebServiceProvider(), studentsUrl);
            PULoaderImpl l = null;
            try {
                l = units.fetchParticipants(id, null, () -> new PULoaderImpl(ret));
            } catch (IllegalArgumentException ilex) {
                //unit not listed
                throw new IOException(ilex);
            }
            l.us.initialize();
            return l.us;
        }
        throw new IOException();
    }

    private String getProviderUrl() {
        Project prj = context.lookup(Project.class);
        if (prj != null) {
            LocalFileProperties prop = prj.getLookup().lookup(LocalFileProperties.class);
            if (prop != null) {
                return prop.getProperty("providerURL");
            }
        }
        return null;
    }

    private void loadInitial() {
        Students studs = null;
        boolean save = false;
        try {
            studs = fetch(getProviderUrl());
            save = true;
        } catch (IOException ex) {
            Logger.getLogger(UnitImp2.class.getName()).log(LogLevel.INFO_WARNING, ex.getMessage());
            Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
            String title = NbBundle.getMessage(UnitImp2.class, "UnitImp2.error.title");
            String name = getProviderUrl();
            ProviderInfo pi = ProviderRegistry.getDefault().get(name);
            if (pi != null) {
                name = pi.getDisplayName();
            }
            String message = NbBundle.getMessage(UnitImp2.class, "UnitImp2.error.message", getDisplayName(), name);
            NotificationDisplayer.getDefault()
                    .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
            try {
                studs = loadBackup();
            } catch (IOException ex1) {
                Logger.getLogger(UnitImp2.class.getName()).log(LogLevel.INFO_WARNING, ex1.getMessage());
            }
        }
        if (studs != null) {
            students = studs;
            if (save) {
                RP.post(this::saveStuds);
            }
        }
    }

    private XmlStudents loadBackup() throws IOException {
        BetulaProject betula = context.lookup(BetulaProject.class);
        if (betula != null) {
            FileObject fo = FileUtil.toFileObject(betula.getBackupDir().toFile());
            if (fo != null) {
                fo = fo.getFileObject(STUDENTS_FILE);
                if (fo != null) {
                    InputSource is = new InputSource(fo.getInputStream());
                    try {
                        Document document = XMLUtil.parse(is, true, true, null, null);
                        BackupUtil.cypherXMLDocument(document, false);
                        return (XmlStudents) studsctx.createUnmarshaller().unmarshal(document);
                    } catch (JAXBException | SAXException ex) {
                        throw new IOException(ex);
                    } finally {
                        is.getByteStream().close();
                    }
                }
            }
        }
        return null;
    }

    private void saveStuds() {
        if (students.getStudents().isEmpty()) {
            String msg = NbBundle.getMessage(UnitImp2.class, "UnitImp2.message.noBackupEmptyStudents");
            Logger.getLogger(UnitImp2.class.getName()).log(Level.INFO, msg);
            return;
        }
        BetulaProject project = context.lookup(BetulaProject.class);
        if (project != null) {
            try {
                FileObject backup = FileUtil.toFileObject(project.getBackupDir().toFile());
                if (!backup.canWrite()) {
                    String msg = NbBundle.getMessage(UnitImp2.class, "UnitImp2.message.backupDirNotWritable", backup.getPath());
                    Logger.getLogger(UnitImp2.class.getName()).log(Level.INFO, msg);
                    return;
                }
                FileObject fo = FileUtil.createData(backup, STUDENTS_FILE);
                FileLock lock = fo.lock();
                try (OutputStream os = fo.getOutputStream(lock)) {
                    XmlStudents xmlStuds = new XmlStudents(students.getStudents());
                    DOMResult result = new DOMResult();
                    studsctx.createMarshaller().marshal(xmlStuds, result);
                    final Document document = (Document) result.getNode();
                    BackupUtil.cypherXMLDocument(document, true);
                    XMLUtil.write(document, os, "utf-8");
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                } finally {
                    lock.releaseLock();
                }
            } catch (IOException ex) {
                Logger.getLogger(UnitImp2.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
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

    private class PULoaderImpl extends UnitInfo {

        final UnitStudents us;

        private PULoaderImpl(UnitStudents studs) {
            us = studs;
        }

        @Override
        protected void beforeSolicit(UnitEntry uentry, UnitId unit) {
            super.beforeSolicit(uentry, unit);
            uentry.getHints().put("units.add-students-primary-units", Boolean.toString(true));
        }

        @Override
        protected void extractStudentIdsFormResponseUnitEntry() throws IOException {
            super.extractStudentIdsFormResponseUnitEntry();

            Arrays.stream(getStudents())
                    .forEach(us::addStudent);

        }

        @Override
        protected void extractResponseUnitEntry(final Container response) {
            super.extractResponseUnitEntry(response);

            final List<Envelope> primNode = DocumentUtilities.findEnvelope(response, PRIMPATH);
            primNode.stream()
                    .filter(Entry.class::isInstance)
                    .map(Entry.class::cast)
                    .filter(e -> e.getIdentity() instanceof UnitId)
                    .forEach(t -> {
                        t.getChildren().stream()
                                .filter(te -> te.getAction() != null && te.getAction().equals(Action.RETURN_COMPLETION))
                                .forEach(te -> addPrimaryUnitToStudent(t, te));
                    });
        }

        private void addPrimaryUnitToStudent(Entry<UnitId, ?> unitEntry, Template<?> node) {
            if (unitEntry.getIdentity() instanceof UnitId) {
                UnitId unit = unitEntry.getIdentity();
                node.getChildren().stream()
                        .filter(Entry.class::isInstance)
                        .map(Entry.class::cast)
                        .filter(e -> e.getIdentity() instanceof StudentId)
                        .map(s -> (StudentId) s.getIdentity())
                        .map(s -> us.find(s))
                        .filter(Objects::nonNull)
                        .forEach(ustud -> ustud.setPrimaryUnit(unit));
            }
        }
    }
}
