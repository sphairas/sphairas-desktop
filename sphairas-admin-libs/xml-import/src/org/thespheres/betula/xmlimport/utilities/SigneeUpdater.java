/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import javax.xml.ws.WebServiceException;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.util.ContentValueEntry;
import org.thespheres.betula.services.ws.BetulaWebService;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.ServiceException;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.model.ImportSigneeItem;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
@Messages({"SigneeUpdater.message.start=Starte Unterzeichner-Update nach {0}.",
    "SigneeUpdater.message.network=... Netzwerk ...",
    "SigneeUpdater.message.finish=Es wurden {0} Unterzeichner in {1} ms nach {2} importiert."})
public class SigneeUpdater<I extends ImportSigneeItem> extends AbstractUpdater<I> {

    protected final ImportTarget config;
    protected final ThreadLocal<Long> timeStart = new ThreadLocal();
    protected final ThreadLocal<Integer> numImport = new ThreadLocal();
    private final SigneeUpdater.Filter filter;
    protected Exception exception;
    
    public SigneeUpdater(final ImportTarget config, final I[] items, final SigneeUpdater.Filter<I> f) {
        super(items);
        this.config = config;
        this.filter = f;
    }

    @Override
    public void run() {
        final String msg = NbBundle.getMessage(SigneeUpdater.class, "SigneeUpdater.message.start", config.getProviderInfo().getDisplayName());
        ImportUtil.getIO().getOut().println(msg);
        numImport.set(0);
        timeStart.set(System.currentTimeMillis());
        final ContainerBuilder builder = new ContainerBuilder();
        final Template rt = builder.createTemplate(null, null, null, Paths.SIGNEES_PATH, null, Action.FILE);
        Arrays.stream(items)
                .filter(i -> filter == null || filter.accept(i))
                .forEach(s -> {
                    final ContentValueEntry<Signee> cve = ContentValueEntry.create(s.getSignee(), Signee.class, null, s.getName());
//                    Entry<Signee, String> entry = new Entry<>(null, s.getSignee());
//                    entry.setValue(s.getName());
//                    rt.getChildren().add(entry);
                    if (s.getMarkers() != null) {
                        final Set<Marker> ms = cve.getValue().getMarkerSet();
                        Arrays.stream(s.getMarkers())
                                .forEach(ms::add);
                    }
                    rt.getChildren().add(cve);
                    numImport.set(numImport.get() + 1);
                });

        dumpContainer(builder.getContainer(), config.getWebServiceProvider());

        if (!isDryRun()) {
            exception = callService(builder);
        }
    }

    protected Exception callService(final ContainerBuilder builder) {
        final BetulaWebService service;
        try {
            final WebServiceProvider p = config.getWebServiceProvider();
            service = p.createServicePort();
        } catch (IOException ex) {
            ex.printStackTrace(ImportUtil.getIO().getErr());
            return ex;
        }
        Container response = null;
        try {
            final String msg2 = NbBundle.getMessage(SigneeUpdater.class, "SigneeUpdater.message.network");
            ImportUtil.getIO().getOut().println(msg2);
            response = service.solicit(builder.getContainer());
            long dur = System.currentTimeMillis() - timeStart.get();
            final String msg3 = NbBundle.getMessage(SigneeUpdater.class, "SigneeUpdater.message.finish", numImport.get(), dur, config.getProviderInfo().getDisplayName());
            ImportUtil.getIO().getOut().println(msg3);
        } catch (Exception ex) {
            if (ex instanceof ServiceException || ex instanceof WebServiceException) {
                if (getErrorWriter() != null) {
                    getErrorWriter().println(ex.getLocalizedMessage());
                    ex.printStackTrace(getErrorWriter());
                }
                return ex;
            }
        }
        AbstractUpdater.handleExceptions(response);
        return null;
    }

    @FunctionalInterface
    public static interface Filter<I extends ImportSigneeItem> {

        public boolean accept(final I item);
    }
}
