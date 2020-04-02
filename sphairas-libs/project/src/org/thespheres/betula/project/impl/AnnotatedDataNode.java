/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.impl;

import java.awt.Image;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataFolder.FolderNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.thespheres.betula.ui.util.IconAnnotator;
import org.thespheres.betula.ui.util.IconAnnotatorFactory;

/**
 *
 * @author boris.heithecker
 */
class AnnotatedDataNode extends FilterNode {

    private final AnnotationListener annotationListener;
    private final Map<IconAnnotatorFactory, IconAnnotator> annotators = new WeakHashMap<>();
    protected final String mimeType;

    AnnotatedDataNode(DataObject original) {
        this(original.getNodeDelegate(), original.getPrimaryFile().getMIMEType());
    }

    private AnnotatedDataNode(Node original, String mime) {
        super(original, new AnnotatedChildren(original));
        this.mimeType = mime;
        annotationListener = new AnnotationListener();
        annotationListener.init();
    }

    @Override
    public boolean canDestroy() {
        if (!canDestroyFolder()) {
            return false;
        }
        return super.canDestroy();
    }

    private boolean canDestroyFolder() {
        if (getOriginal() instanceof FolderNode
                || getOriginal().getLookup().lookup(DataFolder.class) != null) {
            final DataObject[] ch = getOriginal().getLookup().lookup(DataFolder.class).getChildren();
            if (ch.length != 0) {
                return !Arrays.stream(ch)
                        .map(DataObject::getPrimaryFile)
                        .anyMatch(Util::isHidden);
            }
        }
        return true;
    }

    @Override
    public Image getOpenedIcon(int type) {
        Image orig = super.getOpenedIcon(type);
        return annotateIcon(orig, true);
    }

    @Override
    public Image getIcon(int type) {
        Image orig = super.getIcon(type);
        return annotateIcon(orig, false);
    }

    protected Image annotateIcon(Image img, boolean opened) {
        synchronized (annotators) {
            for (IconAnnotator pa : annotators.values()) {
                img = pa.annotateIcon(img, opened);
            }
        }
        return img;
    }

    @Override
    public String getHtmlDisplayName() {
        String orig = super.getHtmlDisplayName();
        return annotateHtml(orig);
    }

    protected String annotateHtml(String html) {
        synchronized (annotators) {
            for (IconAnnotator pa : annotators.values()) {
                html = pa.annotateHtml(getDisplayName(), html);
            }
        }
        return html;
    }

    static class AnnotatedChildren extends FilterNode.Children {

        private final static RequestProcessor RP = new RequestProcessor(AnnotatedChildren.class);

        AnnotatedChildren(Node or) {
            super(or);
        }

        @Override
        protected Node[] createNodes(Node key) {
            return Arrays.stream(super.createNodes(key))
                    .map(this::findDelegate)
                    .filter(Objects::nonNull)
                    .toArray(Node[]::new);
        }

        protected Node findDelegate(Node original) {
            DataObject dob = null;
            try {
                dob = RP.invokeAny(Collections.singletonList((Callable<DataObject>) () -> original.getLookup().lookup(DataObject.class)));
            } catch (InterruptedException | ExecutionException ex) {
            }
            if (dob != null) {
                final FileObject pf = dob.getPrimaryFile();
                if (Util.isHidden(pf)) {
                    return null;
                }
            }
            return dob != null ? new AnnotatedDataNode(dob) : new FilterNode(original, new AnnotatedChildren(original));
        }

    }

    protected class AnnotationListener implements LookupListener, ChangeListener {

        private final Lookup.Result<IconAnnotatorFactory> result;

        private AnnotationListener() {
            result = MimeLookup.getLookup(mimeType).lookupResult(IconAnnotatorFactory.class);
        }

        private void init() {
            result.removeLookupListener(this);
            synchronized (annotators) {
                annotators.clear();
                result.allInstances().stream()
                        .forEach(iaf -> {
                            final IconAnnotator ia = iaf.createIconAnnotator(AnnotatedDataNode.this.getLookup());
                            if (ia != null) {
                                annotators.put(iaf, ia);
                                ia.addChangeListener(WeakListeners.change(this, ia));
                            }
                        });
            }
            result.addLookupListener(this);
        }

        public @Override
        void resultChanged(LookupEvent ev) {
            init();
            stateChanged(null);
        }

        public @Override
        void stateChanged(ChangeEvent e) {
            AnnotatedDataNode.this.fireIconChange();
            AnnotatedDataNode.this.fireOpenedIconChange();
            AnnotatedDataNode.this.fireDisplayNameChange(null, null);
        }
    }
}
