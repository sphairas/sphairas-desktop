/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.configui;

import java.io.IOException;
import java.net.URI;
import java.text.Collator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;
import javax.swing.Action;
import org.openide.awt.Actions;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.adminconfig.AppResourcesProperties;
import org.thespheres.betula.admin.database.configui.AppResourcesFileChildren.Entry;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.client.jms.JMSListener;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.services.dav.DAVProp;
import org.thespheres.betula.services.dav.DisplayName;
import org.thespheres.betula.services.dav.GetLastModified;
import org.thespheres.betula.services.dav.Multistatus;
import org.thespheres.betula.services.dav.PropStat;
import org.thespheres.betula.services.dav.ResourceType;
import org.thespheres.betula.services.dav.Response;
import org.thespheres.betula.services.jms.AppResourceEvent;
import org.thespheres.betula.services.jms.JMSTopic;
import org.thespheres.betula.services.ui.ConfigurationException;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris
 */
public class AppResourcesFileChildren extends ChildFactory<Entry> {

    static Collator COLLATOR = Collator.getInstance(Locale.getDefault());
    protected final Entry dir;

    AppResourcesFileChildren(final Entry dir) {
        this.dir = dir;
    }

    public static AppResourcesFileChildren createRoot(final String provider) {
        class Root extends AppResourcesFileChildren implements Runnable, JMSListener<AppResourceEvent> {

            final RequestProcessor RP = new RequestProcessor(AppResourcesProperties.class.getCanonicalName(), 1, true);
            private final RequestProcessor.Task loadTask;

            @SuppressWarnings({"LeakingThisInConstructor"})
            Root() {
                super(new Entry("", null, true, provider));
                loadTask = RP.create(this);
                loadTask.schedule(0);
            }

            @Override
            public void run() {
                try {
                    final String davBase = URLs.adminResourcesDavBase(LocalProperties.find(provider));
                    final WebProvider service = WebProvider.find(provider, WebProvider.class);
                    final URI uri = URI.create(davBase);
                    readDirectories(service, uri, dir);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    refresh(false);
                }
            }

            @Override
            public void addNotify() {
            }

            @Override
            public void removeNotify() {
            }

            @Override
            public void onMessage(final AppResourceEvent event) {
                loadTask.schedule(1500);
            }

        }
        final Root root = new Root();
        final JMSTopicListenerService jms = JMSTopicListenerService.find(provider, JMSTopic.APP_RESOURCES_TOPIC.getJmsResource());
        if (jms != null) {
            jms.registerListener(AppResourceEvent.class, root);
        }
        return root;
    }

    static Map<String, String> readDirectories(final WebProvider wp, final URI uri, final Entry root) throws IOException {
        try {
            final Multistatus ms = HttpUtilities.getProperties(wp, uri, -1, true);
            final Map<String, String> ret = new HashMap<>();
            synchronized (root) {
                root.folderChildren.clear();
                root.fileChildren.clear();
                parseMultistatus(ms, root);
            }
            return ret;
        } catch (NoProviderException | ConfigurationException ex) {
            throw new IOException(ex);
        }
    }

