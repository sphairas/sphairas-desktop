/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeListener;
import javax.xml.bind.Binder;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;
import org.thespheres.betula.adminconfig.ProviderReference;
import org.thespheres.betula.adminconfig.layerxml.AbstractLayerFile;
import org.thespheres.betula.adminconfig.layerxml.LayerFileSystem;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.client.jms.JMSListener;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.services.implementation.ui.build.LayerUpdater;
import org.thespheres.betula.services.implementation.ui.layerxml.LayerFileAttribute;
import org.thespheres.betula.services.implementation.ui.layerxml.LayerFileImpl;
import org.thespheres.betula.services.implementation.ui.layerxml.LayerFileSystemImpl;
import org.thespheres.betula.services.implementation.ui.layerxml.LayerFolderImpl;
import org.thespheres.betula.services.implementation.ui.layerxml.LayerUtils;
import org.thespheres.betula.services.jms.AppResourceEvent;
import org.thespheres.betula.services.jms.JMSTopic;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.ui.swingx.treetable.NbPluggableSwingXTreeTableModel;
import org.thespheres.betula.ui.util.PluggableTableColumn;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author boris.heithecker
 */
@Messages({"LayerPropertiesModel.hierarchicalColumnHeader=Einträge"})
class LayerPropertiesModel extends NbPluggableSwingXTreeTableModel<LayerFileSystem, AbstractLayerFile> implements Runnable {

    final RequestProcessor RP = new RequestProcessor(LayerPropertiesModel.class.getName());
    private final LayerFileChildren rootChildren;
    private final ChangeSupport cSupport = new ChangeSupport(this);
    private final Listener listener = new Listener();
    private boolean dirty = false;
    private String provider;
    private final RequestProcessor.Task task;
    private Document dom;
    private final Binder<org.w3c.dom.Node> binder;

    @SuppressWarnings({"LeakingThisInConstructor"})
    LayerPropertiesModel(final Node root, final LayerFileChildren children, final Set<? extends PluggableTableColumn<LayerFileSystem, AbstractLayerFile>> s) {
        super(LayerPropertiesModel.class.getName(), root, s);
        this.rootChildren = children;
        binder = LayerUtils.getJAXB().createBinder();
        this.task = RP.create(this);
    }

    static LayerPropertiesModel create() {
        final InstanceContent ic = new InstanceContent();
        final AbstractLookup lookup = new AbstractLookup(ic);
        final LayerFileChildren children = new LayerFileChildren();
        final Node root = new FolderNode(children, lookup, "");
        final HashSet<PluggableTableColumn<LayerFileSystem, AbstractLayerFile>> ret = new HashSet<>();
        ret.add(new AttrValueColumn());
        return new LayerPropertiesModel(root, children, ret);
    }

    void initialize(final ProviderReference env) {
        if (provider != null) {
            throw new IllegalStateException("");
        }
        provider = env.getProviderInfo().getURL();
        final JMSTopicListenerService jms = JMSTopicListenerService.find(provider, JMSTopic.APP_RESOURCES_TOPIC.getJmsResource());
        if (jms != null) {
            jms.registerListener(AppResourceEvent.class, listener);
        }
        task.schedule(0);
    }

    protected void addDefaultToolbarActions(final JToolBar toolbar) {
        final ImageIcon saveImage = ImageUtilities.loadImageIcon("org/thespheres/betula/services/implementation/ui/resources/disk.png", true);
        final JButton saveButton = new JButton(saveImage);
        saveButton.addActionListener(e -> {
            final LayerFileSystem p = getItemsModel();
            if (p != null) {
                save();
            }
        });
        toolbar.add(saveButton);
//        final ImageIcon addImage = ImageUtilities.loadImageIcon("org/thespheres/betula/services/implementation/ui/resources/plus-button.png", true);
//        final JButton addButton = new JButton(addImage);
//        addButton.addActionListener(e -> {
//            final AppResourcesProperties p = getItemsModel();
//            if (p != null) {
//                p.createTemplateProperty();
//            }
//        });
//        toolbar.add(addButton);
    }

