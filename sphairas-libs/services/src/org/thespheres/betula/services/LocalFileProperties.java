/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.MarkerFactory;

/**
 *
 * @author boris.heithecker
 */
public class LocalFileProperties implements CommonTargetProperties, CommonStudentProperties, LocalProperties {

    public static final String USER_PROPERTIES_FILE = "user.properties";
    public static final String PROP_UNIQUE_SUBJECT_CONVENTIONS = "unique.subject.conventions";
    public static final String PROP_UNIQUE_REALM_CONVENTIONS = "unique.realm.conventions";
    public static final String PROP_REALM_CONVENTIONS = "realm.conventions";
    public static final String PROP_ASSESSMENT_CONVENTIONS = "assessment.conventions";
    public static final String PROP_STUDENT_CAREER_CONVENTIONS = "student.career.conventions";
    private final String name;
    private final PropertiesExt internal = new PropertiesExt();
    private final PropertiesExt overrides = new PropertiesExt(internal);
    protected final ChangeSupport cSupport = new ChangeSupport(this);
    private final RequestProcessor WATCH = new RequestProcessor();
    private boolean initialized = false;
    private static boolean[] INIT = new boolean[]{false};

    private LocalFileProperties(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("LocalFileProperties.name cannot be null.");
        }
        this.name = name;
    }

    public LocalFileProperties(String name, InputStream is) throws IOException {
        this(name);
        load(is);
    }

    public LocalFileProperties(String name, String resource) throws IOException {
        this(name);
        try {
            try (final InputStream is = getClass().getResourceAsStream(resource)) {
                load(is);
            } catch (MissingResourceException e) {
                String msg = resource + " not found for class " + getClass().getName();
                throw new IOException(msg, e);
            }
        } catch (IOException io) {
            Logger.getLogger("DEBUG").log(Level.INFO, "Problem loading " + name, io);
            throw io;
        }
    }

    public static LocalFileProperties find(ProviderInfo provider) {
        return LocalFileProperties.find(Lookup.getDefault(), provider.getURL());
    }

    public static LocalFileProperties find(String name) {
        return LocalFileProperties.find(Lookup.getDefault(), name);
    }

    public static LocalFileProperties find(Lookup from, String name) {
        return LocalProperties.find(from, name, LocalFileProperties.class, false);
    }

    public static LocalFileProperties create(final Map<String, ?> attr) throws IOException {
        final String name = (String) attr.get("name");
        final String res = (String) attr.get("resource");
        final InputStream is = Lookup.getDefault().lookup(ClassLoader.class).getResourceAsStream(res);
        return new LocalFileProperties(name, is);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getProperty(final String name) {
        ensureInitialized();
        return getPropertyInsecure(name);
    }

    protected String getPropertyInsecure(final String name) {
        return overrides.getProperty(name);
    }

    @Override
    public Map<String, String> getProperties() {
        ensureInitialized();
        return overrides.stringPropertyNames().stream()
                .collect(Collectors.toMap(n -> n, n -> getProperty(n)));
    }

    protected final void load(final InputStream is) throws IOException {
        internal.load(is);
//        is.close();
    }

    protected boolean isInitialized() {
        return initialized;
    }

    //Make sure properties are never called from the constructor
    protected void ensureInitialized() {
        synchronized (this) {
            if (!initialized) {
                internal.initDefaults();
                initLoadOverrides();
                initialized = true;
            }
        }
    }

    protected void initLoadOverrides() {
        final Path dir = getOverridesDir();
        if (dir != null) {
            loadOverrides(dir.resolve(USER_PROPERTIES_FILE));
            postDirWatch(dir);
            if (isInitialized()) {
                cSupport.fireChange();
            }
        }
    }

    protected void postDirWatch(final Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException ex) {
            Logger.getLogger(LocalFileProperties.class.getName()).log(Level.SEVERE, "Could not create provider dir " + dir.toString(), ex);
            return;
        }
        WATCH.post(new FileWatch(dir), 0, 1);
    }

    protected void loadOverrides(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        try (final InputStream ois = Files.newInputStream(path, StandardOpenOption.READ)) {
            overrides.load(ois);
        } catch (IOException ioex) {
        }
    }

    protected Path getOverridesDir() {
        //This method is called from ensure initialized
        final String sysPropRoot = getPropertyInsecure("user.properties.root");
        if (sysPropRoot != null) {
            try {
                return Paths.get(sysPropRoot);
            } catch (Exception e) {
                Logger.getLogger(LocalFileProperties.class.getCanonicalName()).log(Level.SEVERE, "An error has occurred accessing " + sysPropRoot, e);
            }
        }
        final String providerUrl = getPropertyInsecure(AppPropertyNames.LP_PROVIDER);
        return Optional.ofNullable(providerUrl)
                .map(ServiceConstants::providerConfigBase)
                .orElse(null);
    }

    @Override
    public AssessmentConvention[] getAssessmentConventions() {
        final String[] arr = getProperty(PROP_ASSESSMENT_CONVENTIONS, "").split(",");
        return Arrays.stream(arr)
                .filter(s -> !s.trim().isEmpty())
                .map(GradeFactory::findConvention)
                .filter(Objects::nonNull)
                .toArray(AssessmentConvention[]::new);
    }

    @Override
    public MarkerConvention[] getRealmMarkerConventions() {
        final String[] arr = getProperty(PROP_REALM_CONVENTIONS, "").split(",");
        return Arrays.stream(arr)
                .filter(s -> !s.trim().isEmpty())
                .map(MarkerFactory::findConvention)
                .filter(Objects::nonNull)
                .toArray(MarkerConvention[]::new);
    }

    @Override
    public MarkerConvention[] getSubjectMarkerConventions() {
        final String[] arr = getProperty(PROP_UNIQUE_SUBJECT_CONVENTIONS, "").split(",");
        return Arrays.stream(arr)
                .filter(s -> !s.trim().isEmpty())
                .map(MarkerFactory::findConvention)
                .filter(Objects::nonNull)
                .toArray(MarkerConvention[]::new);
    }

    @Override
    public MarkerConvention[] getStudentCareerConventions() {
        final String[] arr = getProperty(PROP_STUDENT_CAREER_CONVENTIONS, "").split(",");
        return Arrays.stream(arr)
                .filter(s -> !s.trim().isEmpty())
                .map(MarkerFactory::findConvention)
                .filter(Objects::nonNull)
                .toArray(MarkerConvention[]::new);
    }

    public void addChangeListener(ChangeListener l) {
        synchronized (cSupport) {
            cSupport.addChangeListener(l);
        }
    }

    public void removeChangeListener(ChangeListener l) {
        synchronized (cSupport) {
            cSupport.removeChangeListener(l);
        }
    }

    private PropertiesExt overrides() {
        ensureInitialized();
        return overrides;
    }

    protected final void setParent(LocalFileProperties parent) {
        internal.setDefaults(parent.overrides);
    }

    private final class FileWatch implements Runnable {

        private Path dirPath;

        private FileWatch(Path path) {
            this.dirPath = path;
        }

        @Override
        public void run() {
            if (!Files.exists(dirPath)) {
                return;
            }
            FileSystem fs = dirPath.getFileSystem();
            WatchService ws;
            try {
                ws = fs.newWatchService();
                dirPath.register(ws, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            } catch (IOException ex) {
                Logger.getLogger(LocalFileProperties.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                return;
            }
            watch:
            while (true) {
                WatchKey key;
                try {
                    // wait for a key to be available
                    key = ws.take();
                } catch (InterruptedException ex) {
                    return;
                }
                for (final WatchEvent<?> event : key.pollEvents()) {
                    //we only register "ENTRY_MODIFY" so the context is always a Path.
                    final Path changed = (Path) event.context();
                    if (changed.getNameCount() == 1 && changed.endsWith(USER_PROPERTIES_FILE)) {
                        break watch;
                    }
                }
                // IMPORTANT: The key must be reset after processed
                boolean valid = key.reset();
                if (!valid) {
                    Logger.getLogger(LocalFileProperties.class.getName()).log(Level.FINE, "Invalid WatchKey.");
                }
            }
            try {
                if (ws != null) {
                    ws.close();
                }
            } catch (IOException ex) {
            }
            initLoadOverrides();
        }
    }

    private final class PropertiesExt extends Properties {

        private PropertiesExt() {
//            initDefaults();
        }

        private PropertiesExt(Properties defaults) {
            super(defaults);
        }

        @Override
        public void load(final InputStream in) throws IOException {
            synchronized (this) {
                super.load(in);
                in.close();
            }
        }

        private void setDefaults(Properties overrides) {
            synchronized (this) {
                if (overrides == null) {
                    defaults = null; //Reset always
                } else if (defaults == null) { //set defaults only if 1. not set yet, or 2. reset before. Reason: allow set defaults in constrctor setParent()
                    defaults = overrides;
                }
            }
        }

        protected void initDefaults() {
            final String p = getProperty(AppPropertyNames.LP_PROVIDER);
            final String sp = getProperty(AppPropertyNames.LP_SUPER_PROVIDER, p);
            if (sp != null && !LocalFileProperties.this.getName().equals(sp)) {
                synchronized (INIT) {
                    if (!INIT[0]) {
                        try {
//                            final LocalFileProperties found = Lookup.getDefault().lookupAll(LocalFileProperties.class).stream()
//                                    .filter(lfp -> lfp.getName().equals("www.kgs-tarstedt.de/admin"))
//                                    .collect(CollectionUtil.requireSingleOrNull());
                            Thread.sleep(5000);
                            INIT[0] = true;
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                synchronized (this) {
                    final LocalFileProperties parent = LocalProperties.find(Lookup.getDefault(), sp, LocalFileProperties.class, false);
                    if (parent != null) {
                        setDefaults(parent.overrides());
                    }
                }
            }
        }

    }

}
