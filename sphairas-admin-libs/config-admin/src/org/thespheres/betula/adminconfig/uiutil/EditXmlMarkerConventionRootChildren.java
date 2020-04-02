/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig.uiutil;

import com.google.common.collect.ForwardingList;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.openide.awt.Actions;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.xmldefinitions.XmlMarkerConventionDefinition;
import org.thespheres.betula.xmldefinitions.XmlMarkerConventionDefinition.XmlMarkerSubsetDefinition;
import org.thespheres.betula.xmldefinitions.XmlMarkerDefinition;

/**
 *
 * @author boris.heithecker
 */
class EditXmlMarkerConventionRootChildren extends Index.KeysChildren<XmlMarkerSubsetDefinition> {

    private final Node root;
    private EditXmlMarkerConventionTableModel model;
    private XmlMarkerConventionDefinition cnv;

    EditXmlMarkerConventionRootChildren() {
        super(new DelegateList());
        this.root = new EditBemerkungenSetRootNode(this);
    }

    @Override
    protected Node[] createNodes(XmlMarkerSubsetDefinition key) {
        return new SubsetNode[]{new SubsetNode(key)};
    }

    Node getRoot() {
        return root;
    }

    void setTemplate(XmlMarkerConventionDefinition def) {
        this.cnv = def;
        ((DelegateList) list).setDelegate(def.getMarkerSubsets());
        update();
    }

    void setModel(EditXmlMarkerConventionTableModel m) {
        model = m;
    }

    static class DelegateList extends ForwardingList<XmlMarkerSubsetDefinition> {

        private List<XmlMarkerSubsetDefinition> delegate = Collections.EMPTY_LIST;

        @Override
        protected List<XmlMarkerSubsetDefinition> delegate() {
            return delegate;
        }

        void setDelegate(List<XmlMarkerSubsetDefinition> delegate) {
            synchronized (this) {
                this.delegate = delegate;
            }

        }
    }

    static class EditBemerkungenSetRootNode extends AbstractNode {

        public EditBemerkungenSetRootNode(EditXmlMarkerConventionRootChildren ch) {
            super(ch);
        }

    }

    @Messages({"ElementNode.displayName={0} [{1}]",
        "ElementNode.element.multiple=Mehrfachauswahl",
        "ElementNode.element.single=Einfachauswahl"})
    class SubsetNode extends AbstractNode {

        private final SubsetNodeMarkerChildren children;

        @SuppressWarnings(value = {"OverridableMethodCallInConstructor",
            "LeakingThisInConstructor"})
        private SubsetNode(XmlMarkerSubsetDefinition key, SubsetNodeMarkerChildren ch, InstanceContent ic) {
            super(ch, new AbstractLookup(ic));
            setName(key.getSubset());
            ic.add(key);
            ic.add(ch.getIndex());
            ic.add(cnv);
            children = ch;
            final String displayName = NbBundle.getMessage(SubsetNode.class, "ElementNode.displayName", key.getCategory());
            setDisplayName(displayName);
            setIconBaseWithExtension("org/thespheres/betula/niedersachsen/admin/ui/resources/category.png");
        }

        private SubsetNode(XmlMarkerSubsetDefinition key) {
            this(key, new SubsetNodeMarkerChildren(key, cnv), new InstanceContent());
        }

        @Override
        public boolean canCut() {
            return true;
        }

        @Override
        public boolean canCopy() {
            return true;
        }

        @Override
        public boolean canDestroy() {
            return true;
        }

        @Override
        public void destroy() throws IOException {
            //TODO: Warning, template change will effect order of bemerkungen in past reports
            final XmlMarkerSubsetDefinition el = getLookup().lookup(XmlMarkerSubsetDefinition.class);
            final XmlMarkerDefinition im = getLookup().lookup(XmlMarkerDefinition.class);
            el.getMarkerDefinitions().remove(im);
            final SubsetNodeMarkerChildren children = (SubsetNodeMarkerChildren) getParentNode().getChildren();
            children.update();
        }

        @Override
        public Action[] getActions(boolean context) { //Actions.forID(PROP_NAME, PROP_ICON)
            Action up = Actions.forID("System", "org.openide.actions.MoveUpAction");
            Action down = Actions.forID("System", "org.openide.actions.MoveDownAction");
            Action del = Actions.forID("Edit", "org.openide.actions.DeleteAction");
            return new Action[]{up, down, del};
        }

