/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author boris.heithecker
 */
public class IOUtil {

    public static final RequestProcessor RP = new RequestProcessor(IOUtil.class.getName(), 3);

    @Messages({"IOUtil.ioTab.title=Mitteilungen"})
    public static InputOutput getIO() {
        return IOProvider.getDefault().getIO(NbBundle.getMessage(IOUtil.class, "IOUtil.ioTab.title"), false);
    }

    @Messages({"IOUtil.ioEditTab.title=Neue Mitteilung"})
    public static InputOutput getEditIO(ActionListener l) {
        Action[] acc = new Action[]{new SendAction(l)};
        return IOProvider.getDefault().getIO(NbBundle.getMessage(IOUtil.class, "IOUtil.ioEditTab.title"), acc);
    }

    private final static class SendAction extends AbstractAction {

        private final WeakReference<ActionListener> delegate;

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        private SendAction(ActionListener l) {
            super("send-message");
            this.delegate = new WeakReference(l);
            Icon icon = ImageUtilities.loadImageIcon("org/thespheres/acer/remote/ui/resources/mail-send.png", true);
            putValue(Action.SMALL_ICON, icon);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (delegate.get() != null) {
                delegate.get().actionPerformed(e);
            }
        }

    }
}
