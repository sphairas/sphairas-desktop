/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

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
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.NodeAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate.Element;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate.MarkerItem;

/**
 *
 * @author boris.heithecker
 */
class EditBemerkungenSetRootChildren extends Index.KeysChildren<Element> {

    private final Node root;
//    private TermReportNoteSetTemplate2 template;
    private EditBemerkungenSetModel model;
    private EditBemerkungenEnv env;

    EditBemerkungenSetRootChildren() {
        super(new DelegateList());
        this.root = new EditBemerkungenSetRootNode(this);
    }

    @Override
    protected Node[] createNodes(Element key) {
        return new ElementNode[]{new ElementNode(key, env)};
    }

    Node getRoot() {
        return root;
    }

    void setTemplate(TermReportNoteSetTemplate template, EditBemerkungenEnv env) {
        this.env = env;
        ((DelegateList) list).setDelegate(template.getElements());
        update();
    }

//    void refresh() {
//        refresh(false);
//    }
    void setModel(EditBemerkungenSetModel m) {
        model = m;
    }

    static class DelegateList extends ForwardingList<Element> {

        private List<Element> delegate = Collections.EMPTY_LIST;

        @Override
        protected List<Element> delegate() {
            return delegate;
        }

        void setDelegate(List<Element> delegate) {
            synchronized (this) {
                this.delegate = delegate;
            }

        }
    }

    static class EditBemerkungenSetRootNode extends AbstractNode {

        public EditBemerkungenSetRootNode(EditBemerkungenSetRootChildren ch) {
            super(ch);
        }

    }

    @Messages({"ElementNode.displayName={0} [{1}]",
        "ElementNode.element.multiple=Mehrfachauswahl",
        "ElementNode.element.single=Einfachauswahl"})
    class ElementNode extends AbstractNode {

        private final ElementNodeMarkerChildren children;

        @SuppressWarnings(value = {"OverridableMethodCallInConstructor",
            "LeakingThisInConstructor"})
        private ElementNode(Element key, ElementNodeMarkerChildren ch, InstanceContent ic, EditBemerkungenEnv env) {
            super(ch, new AbstractLookup(ic));
            setName(key.getElementDisplayName());
            ic.add(key);
            ic.add(ch.getIndex());
            ic.add(env);
            children = ch;
            final String choice = key.isMultiple() ? NbBundle.getMessage(ElementNode.class, "ElementNode.element.multiple") : NbBundle.getMessage(ElementNode.class, "ElementNode.element.single");
            final String displayName = NbBundle.getMessage(ElementNode.class, "ElementNode.displayName", getName(), choice);
            setDisplayName(displayName);
            setIconBaseWithExtension("org/thespheres/betula/niedersachsen/admin/ui/resources/category.png");
        }

        private ElementNode(Element key, EditBemerkungenEnv env) {
            this(key, new ElementNodeMarkerChildren(key, env), new InstanceContent(), env);
        }

        @Override
        public PasteType getDropType(final Transferable t, int action, int index) {
            final Node n = NodeTransfer.node(t, DnDConstants.ACTION_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
            if (n != null) {
                final Marker m = n.getLookup().lookup(Marker.class);
                final MarkerConvention mc = n.getLookup().lookup(MarkerConvention.class);
                if (mc != null && m != null) {
                    return new PasteType() {
                        @Override
                        public Transferable paste() throws IOException {
                            final Element el = ElementNode.this.getLookup().lookup(Element.class);
                            el.addItem(index, m);
                            children.update();
                            model.setModified();
                            return null;
                        }
                    };
                }
            }
            return null;
        }
    }

    static class ElementNodeMarkerChildren extends Index.KeysChildren<MarkerItem> {

        private final Element element;
        private final EditBemerkungenEnv env;

        ElementNodeMarkerChildren(final Element element, EditBemerkungenEnv env) {
            super(element.getMarkers());
            this.element = element;
            this.env = env;
        }

        @Override
        protected Node[] createNodes(MarkerItem key) {
            return new MarkerNode[]{new MarkerNode(element, key, new InstanceContent(), env)};
        }

    }

    static class MarkerNode extends AbstractNode {

        @SuppressWarnings(value = {"OverridableMethodCallInConstructor",
            "LeakingThisInConstructor"})
        private MarkerNode(Element key, MarkerItem item, InstanceContent ic, EditBemerkungenEnv env) {
            super(Children.LEAF, new AbstractLookup(ic)); //Lookups.fixed(key, key.getMarkers().get(pos)));
            setName(item.getMarker().toString());
            ic.add(key);
            ic.add(item);
            ic.add(this);
            ic.add(env);
            if (Marker.isNull(item.getMarker())) {
                setDisplayName("---");
            } else {
                setDisplayName(item.getMarker().getLongLabel(ReportContextListener.getDefault().getCurrentFormatArgs()));
            }
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
            final Element el = getLookup().lookup(Element.class);
            final MarkerItem im = getLookup().lookup(MarkerItem.class);
            el.removeItem(im);
            final ElementNodeMarkerChildren children = (ElementNodeMarkerChildren) getParentNode().getChildren();
            children.update();
        }

        @Override
        public Action[] getActions(boolean context) { //Actions.forID(PROP_NAME, PROP_ICON)
            Action up = Actions.forID("System", "org.openide.actions.MoveUpAction");
            Action down = Actions.forID("System", "org.openide.actions.MoveDownAction");
            Action del = Actions.forID("Edit", "org.openide.actions.DeleteAction");
            return new Action[]{up, down, del, TestA.instance};
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

    private static class TestA extends NodeAction {

        static TestA instance = new TestA();

        @Override
        protected void performAction(Node[] activatedNodes) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

    }

}
