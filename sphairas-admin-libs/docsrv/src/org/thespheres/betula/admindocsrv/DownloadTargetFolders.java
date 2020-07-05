/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admindocsrv;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.ui.util.FileChooserBuilderWithHint;

/**
 *
 * @author boris.heithecker
 */
@Messages({"download.target.folder.default=Zeugnisse und Zensurenlisten",
    "DownloadTargetFolders.FileChooser.Title=Datei",
    "DownloadTargetFolders.action.overwriteFile.title=Bestätigen",
    "DownloadTargetFolders.action.overwriteFile.text=Existierende Datei {0} überschreiben?"})
@ServiceProvider(service = DownloadTargetFolders.class)
public class DownloadTargetFolders {

    public static String PREF_DOWNLOAD_TARGET_FOLER = "download.target.folder";
    public static String PREF_USE_7Z = "archives.use.7z";
    public static String BACKUP_FOLER = ".backup";
    public static String FILES_FILE = ".files";
    private static JAXBContext jaxb;
    private final ChangeSupport cSupport = new ChangeSupport(this);
    private final Listener listener;
    private Path archives;
    private DownloadTargetMap map;
    private final RequestProcessor rp = new RequestProcessor(DownloadTargetFolders.class);
    static final int WAIT_TIME = 2000;
    private final RequestProcessor.Task writeMap;
    private DownloadTargetWatch watcher;

    public DownloadTargetFolders() {
        listener = new Listener();
        writeMap = rp.create(this::writeMap);
        try {
            initialize();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        NbPreferences.forModule(DownloadTargetFolders.class).addPreferenceChangeListener(listener);
    }

    public static DownloadTargetFolders getDefault() {
        return Lookup.getDefault().lookup(DownloadTargetFolders.class);
    }

    JAXBContext getJAXB() {
        if (jaxb == null) {
            try {
                jaxb = JAXBContext.newInstance(DownloadTargetMap.class);
            } catch (JAXBException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return jaxb;
    }

    private synchronized void initialize() throws IOException {
        archives = getArchivesDir();
        final Path mapPath = archives.resolve(FILES_FILE);
        final JAXBContext ctx = getJAXB();
        if (Files.exists(mapPath)) {
            try {
                map = (DownloadTargetMap) ctx.createUnmarshaller().unmarshal(mapPath.toFile());
            } catch (JAXBException ex) {
                throw new IOException(ex);
            }
        } else {
            map = new DownloadTargetMap();
        }
        if (watcher != null) {
            watcher.cleanup();
        }
        watcher = new DownloadTargetWatch(archives, this);
    }

    static Path getArchivesDir() throws IOException {
        final String pref = NbPreferences.forModule(DownloadTargetFolders.class).get(PREF_DOWNLOAD_TARGET_FOLER, null);
        final Path ret;
        if (pref == null) {
            ret = Paths.get(System.getProperty("user.home"), NbBundle.getMessage(DownloadTargetFolders.class, "download.target.folder.default"));
        } else {
            ret = Paths.get(pref);
        }
        if (!Files.isDirectory(ret)) {
            Files.createDirectories(ret);
        }
        return ret;
    }

    public List<Path> forOwner(final String owner) throws IOException {
        final List<String> files = map.forOwner(owner);
        return files.stream()
                .map(archives::resolve)
                .collect(Collectors.toList());
    }

    public synchronized void copy(final Path source, String fileName, final String owner) throws IOException {
        Path file = archives.resolve(fileName);
        final DownloadTargetMap.Item fi = map.find(fileName);
        if (Files.exists(file)) {
            final FileTime ft = Files.getLastModifiedTime(file);
            final LocalDateTime lfdt = LocalDateTime.ofInstant(ft.toInstant(), ZoneId.systemDefault());
            if (fi != null && lfdt.equals(fi.getTime())) {
                final Path backup = archives.resolve(BACKUP_FOLER);
                if (!Files.isDirectory(backup)) {
                    Files.createDirectories(backup);
                }
//                final DosFileAttributeView attrs = Files.getFileAttributeView(backup, DosFileAttributeView.class);
//                if (attrs != null) {
//                    attrs.setHidden(true);
//                }
                final Path backupFile = backup.resolve(fileName + ".bak");
                Files.copy(file, backupFile, StandardCopyOption.REPLACE_EXISTING);
            } else {
                final Path userfile = userFilename(fileName);
                if (userfile != null) {
                    file = userfile;
                    fileName = userfile.getName(userfile.getNameCount() - 1).toString();
                } else {
                    return;
                }
            }
        }

        final Path target = Files.copy(source, file, StandardCopyOption.REPLACE_EXISTING);
        final LocalDateTime lfdt = LocalDateTime.ofInstant(Files.getLastModifiedTime(target).toInstant(), ZoneId.systemDefault());

        final boolean changed = map.set(fileName, owner, lfdt);

        if (changed) {
            writeMap.schedule(WAIT_TIME);
            cSupport.fireChange();
        }
    }

    private Path userFilename(final String filename) {
        final String ending = filename.substring(filename.lastIndexOf('.'));
        final File home = new File(System.getProperty("user.home"));
        final String title = NbBundle.getMessage(DownloadTargetFolders.class, "DownloadTargetFolders.FileChooser.Title");
        final FileChooserBuilder fcb = new FileChooserBuilderWithHint(DownloadTargetFolders.class, filename);
        fcb.setTitle(title).setDefaultWorkingDirectory(home).setFileHiding(true);
        File ret = fcb.showSaveDialog();
        if (ret != null && !ret.getName().endsWith(ending)) {
            ret = new File(ret.getParentFile(), ret.getName() + ending);
        }
        if (ret != null && ret.exists()) {
            final String dt = NbBundle.getMessage(DownloadTargetFolders.class, "DownloadTargetFolders.action.overwriteFile.title");
            final String text = NbBundle.getMessage(DownloadTargetFolders.class, "DownloadTargetFolders.action.overwriteFile.text", ret.toString());
            final DialogDescriptor dd = new DialogDescriptor(text, dt);
            final Object res = DialogDisplayer.getDefault().notify(dd);
            if (!res.equals(DialogDescriptor.OK_OPTION)) {
                return null;
            }
        }
        return ret != null ? ret.toPath() : null;
    }

    private synchronized void writeMap() {
        final JAXBContext ctx = getJAXB();
        final Path mapPath = archives.resolve(FILES_FILE);
//        final DosFileAttributeView attrs = Files.getFileAttributeView(mapPath, DosFileAttributeView.class);
//        if (attrs != null) {
//            try {
//                attrs.setHidden(false);
//            } catch (IOException ex) {
//                throw new IllegalStateException(ex);
//            }
//        }
        try {
            final Marshaller m = ctx.createMarshaller();
            m.setProperty("jaxb.formatted.output", Boolean.TRUE);
            m.marshal(map, mapPath.toFile());
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
//        if (attrs != null) {
//            try {
//                attrs.setHidden(true);
//            } catch (IOException ex) {
//                throw new IllegalStateException(ex);
//            }
//        }
    }

    void removed(final String file) {
        rp.post(() -> {
            final boolean changed = map.remove(file);
            if (changed) {
                writeMap.schedule(WAIT_TIME);
                cSupport.fireChange();
            }
        });
    }

    public void addChangeListener(ChangeListener l) {
        cSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        cSupport.removeChangeListener(l);
    }

    private class Listener implements PreferenceChangeListener {

        @Override
        public void preferenceChange(final PreferenceChangeEvent evt) {
            evt.getKey();
            try {
                initialize();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

}
