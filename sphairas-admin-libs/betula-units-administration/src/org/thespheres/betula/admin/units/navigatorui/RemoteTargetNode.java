/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.navigatorui;

import java.awt.Image;
import java.io.IOException;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.ui.util.IconAnnotator;
import org.thespheres.betula.ui.util.IconAnnotatorFactory;

/**
 *
 * @author boris.heithecker
 */
@ActionReferences({
    @ActionReference(id = @ActionID(category = "Betula", id = "org.thespheres.betula.admin.units.actions.RefreshAction"),
            path = "Loaders/" + RemoteTargetNode.MIME + "/Actions", position = 130000, separatorBefore = 100000)})
public class RemoteTargetNode extends AbstractNode {

    private final List<RemoteTargetAssessmentDocument> target;
    private final DocumentId baseDocument;
    private final RemoteUnitsModel model;
    public final static String ICON_COLL = "org/thespheres/betula/admin/units/resources/box-document.png";
    public final static String ICON = "org/thespheres/betula/admin/units/resources/blue-document-attribute-l.png";
    private boolean showTargetTypeOnly;
    static final String MIME = "application/betula-remote-target-assessment-document";
    private final AnnotationListener annotationListener;
    private final Map<IconAnnotatorFactory, IconAnnotator> annotators = new WeakHashMap<>();

    @SuppressWarnings("OverridableMethodCallInConstructor")
    private RemoteTargetNode(List<RemoteTargetAssessmentDocument> key, RemoteUnitsModel model, DocumentId display, Lookup lkp) {
        super(key.size() == 1 ? Children.LEAF : Children.create(new RemoteTargetNodeChildren(key, model), true), lkp);
        final String name = key.stream()
                .map(RemoteTargetAssessmentDocument::getDocumentId)
                .map(DocumentId::toString)
                .collect(Collectors.joining(","));
        setName(name);
        this.target = key;
        this.baseDocument = display;
        setShortDescription(key.size() == 1 ? key.get(0).getDocumentId().getId() : baseDocument.getId());//Tooltip
        this.model = model;
        String ib = key.size() == 1 ? ICON : ICON_COLL;
        setIconBaseWithExtension(ib);
        annotationListener = new AnnotationListener();
        annotationListener.init();
        updateName();
    }

    private void updateName() {
        String name = null;
        if (target.size() == 1) {
            final RemoteTargetAssessmentDocument tg = target.get(0);
            if (showTargetTypeOnly) {
                name = tg.getTargetType();
            } else {
                name = tg.getName().getDisplayName(null);
            }
            setDisplayName(name);
        } else if (baseDocument != null) {
            try {
                name = model.getUnitOpenSupport().findNamingResolver().resolveDisplayName(baseDocument);
            } catch (IOException | IllegalAuthorityException ex) {
                name = baseDocument.getId();
            } finally {
                setDisplayName(name);
            }
        }
    }

    private void setShowTargetTypeOnly(boolean b) {
        this.showTargetTypeOnly = b;
    }

    DocumentId key() {
        return key(target, baseDocument);
    }

    static RemoteTargetNode create(RemoteTargetAssessmentDocument single, RemoteUnitsModel model, DocumentId display) {
        return create(Collections.singletonList(single), model, display);
    }

    static RemoteTargetNode create(List<RemoteTargetAssessmentDocument> l, RemoteUnitsModel model, DocumentId display) {
        final Object[] arr = Stream.concat(l.stream(), Stream.of(model.getUnitOpenSupport())).toArray();
        return new RemoteTargetNode(l, model, display, Lookups.fixed(arr));
    }

    private static DocumentId key(List<RemoteTargetAssessmentDocument> key, DocumentId display) {
        if (key.size() == 1) {
            return key.get(0).getDocumentId();
        } else {
            return display;
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Loaders/" + MIME + "/Actions").stream()
                .toArray(Action[]::new);
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

    protected class AnnotationListener implements LookupListener, ChangeListener {

        private final Lookup.Result<IconAnnotatorFactory> result;

        @SuppressWarnings("LeakingThisInConstructor")
        private AnnotationListener() {
            result = MimeLookup.getLookup(MIME).lookupResult(IconAnnotatorFactory.class);

        }

        private void init() {
            result.removeLookupListener(this);
            synchronized (annotators) {
                annotators.clear();
                result.allInstances().stream()
                        .forEach(iaf -> {
                            final IconAnnotator ia = iaf.createIconAnnotator(RemoteTargetNode.this.getLookup());
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
            fireChange();
        }

        private void fireChange() {
            RemoteTargetNode.this.fireIconChange();
            RemoteTargetNode.this.fireOpenedIconChange();
            RemoteTargetNode.this.fireDisplayNameChange(null, null);
        }
    }

    private static class RemoteTargetNodeChildren extends ChildFactory<DocumentId> {

        private final RemoteUnitsModel rModel;
        private final List<RemoteTargetAssessmentDocument> rtad;
        private final Map<DocumentId, RemoteTargetNode> registry = new HashMap<>();

        @SuppressWarnings("LeakingThisInConstructor")
        private RemoteTargetNodeChildren(List<RemoteTargetAssessmentDocument> support, RemoteUnitsModel model) {
            this.rModel = model;
            this.rtad = support;
        }

        @Override
        protected boolean createKeys(List<DocumentId> toPopulate) {
            if (rModel != null) {
                rtad.stream()
                        .map(tg -> RemoteTargetNode.create(tg, rModel, tg.getDocumentId()))
                        .peek(n -> registry.computeIfAbsent(n.key(), k -> n))
                        .peek(tg -> tg.setShowTargetTypeOnly(true))
                        .sorted(Comparator.comparing(Node::getDisplayName, Collator.getInstance(Locale.GERMAN)))
                        .map(RemoteTargetNode::key)
                        .forEach(toPopulate::add);
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(DocumentId key) {
            return registry.get(key);
        }

    }
}