    private static void parseMultistatus(final Multistatus ms, final Entry root) throws IOException {
        responses:
        for (final Response r : ms.getResponses()) {
            final String href = r.getHref().stream()
                    .collect(CollectionUtil.singleOrNull());
            if (href == null) {
                continue;
            }
            final String path = href.substring("/web/dav/".length());
            for (final PropStat ps : r.getPropstat()) {
                if (ps.getStatusCode() == 200) {
                    final DAVProp prop = (DAVProp) ps.getProp();
                    final boolean folder = Optional.ofNullable(prop.getResourcetype())
                            .map(ResourceType::getCollection)
                            .isPresent();
                    final String name = Optional.ofNullable(prop.getDisplayName())
                            .map(DisplayName::getValue)
                            .orElse(null);
                    if (name != null) {
                        final Entry dir;
                        final int li = path.lastIndexOf('/');
                        if (li == -1) {
                            dir = root;
                        } else {
                            Entry ce = root;
                            int i = -1;
                            while ((i = path.indexOf('/', i + 1)) != -1) {
                                final String sp = path.substring(0, i + 1);
                                final Entry found = ce.folderChildren.stream()
                                        .filter(fc -> fc.getResourcePath().equals(sp))
                                        .collect(CollectionUtil.requireSingleOrNull());
                                if (found != null) {
                                    ce = found;
                                } else {
                                    ce = ce.addFolderEntry(sp);
                                }
                            }
                            dir = ce;
                        }
                        if (dir == null) {
                            throw new IOException();
                        }
                        if (folder) {
                            dir.setName(name);
                            continue responses;
                        } else {
                            final String lm = Optional.ofNullable(prop.getGetLastModified())
                                    .map(GetLastModified::getValue)
                                    .orElse(null);
                            dir.addFileEntry(path, name, lm);
                            continue responses;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected boolean createKeys(final List<Entry> list) {
        synchronized (dir) {
            dir.folderChildren.forEach(list::add);
            dir.fileChildren.forEach(list::add);
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(final Entry key) {
        if (key.isFolder()) {
            return new FolderNode(key);
        } else {
            return new FileNode(key);
        }
    }

    static class FolderNode extends AbstractNode {

        public FolderNode(final Entry e) {
            super(Children.create(new AppResourcesFileChildren(e), true), Lookups.fixed(e));
            setName(e.getName());
            setIconBaseWithExtension("org/thespheres/betula/admin/database/resources/blue-folder-horizontal.png");
        }

        @Override
        public Action[] getActions(boolean context) {
            final Action ua = Actions.forID("Tools", "org.thespheres.betula.admin.database.configui.UploadAction");
            return new Action[]{ua};
        }

    }

    static class FileNode extends AbstractNode {

        private final Entry entry;

        FileNode(final Entry e) {
            super(Children.LEAF, Lookups.fixed(e));
            this.entry = e;
            setName(e.getName());
            setIconBaseWithExtension("org/thespheres/betula/admin/database/resources/document-import.png");
        }

        @Override
        public Action getPreferredAction() {
            return Actions.forID("Tools", "org.thespheres.betula.admin.database.configui.DownloadAction");
        }

        @Override
        public String getHtmlDisplayName() {
            final StringJoiner sj = new StringJoiner(" ", "<html>", "</html>");
            sj.add(getName());
            if (entry.getLastModified() != null) {
                sj.add("<font color='D3D3D3'><i>[" + entry.getLastModified() + "]</i></font>");
            }
            return sj.toString();
        }
    }

    protected static class Entry implements Comparable<Entry> {

        final String resourcePath;
        final String provider;
        String name;
        final boolean folder;
        String lastModified;
        final SortedSet<Entry> folderChildren = new TreeSet<>();
        final SortedSet<Entry> fileChildren = new TreeSet<>();

        Entry(final String path, final String name, final boolean folder, final String provider) {
            this.resourcePath = path;
            this.name = name;
            this.folder = folder;
            this.provider = provider;
        }

        String getResourcePath() {
            return resourcePath;
        }

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        boolean isFolder() {
            return folder;
        }

        String getProvider() {
            return provider;
        }

        String getLastModified() {
            return lastModified;
        }

        void setLastModified(String lastModified) {
            this.lastModified = lastModified;
        }

        void addFileEntry(final String path, final String name, final String lastModified) {
            final Entry entry = new Entry(path, name, false, provider);
            entry.setLastModified(lastModified);
            fileChildren.add(entry);
        }

        Entry addFolderEntry(final String path) {
            final Entry entry = new Entry(path, "", true, provider);
            folderChildren.add(entry);
            return entry;
        }

        @Override
        public int compareTo(final Entry other) {
            return COLLATOR.compare(name, other.name);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + Objects.hashCode(this.resourcePath);
            hash = 79 * hash + (this.folder ? 1 : 0);
            return 79 * hash + Objects.hashCode(this.lastModified);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Entry other = (Entry) obj;
            if (this.folder != other.folder) {
                return false;
            }
            if (!Objects.equals(this.resourcePath, other.resourcePath)) {
                return false;
            }
            return Objects.equals(this.lastModified, other.lastModified);
        }

    }

}
