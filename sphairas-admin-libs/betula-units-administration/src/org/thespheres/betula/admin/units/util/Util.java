package org.thespheres.betula.admin.units.util;

import com.google.common.eventbus.AsyncEventBus;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringJoiner;
import javax.swing.Icon;
import javax.xml.ws.WebServiceException;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.Identity;
import org.thespheres.betula.document.ExceptionMessage;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ws.ServiceException;

/**
 *
 * @author boris.heithecker
 */
public class Util {

    public static final int RP_THROUGHPUT = 64; //256;
    AsyncEventBus b;//TODO: 
    public static final RequestProcessor NOTIFICATIONS = new RequestProcessor("remote-units-notifications", 32);
    private static final HashMap<String, RequestProcessor> RP = new HashMap<>();

    public static RequestProcessor RP(String key) {
        return RP.computeIfAbsent(key, k -> {
            return new RequestProcessor(key, RP_THROUGHPUT, true);
        });
    }

    public static boolean isServiceException(Throwable th) {
        boolean found = false;
        while (!found && th != null) {
            found = th instanceof ServiceException
                    || th instanceof WebServiceException
                    || th instanceof IOException;
            th = th.getCause();
        }
        return found;
    }

    public static Exception serviceExceptionOrThrow(final RuntimeException rex) {
        Throwable cause = rex;
        while (true) {
            if (isServiceException((cause))) {
                return (Exception) cause;
            } else if (cause == null) {
                throw rex;
            } else {
                cause = cause.getCause();
            }
        }
    }

    @NbBundle.Messages(value = {"Util.processException.message=Beim Verarbeiten von {0} ist auf dem Server ein Fehler aufgetretn.",
        "Util.processException.header====== Server-Fehler =====",
        "Util.processException.finisher====== Ende ====="})
    public static void processException(Template<?> t, Identity key) throws IOException {
        final ExceptionMessage pre = t.getException();
        if (pre == null) {
            return;
        }
        final StringJoiner sj = new StringJoiner("/n");
        sj.add(NbBundle.getMessage(Util.class, "Util.processException.header"));
        sj.add(NbBundle.getMessage(Util.class, "Util.processException.message", key != null ? key.toString() : "?"));
        sj.add(pre.getUserMessage());
        sj.add(pre.getLogMessage());
        sj.add(pre.getStackTraceElement());
        sj.add(NbBundle.getMessage(Util.class, "Util.processException.finisher"));
        throw new IOException(sj.toString());
    }

    @NbBundle.Messages(value = {"Util.error.title=Fehler bei der Übertragung",
        "Util.error.message=Beim Übertragen von {0} an {1} ist ein Fehler aufgetreten. Siehe sphairas-log für mehr Informationen."})
    public static void notify(final Identity key, final ProviderInfo provider) {
        final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        final String title = NbBundle.getMessage(Util.class, "Util.error.title");
        final String message = NbBundle.getMessage(Util.class, "Util.error.message", key != null ? key.getId().toString() : "null", provider != null ? provider.getDisplayName() : "unknown");
        NotificationDisplayer.getDefault()
                .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }
}
