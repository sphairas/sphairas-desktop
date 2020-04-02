/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import org.jdesktop.swingx.renderer.StringValue;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportTargetFactory;
import org.thespheres.betula.xmlimport.model.Product;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
public class ImportProviderComboBoxModel<I extends ImportTarget> extends DefaultComboBoxModel<ImportTargetFactory<I>.ProviderRef> implements StringValue {

    private final Map<String, WeakReference<I>> available = new HashMap<>();

    public I findTarget() {
        final ImportTargetFactory<I>.ProviderRef f = (ImportTargetFactory<I>.ProviderRef) getSelectedItem();
        I p = null;
        if (f != null) {
            final String key = f.getProvider();
            synchronized (available) {
                if (!available.containsKey(key) || (p = available.get(key).get()) == null) {
                    try {
                        p = f.createInstance();
                        available.put(key, new WeakReference<>(p));
                    } catch (IOException ex) {
                        log(ex, f.getFactory().getProduct(), f.getProvider());
                        removeElement(f);
                        return null;
                    }
                }
            }
        }
        return p;
    }

    public void setSelectedTarget(I p) {
        final Product product = p.getProduct();
        setSelectedTarget(product, p.getProviderInfo().getURL());
    }

    public void setSelectedTarget(Product p, String provider) {
        for (int i = 0; i < getSize(); i++) {
            final ImportTargetFactory<I>.ProviderRef f = getElementAt(i);
            if (f != null && f.getFactory().getProduct().equals(p) && f.getProvider().equals(provider)) {
                setSelectedItem(f);
                break;
            }
        }
    }

    @Override
    public String getString(Object v) {
        return v == null ? "" : ProviderRegistry.getDefault().get(((ImportTargetFactory<I>.ProviderRef) v).getProvider()).getDisplayName();
    }

    @Messages({"ImportProviderComboBoxModel.log=Die Import-Konfiguration {0} für {1} konnte nicht geladen werden.",
        "ImportProviderComboBoxModel.log.title=Konfigurationsfehler"})
    private void log(IOException ex, Product prod, String provider) {
        final ProviderInfo info = ProviderRegistry.getDefault().get(provider);
        Logger.getLogger(ImportProviderComboBoxModel.class.getCanonicalName()).log(LogLevel.INFO_WARNING, ex.getMessage(), ex);
        final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        final String title = NbBundle.getMessage(ImportProviderComboBoxModel.class, "ImportProviderComboBoxModel.log.title");
        final String message = NbBundle.getMessage(ImportProviderComboBoxModel.class, "ImportProviderComboBoxModel.log", prod.getDisplay(), info.getDisplayName());
        NotificationDisplayer.getDefault()
                .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }
}
