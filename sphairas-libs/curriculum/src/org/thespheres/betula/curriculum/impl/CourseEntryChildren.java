/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.impl;

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
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.NodeAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.thespheres.betula.curriculum.CourseEntry;
import org.thespheres.betula.curriculum.CourseGroup;
import org.thespheres.betula.curriculum.Curriculum;
import org.thespheres.betula.curriculum.util.CurriculumUtil;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;

/**
 *
 * @author boris.heithecker
 */
class CourseEntryChildren extends Index.KeysChildren<CourseEntry> {

//    private Model2 model;
    private CurriculumDataObject env;
    private final CourseEntry entry;

    CourseEntryChildren(final CourseGroup key, final CurriculumDataObject env) {
        super(key != null ? new DelegateList(key.getChildren()) : new DelegateList());
        this.entry = key;
    }

    @Override
    protected Node[] createNodes(final CourseEntry key) {
        if (CourseGroup.class.isInstance(key)) {
            return new CourseGroupNode[]{new CourseGroupNode((CourseGroup) key, env)};
        } else if (CourseEntry.class.isInstance(key)) {
            return new CourseNode[]{new CourseNode((CourseEntry) key, new InstanceContent(), env)};
        }
        throw new IllegalArgumentException();
    }

    void setCurriculum(final Curriculum cur, final CurriculumDataObject env) {
        this.env = env;
        ((DelegateList) list).setDelegate(cur.getEntries());
        update();
    }

//    void setModel(Model2 m) {
//        model = m;
//    }
    static class DelegateList<CE extends CourseEntry> extends ForwardingList<CE> {

        private List<CE> delegate;

        DelegateList(final List<CE> children) {
            this.delegate = children;
        }

        DelegateList() {
            this(Collections.EMPTY_LIST);
        }

        @Override
        protected List<CE> delegate() {
            return delegate;
        }

        void setDelegate(final List<CE> delegate) {
            synchronized (this) {
                this.delegate = delegate;
            }

        }
    }

    static class EditBemerkungenSetRootNode extends AbstractNode {

        public EditBemerkungenSetRootNode(final CourseEntryChildren ch) {
            super(ch);
        }

    }

    @Messages({"ElementNode.displayName={0} [{1}]",
        "ElementNode.element.multiple=Mehrfachauswahl",
        "ElementNode.element.single=Einfachauswahl"})
    class CourseGroupNode extends AbstractNode {

        private final CourseEntryChildren children;

        @SuppressWarnings(value = {"OverridableMethodCallInConstructor",
            "LeakingThisInConstructor"})
        private CourseGroupNode(final CourseGroup key, final CourseEntryChildren ch, final InstanceContent ic, final CurriculumDataObject env) {
            super(ch, new AbstractLookup(ic));
            setName(key.getId());
            setDisplayName(key.getName());
            ic.add(key);
            ic.add(ch.getIndex());
            ic.add(env);
            children = ch;
            setIconBaseWithExtension("org/thespheres/betula/curriculum/resources/books-stack.png");
        }

        private CourseGroupNode(final CourseGroup key, final CurriculumDataObject env) {
            this(key, new CourseEntryChildren(key, env), new InstanceContent(), env);
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
                            final CourseGroup el = CourseGroupNode.this.getLookup().lookup(CourseGroup.class);
//                            el.addItem(index, m);
//                            children.update();
//                            model.setModified();
                            return null;
                        }
                    };
                }
            }
            return null;
        }
    }

    static class CourseNode extends AbstractNode {

        @SuppressWarnings(value = {"OverridableMethodCallInConstructor",
            "LeakingThisInConstructor"})
        private CourseNode(final CourseEntry item, final InstanceContent ic, final CurriculumDataObject env) {
            super(Children.LEAF, new AbstractLookup(ic)); //Lookups.fixed(key, key.getMarkers().get(pos)));
            setName(item.getId());
            setDisplayName(CurriculumUtil.getDisplayName(item));
            ic.add(item);
            ic.add(this);
//            ic.add(env);
            setIconBaseWithExtension("org/thespheres/betula/curriculum/resources/book.png");
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
//            final Element el = getLookup().lookup(Element.class);
//            final MarkerItem im = getLookup().lookup(MarkerItem.class);
//            el.removeItem(im);
//            final CourseGroupNodeChildren children = (CourseGroupNodeChildren) getParentNode().getChildren();
//            children.update();
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