    @Override
    public void run() {
        try {
            runImpl();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void runImpl() throws IOException {
        final String davBase = URLs.adminResourcesDavBase(LocalProperties.find(provider));
        final WebProvider service = WebProvider.find(provider, WebProvider.class);
        final URI uri = URI.create(davBase + LayerUpdater.LAYER_XML_FILE);
        final LayerFileSystemImpl fs = HttpUtilities.get(service, uri, this::readLayer, null, true);
        rootChildren.setFolder(fs);
        dirty = false;
        Mutex.EVENT.writeAccess(() -> initialize(fs, Lookups.fixed(fs, root)));
        cSupport.fireChange();
    }

    protected LayerFileSystemImpl readLayer(final String lm, final InputStream in) throws IOException {
        try (final InputStream is = in) {
            final InputSource inputSource = new InputSource(is);
            dom = XMLUtil.parse(inputSource, false, true, null, null);
            final JAXBElement<LayerFileSystemImpl> el = binder.unmarshal(dom, LayerFileSystemImpl.class);
            return el.getValue();
        } catch (JAXBException | SAXException ex) {
            throw new IOException(ex);
        }
    }

    public void save() {
        RP.post(() -> {
            try {
                saveImpl();
                dirty = true;
                cSupport.fireChange();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    public void saveImpl() throws IOException {
        final String davBase = URLs.adminResourcesDavBase(LocalProperties.find(provider));
        final WebProvider service = WebProvider.find(provider, WebProvider.class);
        final URI uri = URI.create(davBase + LayerUpdater.LAYER_XML_FILE);
        final byte[] file = writeLayer((LayerFileSystemImpl) rootChildren.getFolder());
        HttpUtilities.put(service, uri, file, null, null);
    }

    protected byte[] writeLayer(final LayerFileSystemImpl fs) throws IOException {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream(10000)) {
            binder.updateXML(fs);
            final String now = LocalDateTime.now().atZone(ZoneId.of("UTC")).toString();
            final Comment comment = dom.createComment("Modified: " + now);
            dom.getDocumentElement().appendChild(comment);
            XMLUtil.write(dom, baos, "UTF-8");
            return baos.toByteArray();
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    boolean markedDirty() {
        return dirty;
    }

    @Override
    protected AbstractLayerFile getItemAt(final Object node) {
        final Node n = Visualizer.findNode(node);
        return n.getLookup().lookup(AbstractLayerFile.class);
    }

    @Override
    protected Object getHierarchicalColumnHeader() {
        return NbBundle.getMessage(LayerPropertiesModel.class, "LayerPropertiesModel.hierarchicalColumnHeader");
    }

    @Override
    protected int getHierarchicalColumnWidth() {
        return 240;
    }

    public boolean isReadOnly() {
        return false;
    }

    public boolean isModified() {
        return false;
    }

    public void addChangeListener(final ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    public void removeChangeListener(final ChangeListener listener) {
        cSupport.removeChangeListener(listener);
    }

    private class Listener implements JMSListener<AppResourceEvent> {

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }

        @Override
        public void onMessage(final AppResourceEvent event) {
            if (Objects.equals(LayerUpdater.LAYER_XML_FILE, event.getResource())) {
                task.schedule(1500);
            }
        }
    }

    static class FolderNode extends AbstractNode {

        FolderNode(final LayerFolderImpl folder) {
            this(new LayerFileChildren(folder), Lookups.singleton(folder), folder.getName());
        }

        FolderNode(final ChildFactory<AbstractLayerFile> ch, final Lookup lkp, final String name) {
            super(Children.create(ch, true), lkp);
            setIconBaseWithExtension("/org/thespheres/betula/services/implementation/ui/resources/layers-stack.png");
            setDisplayName(name);
        }

    }

    static class FileNode extends AbstractNode {

        FileNode(final LayerFileImpl file) {
            super(Children.create(new LayerFileChildren(file), true), Lookups.singleton(file));
            setIconBaseWithExtension("/org/thespheres/betula/services/implementation/ui/resources/layer.png");
            setDisplayName(file.getName());
        }

    }

    static class AttributeNode extends AbstractNode {

        AttributeNode(final LayerFileAttribute attr) {
            super(Children.LEAF, Lookups.singleton(attr));
            setIconBaseWithExtension("/org/thespheres/betula/services/implementation/ui/resources/zone-medium.png");
            setDisplayName(attr.getName());
        }

    }

    static class LayerFileChildren extends ChildFactory<AbstractLayerFile> {

        private LayerFolderImpl folder;
        private LayerFileImpl file;

        LayerFileChildren() {
        }

        LayerFileChildren(final LayerFolderImpl folder) {
            this.folder = folder;
        }

        LayerFileChildren(final LayerFileImpl file) {
            this.file = file;
        }

        void setFolder(final LayerFolderImpl folder) {
            this.folder = folder;
            refresh(false);
        }

        LayerFolderImpl getFolder() {
            return folder;
        }

        @Override
        protected boolean createKeys(final List<AbstractLayerFile> list) {
            if (folder != null) {
                folder.files().stream()
                        .filter(LayerFolderImpl.class::isInstance)
                        .forEach(list::add);
                folder.files().stream()
                        .filter(LayerFileImpl.class::isInstance)
                        .forEach(list::add);
                folder.files().stream()
                        .filter(LayerFileAttribute.class::isInstance)
                        .forEach(list::add);
            } else if (file != null) {
                file.getAttributes().stream()
                        .forEach(list::add);
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(final AbstractLayerFile key) {
            if (key instanceof LayerFolderImpl) {
                return new FolderNode((LayerFolderImpl) key);
            } else if (key instanceof LayerFileImpl) {
                return new FileNode((LayerFileImpl) key);
            } else if (key instanceof LayerFileAttribute) {
                return new AttributeNode((LayerFileAttribute) key);
            }
            return null;
        }

    }

}
