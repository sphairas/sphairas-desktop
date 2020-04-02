/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport;

import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Mutex;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.MutexException;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.PasteType;
import org.thespheres.betula.termreport.model.XmlNumberAssessmentProvider;

/**
 *
 * @author boris.heithecker
 * @param <T>
 */
public class AssessmentProviderNode<T extends AssessmentProvider> extends AbstractNode implements PropertyChangeListener, NodeTransfer.Paste {

    protected final T provider;

    @SuppressWarnings({"OverridableMethodCallInConstructor",
        "LeakingThisInConstructor"})
    public AssessmentProviderNode(T p) {
        super(Children.LEAF, p.getLookup());
        this.provider = p;
        super.setName(p.getId());
        setDisplayName(provider.getDisplayName());
        setIconBaseWithExtension("org/thespheres/betula/termreport/resources/table-insert-column.png");
        PropertyChangeListener pcl = WeakListeners.propertyChange(this, p);
        p.addPropertyChangeListener(pcl);
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
        try {
            Mutex.EVENT.writeAccess((ExceptionAction) (() -> {
                provider.remove();
                return null;
            }));
        } catch (MutexException ex) {
            Exception e = ex.getException();
            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw new IOException(e);
            }
        }
//        provider.remove();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (AssessmentProvider.PROP_DISPLAYNAME.equals(evt.getPropertyName())) {
            setDisplayName(provider.getDisplayName());
        }
    }

    @Override
    public Transferable drag() throws IOException {
        return NodeTransfer.createPaste(this);
    }

    @Override
    public PasteType[] types(Node target) {
        final XmlNumberAssessmentProvider nap = target.getLookup().lookup(XmlNumberAssessmentProvider.class);
        if (nap != null) {
            class PasteToNumberAssessment extends PasteType {

                @Override
                public Transferable paste() throws IOException {
                    if (!nap.contains(provider)) {
                        nap.addReference(provider);
                    }
                    return null;
                }

            }
            return new PasteType[]{new PasteToNumberAssessment()};
        }
        return new PasteType[]{};
    }
}
