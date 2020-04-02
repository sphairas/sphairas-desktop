/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.model;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.Action;
import org.openide.actions.ReorderAction;
import org.openide.nodes.*;
import org.openide.util.Exceptions;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.thespheres.betula.termreport.AssessmentProvider;
import org.thespheres.betula.termreport.xml.XmlTermReport;

/**
 *
 * @author boris.heithecker
 */
class XmlTermReportImplNode extends FilterNode {

    private final InstanceContent ic;

    XmlTermReportImplNode(XmlTermReportImpl impl) { // use Index.ArrayChildren
        this(impl, new InstanceContent());
    }

    private XmlTermReportImplNode(XmlTermReportImpl impl, InstanceContent ic) { // use Index.ArrayChildren
        super(impl.getEnvironment().getNodeDelegate(), impl.children, new ProxyLookup(impl.getEnvironment().getLookup(), new AbstractLookup(ic)));
        this.ic = ic;
        this.ic.add(impl.children.getIndex());
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] orig = getOriginal().getActions(context);
        Action[] ret = Arrays.copyOf(orig, orig.length + 2);
        ret[ret.length - 1] = ReorderAction.get(ReorderAction.class);
        return ret;
    }

    @Override
    public PasteType getDropType(final Transferable t, int arg1, int arg2) {
        if (t.isDataFlavorSupported(AssessmentProvider.ASSESSMENT_PROVIDER_FLAVOR)) {
            return new PasteType() {
                @Override
                public Transferable paste() throws IOException {
                    try {
                        AssessmentProvider nta = (AssessmentProvider) t.getTransferData(AssessmentProvider.ASSESSMENT_PROVIDER_FLAVOR);
                        XmlTermReport tr = getLookup().lookup(XmlTermReport.class);
                        final Node node = NodeTransfer.node(t, NodeTransfer.DND_MOVE + NodeTransfer.CLIPBOARD_CUT);
                        if (node != null) {
                            node.destroy();
                        }
                    } catch (UnsupportedFlavorException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    return null;
                }
            };
        } else {
            return null;
        }
    }
}
