/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl;

import java.awt.event.ActionListener;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Optional;
import org.netbeans.api.actions.Closable;
import org.netbeans.api.actions.Openable;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author boris
 */
public final class ZeugnisBemerkungenEnv implements Serializable, Lookup.Provider, Openable, Closable {

    public static final String MIME = "application/betula-report-notes-editor";
    private static final ZeugnisBemerkungenEnv INSTANCE = new ZeugnisBemerkungenEnv();
    private final transient CloneableTopComponent.Ref tcid = new CloneableTopComponent.Ref() {
    };

    private ZeugnisBemerkungenEnv() {
    }

    @ActionRegistration(displayName = "#ZeugnisBemerkungenEnv.openAction")
    @ActionID(category = "Window", id = "org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.ZeugnisBemerkungenEnv.openAction")
    @ActionReference(path = "Menu/Window/betula-beans-services-windows", position = 1050)
    @NbBundle.Messages({"ZeugnisBemerkungenEnv.openAction=Zeugnisbemerkungen"})
    public static ActionListener openAction() {
        return evt -> INSTANCE.open();
    }

    public static ZeugnisBemerkungenEnv getInstance() {
        return INSTANCE;
    }

    @Override
    public void open() {
        CloneableTopComponent tc;
        synchronized (tcid) {
            tc = tcid.getArbitraryComponent();
            if (tc == null) {
                tc = MultiViews.createCloneableMultiView(MIME, INSTANCE);
                final CloneableTopComponent ftc = tc;
                Optional.ofNullable(WindowManager.getDefault().findMode("output"))
                        .ifPresent(m -> m.dockInto(ftc));
                tc.setReference(tcid);
            }
        }
        tc.open();
        tc.requestActive();
    }

    @Override
    public boolean close() {
        if (tcid.isEmpty()) {
            return true;
        }

        return Mutex.EVENT.writeAccess((Mutex.Action<Boolean>) () -> {
            synchronized (tcid) {
                final java.util.Enumeration<CloneableTopComponent> en = tcid.getComponents();
                while (en.hasMoreElements()) {
                    final TopComponent c = en.nextElement();
                    if (!c.close()) {
                        return false;
                    }
                }
            }
            return true;
        });
    }

    @Override
    public Lookup getLookup() {
        return Lookups.singleton(this);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }

}
