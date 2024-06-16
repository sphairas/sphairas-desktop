/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui;

import java.awt.Image;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.ui.util.IconAnnotator;
import org.thespheres.betula.ui.util.IconAnnotatorFactory;

/**
 *
 * @author boris.heithecker
 */
@ActionReferences({
    @ActionReference(id = @ActionID(category = "Betula", id = "org.thespheres.betula.ui.actions.ExportCSVAction"),
            path = "Loaders" + TargetsSelectionTargetNode.MIME + "Actions", position = 280000, separatorBefore = 200000)}
)
public class TargetsSelectionTargetNode extends AbstractNode {

    private final List<DocumentId> targets;
    public final static String ICON = "org/thespheres/betula/admin/units/resources/blue-document-attribute-l.png";
    static final String MIME = "application/betula-remote-target-assessment-document";
    private final AnnotationListener annotationListener;
    private final Map<IconAnnotatorFactory, IconAnnotator> annotators = new WeakHashMap<>();
    private final String provider;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public TargetsSelectionTargetNode(List<DocumentId> key, String provider) {
        super(Children.LEAF, Lookups.fixed(key));
        setName(key.stream().map(DocumentId::toString).collect(Collectors.joining(" ")));
        this.targets = key;
        this.provider = provider;
        setIconBaseWithExtension(ICON);
        annotationListener = new AnnotationListener();
        annotationListener.init();
        updateName();
    }

    private void updateName() {
        String name = targets.stream()
                .map(d -> {
                    try {
                        return NamingResolver.find(provider).resolveDisplayName(d);
                    } catch (IllegalAuthorityException ex) {
                        return d.toString();
                    }
                })
                .collect(Collectors.joining(", "));
        setDisplayName(name);
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
                            final IconAnnotator ia = iaf.createIconAnnotator(TargetsSelectionTargetNode.this.getLookup());
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
            TargetsSelectionTargetNode.this.fireIconChange();
            TargetsSelectionTargetNode.this.fireOpenedIconChange();
            TargetsSelectionTargetNode.this.fireDisplayNameChange(null, null);
        }
    }

}
