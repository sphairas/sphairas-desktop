package org.thespheres.betula.tableimport.action;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.openide.WizardDescriptor;
import org.openide.xml.XMLUtil;
import org.thespheres.betula.tableimport.csv.XmlCsvDictionary;
import org.thespheres.betula.tableimport.csv.XmlCsvFile;
import org.thespheres.betula.tableimport.csv.XmlCsvFileGroupingKey;
import org.thespheres.betula.tableimport.csv.XmlCsvUtil;
import org.thespheres.betula.ui.util.JAXBUtil;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.model.XmlImport;
import org.thespheres.betula.xmlimport.model.XmlItem;
import org.thespheres.betula.xmlimport.model.XmlStudentItem;
import org.thespheres.betula.xmlimport.model.XmlTargetEntryItem;
import org.thespheres.betula.xmlimport.uiutil.DefaultImportWizardSettings;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
public abstract class XmlCsvImportSettings<T extends ImportItem> extends DefaultImportWizardSettings<ConfigurableImportTarget, T> {

    protected static final TransformerFactory FACTORY = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
    private static final JAXBContext JAXB;
    protected static final JAXBContext DICTJAXB;
    private final XmlCsvFile[] csv;
    private ConfigurableImportTarget config;
    private boolean group;

    static {
        try {
            final Class[] types = JAXBUtil.lookupJAXBTypes("XmlImport", XmlImport.class, XmlTargetEntryItem.class, XmlStudentItem.class);
            JAXB = JAXBContext.newInstance(types);
            DICTJAXB = JAXBContext.newInstance(XmlCsvDictionary.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    protected XmlCsvImportSettings(final XmlCsvFile[] file, final boolean group) {
        this.csv = file;
        this.group = group;
    }

    public XmlCsvFile[] getXmlCsv() {
        return csv;
    }

    @Override
    public void initialize(WizardDescriptor panel) throws IOException {
        super.initialize(panel);
    }

    protected ConfigurableImportTarget getConfiguration() {
        return config;
    }

    public boolean useGrouping() {
        return group;
    }

    public void setGrouping(boolean group) {
        this.group = group;
    }

    public boolean allowSelectUseGrouping() {
        return false;
    }

    public abstract WizardDescriptor.Iterator<XmlCsvImportSettings<T>> createIterator(WizardDescriptor.Panel<XmlCsvImportSettings<T>> firstPanel);

    synchronized void initialize(ConfigurableImportTarget newConfig) throws IOException {
        if (!Objects.equals(config, newConfig)) {
            config = newConfig;
            for (XmlCsvFile f : csv) {
                final XmlCsvDictionary d = createDictionary();
                f.setDictionary(d);
            }
            reload();
        }
    }

    public synchronized void reload() throws IOException {
        Arrays.stream(csv)
                .forEach(f -> XmlCsvUtil.assignKeys(f, f.getDictionary()));
        //new String[]{"source-unit", "source-subject", "source-signee", "source-level"}
        if (useGrouping()) {
            Arrays.stream(csv)
                    .forEach(XmlCsvFileGroupingKey::group);
        }
        final ChangeSet<T> cs = getSelectedNodesProperty();
        cs.clear();
        if (config != null) {
            for (XmlCsvFile f : csv) {
                final XmlImport xi = load(f);
                xi.getItems().stream()
                        .map(i -> createItem(i, csv.length > 1 ? f.getId() : null))
                        .filter(Objects::nonNull)
                        .forEach(cs::add);
            }
        } else {
            cs.clear();
        }
    }

    public XmlCsvDictionary createDictionary() throws IOException {
        return null;
    }

    protected abstract T createItem(XmlItem i, String optionalCsvFileId);

    protected XmlImport load(final XmlCsvFile f) throws IOException {
        final DOMResult result = new DOMResult();
        try {
            XmlCsvUtil.JAXB.createMarshaller().marshal(f, result);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }

        //Write XmlCsvFile
        dump((Document) result.getNode(), "csv-import.xml");
        //

//        //
//        final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
//        try {
//            final Marshaller m = XmlCsvUtil.JAXB.createMarshaller();
//            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//            m.marshal(f, baos2);
//        } catch (JAXBException ex) {
//            throw new IOException(ex);
//        }
//        ImportUtil.getIO().getOut().println(new String(baos2.toByteArray(), Charset.defaultCharset()));
//        //
        final Node n = loadAndTransform(result.getNode());
        dump((Document) n, "csv-import-transformed.xml");
        try {
            return (XmlImport) JAXB.createUnmarshaller().unmarshal(n);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    protected void dump(final Document node, final String file) {
        final String nbuser = System.getProperty("netbeans.user");
        if (nbuser != null) {
            final Path dir = Paths.get(nbuser, "var/log");
            final Path f = dir.resolve(file);
            try (final OutputStream os = Files.newOutputStream(f)) {
                XMLUtil.write(node, os, Charset.defaultCharset().name());
            } catch (IOException ioex) {
                final String msg = "An exception has ocurred dumping " + file + ".";
                PlatformUtil.getCodeNameBaseLogger(XmlCsvImportSettings.class).log(Level.SEVERE, msg, ioex);
            }
        }
    }

    protected Node loadAndTransform(Node source) throws IOException {
        try {
            final Transformer transformer = getTemplate().newTransformer();
            transformer.setParameter("versionParam", "2.0");
//                transformer.setParameter("student.authority", config.getp);
//            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            configureTransformer(transformer);
            final Source src = new DOMSource(source);
            final DOMResult res = new DOMResult();
            transformer.transform(src, res);
            return res.getNode();
        } catch (TransformerException tex) {
            throw new IOException(tex);
        }
    }

    protected abstract Templates getTemplate();

    protected void configureTransformer(final Transformer transformer) {
        transformer.setParameter("authority", config.getAuthority());
    }

}