        @Override
        public PasteType getDropType(final Transferable t, int action, int index) {
            final Node n = NodeTransfer.node(t, DnDConstants.ACTION_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
            if (n != null) {
                final XmlMarkerDefinition m = n.getLookup().lookup(XmlMarkerDefinition.class);
                final XmlMarkerSubsetDefinition sub = n.getLookup().lookup(XmlMarkerSubsetDefinition.class);
                if (sub != null && m != null) {
                    return new PasteType() {
                        @Override
                        public Transferable paste() throws IOException {
                            final XmlMarkerSubsetDefinition el = SubsetNode.this.getLookup().lookup(XmlMarkerSubsetDefinition.class);
                            el.getMarkerDefinitions().add(index, m);
                            children.update();
                            model.setModified();
                            return null;
                        }
                    };
                } else if (sub != null) {
                    return new PasteType() {
                        @Override
                        public Transferable paste() throws IOException {
                            final XmlMarkerSubsetDefinition el = SubsetNode.this.getLookup().lookup(XmlMarkerSubsetDefinition.class);
                            el.getConvention().getMarkerSubsets().add(0, el);
                            EditXmlMarkerConventionRootChildren.this.update();
                            model.setModified();
                            return null;
                        }
                    };
                }
            }
            return null;
        }
    }

    static class SubsetNodeMarkerChildren extends Index.KeysChildren<XmlMarkerDefinition> {

        private final XmlMarkerSubsetDefinition element;
        private final XmlMarkerConventionDefinition env;

        SubsetNodeMarkerChildren(final XmlMarkerSubsetDefinition element, XmlMarkerConventionDefinition env) {
            super(element.getMarkerDefinitions());
            this.element = element;
            this.env = env;
        }

        @Override
        protected Node[] createNodes(XmlMarkerDefinition key) {
            return new MarkerNode[]{new MarkerNode(element, key, new InstanceContent(), env)};
        }

    }

    static class MarkerNode extends AbstractNode {

        @SuppressWarnings(value = {"OverridableMethodCallInConstructor",
            "LeakingThisInConstructor"})
        private MarkerNode(XmlMarkerSubsetDefinition key, XmlMarkerDefinition item, InstanceContent ic, XmlMarkerConventionDefinition env) {
            super(Children.LEAF, new AbstractLookup(ic)); //Lookups.fixed(key, key.getMarkers().get(pos)));
            setName(item.getId());
            ic.add(key);
            ic.add(item);
            ic.add(this);
            ic.add(env);
//            if (Marker.isNull(item)) {
//                setDisplayName("---");
//            } else {
////                setDisplayName(item.getLongLabel(ReportContextListener.getDefault().getCurrentFormatArgs()));
//            }
            setIconBaseWithExtension("org/thespheres/betula/niedersachsen/admin/ui/resources/document-horizontal-text.png");
        }

        @Override
        public boolean canCut() {
            return true;
        }

        @Override
        public boolean canCopy() {
            return true;
        }

        @Override
        public boolean canDestroy() {
            return true;
        }

        @Override
        public void destroy() throws IOException {
            //TODO: Warning, template change will effect order of bemerkungen in past reports
            final XmlMarkerSubsetDefinition el = getLookup().lookup(XmlMarkerSubsetDefinition.class);
            final XmlMarkerDefinition im = getLookup().lookup(XmlMarkerDefinition.class);
            el.getMarkerDefinitions().remove(im);
            final SubsetNodeMarkerChildren children = (SubsetNodeMarkerChildren) getParentNode().getChildren();
            children.update();
        }

        @Override
        public Action[] getActions(boolean context) { //Actions.forID(PROP_NAME, PROP_ICON)
            Action up = Actions.forID("System", "org.openide.actions.MoveUpAction");
            Action down = Actions.forID("System", "org.openide.actions.MoveDownAction");
            Action del = Actions.forID("Edit", "org.openide.actions.DeleteAction");
            return new Action[]{up, down, del};
        }

        @Override
        public PasteType getDropType(final Transferable t, int action, int index) {
            final Node n = NodeTransfer.node(t, DnDConstants.ACTION_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
            if (n != null) {
                final Marker m = n.getLookup().lookup(Marker.class);
//                final EditableProblem p = n.getLookup().lookup(EditableProblem.class);
//                if (p != null && !p.isBasket() && !this.equals(n.getParentNode())) {
//                    return new PasteType() {
//                        @Override
//                        public Transferable paste() throws IOException {
//                            ((EditableBasket) problem).addReference(p);
////                        ((EditableBasket) problem).setParent((EditableBasket) problem);
//                            return null;
//                        }
//                    };
//                }
            }
            return null;
        }
    }
}
