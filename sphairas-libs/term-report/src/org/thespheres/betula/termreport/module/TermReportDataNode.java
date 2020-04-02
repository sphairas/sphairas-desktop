/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.module;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.net.URI;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;
import org.thespheres.betula.termreport.TermReportActions;
import org.thespheres.betula.termreport.model.XmlFileTargetAssessmentProvider;

/**
 *
 * @author boris.heithecker
 */
public class TermReportDataNode extends DataNode implements NodeTransfer.Paste {

    TermReportDataNode(TermReportDataObject dob, Lookup lookup) {
        super(dob, Children.LEAF, lookup);
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        ExTransferable etrans = (ExTransferable) super.clipboardCopy();
//        etrans.put(NodeTransfer.createPaste(new TermReportPaste(getLookup())));
        return etrans;
    }
//
//    @Override
//    protected Sheet createSheet() {
//        Sheet ret = super.createSheet();
//        Set set = Sheet.createPropertiesSet();
//        final XmlTermReport tr = getLookup().lookup(XmlTermReport.class);
//        Property preCon = new PropertySupport.ReadWrite<String>("PrefCon", String.class, "DisplayPrefCon", "shortdesPC") {
//
//            @Override
//            public String getValue() throws IllegalAccessException, InvocationTargetException {
//                if (tr != null) {
//                    return "?"; //tr.getPreferredConvention();
//                } else {
//                    return "no ta";
//                }
//            }
//
//            @Override
//            public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//                AssessmentConvention c = null;
//                if ((c = GradeFactory.findConvention(val)) != null) {
////                    tr.setPreferredConvention(c.getName());
//                } else {
//                    throw new IllegalArgumentException("Not a know convention.");
//                }
//            }
//        };
//        set.put(preCon);
//        ret.put(set);
//        return ret;
//    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public Transferable drag() throws IOException {
        return NodeTransfer.createPaste(this);
    }

    @Override
    public PasteType[] types(final Node target) {
        final TermReportDataObject targetData = target.getLookup().lookup(TermReportDataObject.class);
        if (targetData != null) {
            final TermReportDataObject data = getLookup().lookup(TermReportDataObject.class);
            class PasteToNumberAssessment extends PasteType {

                private final TargetAssessmentDelegate delegate;

                private PasteToNumberAssessment(TargetAssessmentDelegate d) {
                    delegate = d;
                }

                @Override
                public String getName() {
                    return delegate.getOriginal().getDisplayName();
                }

                @Override
                public Transferable paste() throws IOException {
                    final URI link = data.getPrimaryFile().toURI();
                    final XmlFileTargetAssessmentProvider prov = XmlFileTargetAssessmentProvider.create(link, targetData.getLookup().lookup(TermReportActions.class).getContext(), delegate.getOriginal().getId());
                    targetData.getLookup().lookup(TermReportActions.class).addAssessmentProvider(prov);
                    return null;
                }

            }
            return data.getLookup().lookupAll(TargetAssessmentDelegate.class).stream()
                    .map(d -> new PasteToNumberAssessment(d))
                    .toArray(PasteType[]::new);
        }
        return new PasteType[]{};
    }
}
