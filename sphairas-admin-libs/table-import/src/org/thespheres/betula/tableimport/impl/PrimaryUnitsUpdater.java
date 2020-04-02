/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.impl;

import org.thespheres.betula.services.ui.util.dav.VCardStudents;
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
import org.thespheres.betula.services.vcard.VCardStudentsCollection;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdaterDescriptions;
import org.thespheres.betula.xmlimport.utilities.UpdaterFilter;
import org.thespheres.betula.xmlimport.utilities.VCardStudentsUtil;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"PrimaryUnitsUpdater.message.updateVCards.network=Schülerinnen/Schüler werden importiert ...",
    "PrimaryUnitsUpdater.message.updateVCards.finish=Es wurden {0} Schülerinnen/Schüler in {1} ms importiert."})
class PrimaryUnitsUpdater extends TargetItemsUpdater<PrimaryUnitsXmlCsvItem> {

    private static JAXBContext jaxb;
    private final ConfigurableImportTarget configuration;

    PrimaryUnitsUpdater(PrimaryUnitsXmlCsvItem[] impKurse, WebServiceProvider provider, Term current, List<UpdaterFilter> filters, ConfigurableImportTarget config, TargetItemsUpdaterDescriptions descriptions) {
        super(impKurse, provider, current, filters, descriptions);
        configuration = config;
    }

    @Override
    protected Exception callService(ContainerBuilder builder) throws MissingResourceException {
        //update VCards first, so a new student's name can always be resolved
        //otherwise table immed. after update show only id number.
//        updateVCards();

        Exception ex = updateVCards2();
        if (ex != null) {
            return ex;
        }
        ex = super.callService(builder);
        try {
            final VCardStudents sbs = VCardStudentsUtil.findFromConfiguration(configuration);
            sbs.forceReload();
        } catch (IOException ioex) {
            ioex.printStackTrace(ImportUtil.getIO().getErr());
        }
        return ex;
    }

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

    private Exception updateVCards2() {
        String msg2 = NbBundle.getMessage(PrimaryUnitsUpdater.class, "PrimaryUnitsUpdater.message.updateVCards.network");
        ImportUtil.getIO().getOut().println(msg2);
        long ts = System.currentTimeMillis();
        final VCardStudentsCollection map = createCollection();
        final URI uri = VCardStudentsUtil.findStudentsURI(configuration);

        try {
            final Marshaller m = getJAXB().createMarshaller();
            HttpUtilities.post(provider, uri, os -> {
                try {
                    m.marshal(map, os);
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                }
            }, null);
        } catch (JAXBException | IOException ex) {
            return ex;
        }

        long dur = System.currentTimeMillis() - ts;
        msg2 = NbBundle.getMessage(PrimaryUnitsUpdater.class, "PrimaryUnitsUpdater.message.updateVCards.finish", map.size(), dur);
        ImportUtil.getIO().getOut().println(msg2);
        return null;
    }

    private VCardStudentsCollection createCollection() {
        final VCardStudentsCollection map = new VCardStudentsCollection();
        Arrays.stream(items)
                .filter(PrimaryUnitsXmlCsvItem::isValid)
                .filter(this::checkImportTargetsItem)
                .forEach(ik -> {
                    final UnitId u = ik.getUnitId();
                    if (u != null) {
                        ik.getImportStudents().values().stream()
                                .filter(isi -> checkStudent(ik, u, isi.getStudentId()))
                                .filter(PrimaryUnitImportStudentItem::isVCardUpdated)
                                .distinct()
                                .forEach(isi -> map.put(isi.getStudentId(), isi.getVCard()));
                    }
                });
        return map;
    }

    @Override
    protected void dumpContainer(Container container, WebServiceProvider provider) {
        super.dumpContainer(container, provider);
        final VCardStudentsCollection map = createCollection();
        dumpVCards(map);
    }

    protected void dumpVCards(final VCardStudentsCollection map) {

        final String nbuser = System.getProperty("netbeans.user");
        if (nbuser != null) {
            final Path dir = Paths.get(nbuser, "var/log");
            final Path backup2 = dir.resolve("vcard-import.2.xml");
            final Path backup1 = dir.resolve("vcard-import.1.xml");
            final Path target = dir.resolve("vcard-import.xml");
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
//        final FileObject folder = getDumpContainerFolder();
//        if (folder != null) {
//            final String filename = FileUtil.findFreeFileName(folder, "vcard-import", "xml");
//            final LocalProperties prop = LocalProperties.find(provider.getInfo().getURL());
//            final String privateKeyAlias = AppProperties.privateKeyAlias(prop, provider.getInfo().getURL());
//            try {
//                final Marshaller m = getJAXB().createMarshaller();
//                      m.marshal(map, os);
//            } catch (IOException ex) {
//                ex.printStackTrace(getErrorWriter());
//            }
//        }
    }
}
