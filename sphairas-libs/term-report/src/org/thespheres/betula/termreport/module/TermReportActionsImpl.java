/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.module;

import com.google.common.eventbus.Subscribe;
import java.awt.Component;
import java.awt.Point;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.PasteType;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.termreport.AssessmentProvider;
import org.thespheres.betula.termreport.TableColumnConfiguration;
import org.thespheres.betula.termreport.TermReport;
import org.thespheres.betula.termreport.TermReportActions;
import org.thespheres.betula.termreport.model.XmlFileTargetAssessmentProvider;
import org.thespheres.betula.termreport.model.XmlTermReportImpl;
import org.thespheres.betula.util.CollectionChangeEvent;

class TermReportActionsImpl extends TermReportActions<XmlTermReportImpl> {

    private final Lookup context;

    @SuppressWarnings({"LeakingThisInConstructor"})
    TermReportActionsImpl(XmlTermReportImpl report, Lookup lookup) {
        super(report);
        context = lookup;
        report.getEventBus().register(this);
    }

    @Override
    public Lookup getContext() {
        return context;
    }

    @Override
    public LocalFileProperties getProperties() {
        return getProject() != null ? getProject().getLookup().lookup(LocalFileProperties.class) : null;
    }

//    @Override
//    public AssessmentProvider getAssessmentProviderForTabelModelColumn(int modelIndex) {
//        if (modelIndex < 0 || modelIndex >= report.getProviders().size()) {
//            return null;
//        }
//        TermReportTableModel2 model = getContext().lookup(TermReportSupport.class).getModel();
////        return model.getAssessmentProviderAt(modelIndex);
//        PluggableTableColumn<TermReport, StudentId> c = model.getColumn("providers");
//        if (c instanceof AssessmentProviderColumn) {
//            AssessmentProviderColumn apc = (AssessmentProviderColumn) c;
//            
//            return apc.getAssessmentProvider(modelIndex);
//        }
//        return null;
//    }
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        Node n = NodeTransfer.node(dtde.getTransferable(), NodeTransfer.CLIPBOARD_COPY);
        if (n != null && n.getLookup().lookup(DataObject.class) != null && n.getLookup().lookup(TargetAssessment.class) != null) {
            dtde.acceptDrag(NodeTransfer.CLIPBOARD_COPY);
        } else if (NodeTransfer.findPaste(dtde.getTransferable()) != null) {
            dtde.acceptDrag(NodeTransfer.CLIPBOARD_COPY);
        } else {
            dtde.rejectDrag();
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        final Node n = NodeTransfer.node(dtde.getTransferable(), NodeTransfer.CLIPBOARD_COPY);
        if (n != null && n.getLookup().lookup(DataObject.class) != null && n.getLookup().lookup(TargetAssessment.class) != null && n.getLookup().lookup(TermReportDataObject.class) == null) {
            final DataObject linked = n.getLookup().lookup(DataObject.class);
            final URI link = linked.getPrimaryFile().toURI();
            if (n.getLookup().lookupAll(TargetAssessment.class).size() == 1) {
                final XmlFileTargetAssessmentProvider prov = XmlFileTargetAssessmentProvider.create(link, context, null);
                addAssessmentProvider(prov);
            }
            dtde.dropComplete(true);
        } else if (dropPaste(dtde)) {
            dtde.dropComplete(true);
        } else {
            dtde.rejectDrop();
        }
    }

    private boolean dropPaste(DropTargetDropEvent dtde) {
        Component cmp = dtde.getDropTargetContext().getComponent();
        JXTable tbl = null;
        if (cmp instanceof JScrollPane && ((JScrollPane) cmp).getViewport().getView() instanceof JXTable) {
            tbl = (JXTable) ((JScrollPane) cmp).getViewport().getView();
        } else if (cmp instanceof JXTable) {
            tbl = (JXTable) cmp;
        }
        final JXTable table = tbl;
        if (table != null) {// && table.getModel() instanceof TermReportTableModel) {
            Point p = SwingUtilities.convertPoint(table, dtde.getLocation(), table);
//            int index = table.convertColumnIndexToModel(table.columnAtPoint(p)) - 1;
            TableColumnExt colex = table.getColumnExt(table.columnAtPoint(p));
            AssessmentProvider rtad = (AssessmentProvider) colex.getClientProperty(TableColumnConfiguration.PROP_ASSESSMENTPROVIDER);
//            if (index >= 0) { //isPopupAllowed()
//            AssessmentProvider rtad = getAssessmentProviderForTabelModelColumn(index); //currentModel.getAssessmentProviderAt(index);
            Node n = rtad == null ? null : rtad.getNodeDelegate();
            if (pasteNode(dtde, n, cmp, table)) {
                return true;
            }
//            }
        }
        return false;
    }

    protected boolean pasteNode(DropTargetDropEvent dtde, Node n, Component cmp, final JXTable table) {
        final NodeTransfer.Paste paste = NodeTransfer.findPaste(dtde.getTransferable());
        if (paste != null) {
            final PasteType[] types = Stream.concat(Arrays.stream(paste.types(getTermReport().getNodeDelegate())), n == null ? Stream.empty() : Arrays.stream(paste.types(n)))
                    .toArray(PasteType[]::new);
            if (types.length == 1) {
                try {
                    types[0].paste();
                    return true;
                } catch (IOException ex) {
                }
            } else if (types.length > 1) {
                class PasteAction extends AbstractAction {

                    final PasteType paste;

                    PasteAction(PasteType paste) {
                        super(paste.getName());
                        this.paste = paste;
                    }

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            paste.paste();
                        } catch (IOException ex) {
                        }
                    }

                }
                final Action[] ac = Arrays.stream(types)
                        .map(pt -> new PasteAction(pt))
                        .toArray(Action[]::new);
                Point loc = SwingUtilities.convertPoint(cmp, dtde.getLocation(), table);
                JPopupMenu popup = Utilities.actionsToPopup(ac, table);
                final PopupMenuListener pl = new PopupMenuListener() {

                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    }

                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                        popup.removePopupMenuListener(this);
                        table.requestFocus();
                    }

                    @Override
                    public void popupMenuCanceled(PopupMenuEvent e) {
                    }
                };
                popup.addPopupMenuListener(pl);
                popup.show(table, loc.x, loc.y);
            }
        }
        return false;
    }

    @Override
    public void addAssessmentProvider(AssessmentProvider prov) {
        report.addAssessmentProvider(prov);
    }

    @Override
    public void removeAssessmentProvider(AssessmentProvider prov) {
        report.removeAssessmentProvider(prov);
    }

    @Subscribe
    public void onChange(CollectionChangeEvent event) {
        if (event.getSource() instanceof TermReport) {
            getDataObject().setModified(true);
        }
    }

    private DataObject getDataObject() {
        return context.lookup(DataObject.class);
    }

    private Project getProject() {
        return FileOwnerQuery.getOwner(getDataObject().getPrimaryFile());
    }
}
