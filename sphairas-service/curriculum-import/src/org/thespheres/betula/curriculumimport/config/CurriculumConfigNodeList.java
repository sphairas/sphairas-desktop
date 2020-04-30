/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.config;

import com.google.common.eventbus.Subscribe;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.thespheres.betula.adminconfig.ConfigNodeTopComponentNodeList;
import org.thespheres.betula.adminconfig.ConfigurationEventBus;
import org.thespheres.betula.adminconfig.ProviderSyncEvent;
import org.thespheres.betula.services.ServiceConstants;
import org.thespheres.betula.services.implementation.ui.ProviderFileListName;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
@ProviderFileListName(CurriculumConfigNodeList.PROVIDER_FILE_LIST_NAME)
public class CurriculumConfigNodeList extends ConfigNodeTopComponentNodeList<DataObject> implements PropertyChangeListener, Runnable {

    static final int DELAY = 2000;
    public static final String PROVIDER_FILE_LIST_NAME = "curriculum-files";
    public static final String NAME = "curriculum";
    public static final String BEMERKUNGEN_CONFIG_NODE_POSITION_KEY = "CurriculumConfigNode.position";
    static final Map<String, CurriculumConfigNodeList> NODE_LISTS = new HashMap<>();
    private final Map<DataObject, Node> nodes = new HashMap<>();
    private final List<DataObject> files = new ArrayList<>();
    private final RequestProcessor RP = new RequestProcessor(CurriculumConfigNodeList.class.getName());
    private final RequestProcessor.Task task;

    @SuppressWarnings({"LeakingThisInConstructor"})
    private CurriculumConfigNodeList(String provider) {
        super(provider, NbPreferences.forModule(CurriculumConfigNodeList.class).getInt(BEMERKUNGEN_CONFIG_NODE_POSITION_KEY, 1500));
        task = RP.create(this);
        task.schedule(0);
        Optional.ofNullable(ConfigurationEventBus.find(provider))
                .ifPresent(b -> b.register(this));
    }

    public static CurriculumConfigNodeList find(String provider) {
        return NODE_LISTS.computeIfAbsent(provider, p -> new CurriculumConfigNodeList(p));
    }

    @Override
    public Key<DataObject>[] getKeys() {
        final Key[] ret;
        synchronized (files) {
            ret = files.stream()
                    .map(Key::new)
                    .toArray(Key[]::new);
        }
        return ret;
    }

    @Override
    public Node[] getNodes(final Key<DataObject> key) {
        final Node ret;
        synchronized (nodes) {
            ret = nodes.computeIfAbsent(key.getKey(), this::createNode);
        }
        return new Node[]{ret};
    }

    @Override
    public void run() {
        try {
            runImpl();
        } catch (final IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(CurriculumConfigNodeList.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
        } finally {
            cSupport.fireChange();
        }
    }

    private void runImpl() throws IOException {
        final List<DataObject> fileList = new ArrayList<>();
        final Path base = ServiceConstants.providerConfigBase(provider);
        final Path list = base.resolve(CurriculumConfigNodeList.PROVIDER_FILE_LIST_NAME);
        final List<Path> lost = new ArrayList<>();
        if (Files.exists(list)) {
            final List<String> ff = Files.readAllLines(list, StandardCharsets.UTF_8);
            final List<FileObject> files = new ArrayList<>();
            for (final String l : ff) {
                if (!l.trim().startsWith("#")) {
                    final Path p = base.resolve(l);
                    if (Files.exists(p)) {
                        final FileObject fo = FileUtil.toFileObject(p.toFile());
                        if (fo != null) {
                            files.add(fo);
                            continue;
                        }
                    }
                    lost.add(p);
                }
            }
            for (final FileObject fo : files) {
                try {
                    final DataObject obj = DataObject.find(fo);
                    fileList.add(obj);
                } catch (DataObjectNotFoundException ex) {
                    PlatformUtil.getCodeNameBaseLogger(CurriculumConfigNodeList.class).log(Level.SEVERE, "Error", ex);
                }
            }
        }
        Collections.sort(fileList, Comparator.comparing(d -> d.getNodeDelegate().getDisplayName(), Collator.getInstance(Locale.getDefault())));
        synchronized (files) {
            files.clear();
            files.addAll(fileList);
        }
        if (!lost.isEmpty()) {
            task.schedule(DELAY);
        }
    }

    private Node createNode(final DataObject data) {
        CurriculumWriteLockSupport.create(data);
        final Node k = data.getNodeDelegate();
        final PropertyChangeListener l = WeakListeners.propertyChange(this, data);
        data.addPropertyChangeListener(l);
        return new CurriculumFilterNode(k);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (DataObject.PROP_VALID.equals(evt.getPropertyName())) {
            cSupport.fireChange();
        }
    }

    @Subscribe
    public void onConfigurationEvent(final ProviderSyncEvent event) {
        if (PROVIDER_FILE_LIST_NAME.equals(event.getResource())) {
            task.schedule(0);
        }
    }

    @ConfigNodeTopComponentNodeList.Factory.Registration
    public static class CurriculumConfigNodeFactory extends ConfigNodeTopComponentNodeList.Factory<CurriculumConfigNodeList> {

        public CurriculumConfigNodeFactory() {
            super(NAME);
        }

        @Override
        public CurriculumConfigNodeList create(String provider, Map props) {
            return CurriculumConfigNodeList.find(provider);
        }

    }
}
