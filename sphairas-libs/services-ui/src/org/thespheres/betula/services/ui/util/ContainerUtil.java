/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.NbBundle;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.services.ui.docfile.SignContainerSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author boris.heithecker
 */
public class ContainerUtil {

    private static JAXBContext JAXB;

    private ContainerUtil() {
    }

    public static JAXBContext getJAXB() {
        if (JAXB == null) {
            try {
                JAXB = JAXBContext.newInstance(Container.class);
            } catch (JAXBException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return JAXB;
    }

    public static void write(final Container container, final Path destination) throws IOException {
        Marshaller m;
        try {
            m = getJAXB().createMarshaller();
            m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
        try (final OutputStream os = Files.newOutputStream(destination)) {
            m.marshal(container, os);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    @NbBundle.Messages("ContainerFileWriter.message.success={0} geschrieben")
    public static void write(final Container container, final FileObject folder, final String filename, final String privateKeyAlias) throws IOException {
        try {
            final FileObject tempfo = FileUtil.getConfigFile("/Templates/Betula/containerTemplate.xml");
            final DataObject template = DataObject.find(tempfo);
            final DataFolder destfolder = DataFolder.findFolder(folder);
            final XMLDataObject dest = (XMLDataObject) template.createFromTemplate(destfolder, filename);
            final boolean signContainer = !StringUtils.isBlank(privateKeyAlias);
            dest.getPrimaryFile().setAttribute(SignContainerSupport.ATTR_ON_SAVE_SIGN_CONTAINER_KEY_NAME, signContainer ? privateKeyAlias : null);
            final Binder<Node> marshaller = getJAXB().createBinder(Node.class);
            marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
            final Document d = dest.getDocument();
            synchronized (d) {
                marshaller.updateXML(container, d.getDocumentElement());
            }
            dest.setModified(true);
            final AbstractSavable save = dest.getLookup().lookup(AbstractSavable.class);
            if (save != null) {
                if (signContainer) {
                    dest.addPropertyChangeListener(evt -> {
                        if (DataObject.PROP_MODIFIED.equals(evt.getPropertyName()) && !dest.isModified()) {
                            try {
                                dest.getPrimaryFile().setAttribute(SignContainerSupport.ATTR_ON_SAVE_SIGN_CONTAINER_KEY_NAME, null);
                            } catch (IOException ex) {
                            }
                        }
                    });
                }
                save.save();
                final String msg = NbBundle.getMessage(FileTargetAssessmentExport.class, "ContainerFileWriter.message.success", dest.getPrimaryFile().getPath());
                StatusDisplayer.getDefault().setStatusText(msg);
            }
        } catch (JAXBException | SAXException ex) {
            throw new IOException(ex);
        }
    }
    //        tae.getHints().putAll(td.getProcessorHints());

}
