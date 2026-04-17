/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.openide.util.NbBundle;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;
import org.thespheres.betula.services.vcard.VCardStudentsCollection;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.schulconnex.SchulconnexImportConfiguration;
import org.thespheres.betula.schulconnex.SchulconnexKlasseItem;
import org.thespheres.betula.schulconnex.SchulconnexStudentItem;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdaterDescriptions;
import org.thespheres.betula.xmlimport.utilities.UpdaterFilter;
import org.thespheres.betula.xmlimport.utilities.VCardStudentsUtil;

/**
 * Schulconnex updater for primary units (classes).
 */
@NbBundle.Messages({
    "SchulconnexPrimaryUnitsUpdater.message.updateVCards.network=Schüler/-innen werden importiert ...",
    "SchulconnexPrimaryUnitsUpdater.message.updateVCards.finish=Es wurden {0} Schüler/-innen in {1} ms importiert.",
    "SchulconnexPrimaryUnitsUpdater.message.updateVCards.skip=VCard-Import übersprungen: Für die Schulconnex-Klassen sind noch keine Schüler-VCard-Daten verfügbar."
})
public class SchulconnexPrimaryUnitsUpdater extends TargetItemsUpdater<SchulconnexKlasseItem> {

    private static JAXBContext jaxb;
    private final SchulconnexImportConfiguration configuration;

    static JAXBContext getJAXB() {
        if (jaxb == null) {
            try {
                jaxb = JAXBContext.newInstance(VCardStudentsCollection.class);
            } catch (JAXBException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return jaxb;
    }

    public SchulconnexPrimaryUnitsUpdater(final SchulconnexKlasseItem[] items,
            final WebServiceProvider provider,
            final Term current,
            final List<UpdaterFilter> filters,
            final SchulconnexImportConfiguration config,
            final TargetItemsUpdaterDescriptions descriptions) {
        super(items, provider, current, filters, descriptions);
        configuration = config;
    }

    @Override
    protected void dumpContainer(Container container, WebServiceProvider provider) {
        super.dumpContainer(container, provider);
        final VCardStudentsCollection map = createCollection();
        dumpVCards("vcard-import", map);
    }

    protected void dumpVCards(final String baseName, final VCardStudentsCollection map) {
        final String nbuser = System.getProperty("netbeans.user");
        if (nbuser != null) {
            final Path dir = Paths.get(nbuser, "var/log");
            final Path backup2 = dir.resolve(baseName + ".2.xml");
            final Path backup1 = dir.resolve(baseName + ".1.xml");
            final Path target = dir.resolve(baseName + ".xml");
            if (Files.exists(backup1)) {
                try {
                    Files.copy(backup1, backup2, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    PlatformUtil.getCodeNameBaseLogger(AbstractUpdater.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                }
            }
            if (Files.exists(target)) {
                try {
                    Files.copy(target, backup1, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    PlatformUtil.getCodeNameBaseLogger(AbstractUpdater.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                }
            }
            try (final OutputStream os = Files.newOutputStream(target)) {
                final Marshaller m = getJAXB().createMarshaller();
                m.setProperty("jaxb.formatted.output", Boolean.TRUE);
                m.marshal(map, os);
            } catch (IOException | JAXBException ex) {
                PlatformUtil.getCodeNameBaseLogger(AbstractUpdater.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
        }
    }

    @Override
    protected Exception callService(final ContainerBuilder builder) throws MissingResourceException {
        final Exception ex = updateVCards();
        if (ex != null) {
            return ex;
        }
        final Exception updateEx = super.callService(builder);
        try {
            final VCardStudents sbs = VCardStudentsUtil.findFromConfiguration(configuration);
            sbs.forceReload();
        } catch (IOException ioex) {
            ioex.printStackTrace(ImportUtil.getIO().getErr());
        }
        return updateEx;
    }

    private Exception updateVCards() {
        final String start = NbBundle.getMessage(SchulconnexPrimaryUnitsUpdater.class,
                "SchulconnexPrimaryUnitsUpdater.message.updateVCards.network");
        ImportUtil.getIO().getOut().println(start);
        final long ts = System.currentTimeMillis();

        final VCardStudentsCollection collection = createCollection();
        if (collection.size() == 0) {
            // TODO Schulconnex: map selected person data to VCard entries and fill collection.
            final String skip = NbBundle.getMessage(SchulconnexPrimaryUnitsUpdater.class,
                    "SchulconnexPrimaryUnitsUpdater.message.updateVCards.skip");
            ImportUtil.getIO().getOut().println(skip);
            return null;
        }

        final URI uri = VCardStudentsUtil.findStudentsURI(configuration);
        try {
            final Marshaller m = getJAXB().createMarshaller();
            HttpUtilities.post(provider, uri, os -> {
                try {
                    m.marshal(collection, os);
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                }
            }, null);
        } catch (JAXBException | IOException ex) {
            return ex;
        }

        final long dur = System.currentTimeMillis() - ts;
        final String finish = NbBundle.getMessage(SchulconnexPrimaryUnitsUpdater.class,
                "SchulconnexPrimaryUnitsUpdater.message.updateVCards.finish",
                collection.size(), dur);
        ImportUtil.getIO().getOut().println(finish);
        return null;
    }

    private VCardStudentsCollection createCollection() {
        final VCardStudentsCollection ret = new VCardStudentsCollection();
        Arrays.stream(items)
                .map(SchulconnexKlasseItem.class::cast)
                .filter(SchulconnexKlasseItem::isValid)
                .filter(this::checkImportTargetsItem)
                .forEach(ik -> {
                    final UnitId u = ik.getUnitId();
                    if (u != null) {
                        ik.getImportStudents().stream()
                                .filter(isi -> checkStudent(ik, u, isi.getStudentId()))
                                .filter(SchulconnexStudentItem::isSelected)
                                .filter(SchulconnexStudentItem::isVCardUpdated)
                                .distinct()
                                .forEach(isi -> ret.put(isi.getStudentId(), isi.getVCard()));
                    }
                });
        return ret;
    }
}
