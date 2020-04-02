package org.thespheres.betula.services.implementation.ui.build;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import org.openide.xml.XMLUtil;
import org.thespheres.betula.adminconfig.ConfigurationBuildTask;
import org.thespheres.betula.adminconfig.layerxml.LayerFileSystem;
import org.thespheres.betula.services.implementation.ui.impl.SyncedProviderInstance;
import org.thespheres.betula.services.implementation.ui.layerxml.LayerFileSystemImpl;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author boris.heithecker
 */
public class LayerUpdater extends AbstractResourceUpdater {

    public static final String LAYER_XML_FILE = "layer.xml";
    private static JAXBContext JAXB;
    protected final BiConsumer<ConfigurationBuildTask, LayerFileSystem> agent;
    private final Binder<Node> binder;
    private Document dom;

    LayerUpdater(final SyncedProviderInstance instance, final BiConsumer<ConfigurationBuildTask, LayerFileSystem> agent) {
        super(instance, LAYER_XML_FILE, null);
        this.agent = agent;
        binder = getJAXB().createBinder();
    }

    static JAXBContext getJAXB() {
        synchronized (LayerUpdater.class) {
            if (JAXB == null) {
                try {
                    JAXB = JAXBContext.newInstance(LayerFileSystemImpl.class);
                } catch (JAXBException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return JAXB;
        }
    }

    @Override
    protected void runUpdates() throws IOException {
        lock(10);
        boolean updated = instance.enqueue(-1);
        if (!updated) {
            throw new IOException("Cannot update layer.xml because provider synchronization is disabled.");
        }
        final LayerFileSystemImpl fs = readLayer();
        modifyLayer(fs);
        backupLayer();
        put(fs, false);
        unlock();
    }

    protected void backupLayer() throws IOException {
        HttpUtilities.copy(web, resolveResource(LAYER_XML_FILE), LAYER_XML_FILE + ".bak");
    }

    protected LayerFileSystemImpl readLayer() throws IOException {
        final Path layerPath = instance.getBaseDir().resolve(LAYER_XML_FILE);
        try (final InputStream is = Files.newInputStream(layerPath)) {
//            final SAXParserFactory spf = SAXParserFactory.newInstance();
//            final XMLReader xmlReader = spf.newSAXParser().getXMLReader();
//            xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); // This may not be strictly required as DTDs shouldn't be allowed at all, per previous line.
//            xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
//            xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            final InputSource inputSource = new InputSource(is);
//            final SAXSource source = new SAXSource(xmlReader, inputSource);
            dom = XMLUtil.parse(inputSource, false, true, null, null);
            final JAXBElement<LayerFileSystemImpl> el = binder.unmarshal(dom, LayerFileSystemImpl.class);

            return el.getValue();
        } catch (JAXBException | SAXException ex) {
            throw new IOException(ex);
        }
    }

    protected void put(final LayerFileSystemImpl fs, final boolean dryRun) throws IOException {
        final byte[] content;
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream(10000)) {
//            final Marshaller m = getJAXB().createMarshaller();
//            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//            m.setProperty("com.sun.xml.internal.bind.xmlHeaders",
//                    "\n<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.2//EN\" \"http://www.netbeans.org/dtds/filesystem-1_2.dtd\">");
//            m.marshal(fs, baos);
//            binder.marshal(fs, dom.getDocumentElement());

//            LayerFile f = fs.getFile("/TestDir/test.txt", false);
//            LayerFile f2 = fs.getFile("/Provider/jupiter.instance", false);
            binder.updateXML(fs);
//            binder.updateJAXB(dom);
//            LayerFolder fo = fs.findChildFolder("TestDir", false);
//binder.updateXML(fo);
//            Element e = (Element) binder.getXMLNode(f2);
//            Comment comment = dom.createComment("This is a comment");
//            Node insertBefore = e.appendChild(comment);    // insertBefore(comment, e);
//            String nodeName = e.getNodeName();
            final String now = LocalDateTime.now().toString();
            Comment comment2 = dom.createComment("Modified: " + now);
            dom.getDocumentElement().appendChild(comment2);
            XMLUtil.write(dom, baos, "UTF-8");
//            write(dom, baos, "UTF-8");
            content = baos.toByteArray();
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
        dumpLayer(content);
        if (!dryRun) {
            HttpUtilities.put(web, resolveResource(LAYER_XML_FILE), content, null, getLockToken());
        }
    }

    protected void modifyLayer(final LayerFileSystemImpl fs) throws IOException {
        try {
            agent.accept(this, fs);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    protected void dumpLayer(final byte[] file) {
        final String nbuser = System.getProperty("netbeans.user");
        if (nbuser != null) {
            final Path dir = Paths.get(nbuser, "var/log");
            final Path target = dir.resolve("layer-update.xml");
            try {
                Files.write(target, file);
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(LayerUpdater.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
        }
    }
//    private static final String IDENTITY_XSLT_WITH_INDENT
//            = "<xsl:stylesheet version='1.0' "
//            + // NOI18N
//            "xmlns:xsl='http://www.w3.org/1999/XSL/Transform' "
//            + // NOI18N
//            "xmlns:xalan='http://xml.apache.org/xslt' "
//            + // NOI18N
//            "exclude-result-prefixes='xalan'>"
//            + // NOI18N
//            "<xsl:output method='xml' indent='yes' xalan:indent-amount='4'/>"
//            + // NOI18N
//            "<xsl:template match='@*|node()'>"
//            + // NOI18N
//            "<xsl:copy>"
//            + // NOI18N
//            "<xsl:apply-templates select='@*|node()'/>"
//            + // NOI18N
//            "</xsl:copy>"
//            + // NOI18N
//            "</xsl:template>"
//            + // NOI18N
//            "</xsl:stylesheet>"; // NOI18N

//    public static void write(Document doc, OutputStream out, String enc) throws IOException {
//        if (enc == null) {
//            throw new NullPointerException("You must set an encoding; use \"UTF-8\" unless you have a good reason not to!"); // NOI18N
//        }
//        Document doc2 = doc; //normalize(doc);
//        ClassLoader orig = Thread.currentThread().getContextClassLoader();
//        Thread.currentThread().setContextClassLoader(AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() { // #195921
//            @Override
//            public ClassLoader run() {
//                return new ClassLoader(ClassLoader.getSystemClassLoader().getParent()) {
//                    @Override
//                    public InputStream getResourceAsStream(String name) {
//                        if (name.startsWith("META-INF/services/")) {
//                            return new ByteArrayInputStream(new byte[0]); // JAXP #6723276
//                        }
//                        return super.getResourceAsStream(name);
//                    }
//                };
//            }
//        }));
//        try {
//            TransformerFactory tf = TransformerFactory.newInstance();
//            Transformer t = tf.newTransformer(
//                    new StreamSource(new StringReader(IDENTITY_XSLT_WITH_INDENT)));
////                        Transformer t = tf.newTransformer();#
//
//            DocumentType dt = doc2.getDoctype();
//            if (dt != null) {
//                String pub = dt.getPublicId();
//                if (pub != null) {
//                    t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, pub);
//                }
//                String sys = dt.getSystemId();
//                if (sys != null) {
//                    t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, sys);
//                }
//            }
//            t.setOutputProperty(OutputKeys.ENCODING, enc);
//            try {
//                t.setOutputProperty(OutputKeys.STANDALONE, "yes");
//            } catch (IllegalArgumentException x) {
//                // fine, introduced in JDK 7u4
//            }
//
//            // See #123816
//            Set<String> cdataQNames = new HashSet<String>();
//            collectCDATASections(doc2, cdataQNames);
//            if (cdataQNames.size() > 0) {
//                StringBuilder cdataSections = new StringBuilder();
//                for (String s : cdataQNames) {
//                    cdataSections.append(s).append(' '); //NOI18N
//                }
//                t.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, cdataSections.toString());
//            }
//
//            Source source = new DOMSource(doc2);
////            javax.xml.transform.Result result = new StreamResult(out);
//
////            TransformerHandler transformHandler = ((SAXTransformerFactory) SAXTransformerFactory.newInstance()).newTransformerHandler();
//////            OutputStream output = new FileOutputStream(OUTPUT_FILENAME);
////            transformHandler.setResult(new StreamResult(out));
////            SAXResult saxResult = new SAXResult(transformHandler);
////            saxResult.setLexicalHandler(new DefaultHandler2());
////            t.transform(source, saxResult);
//            DOMImplementationRegistry domImplementationRegistry = DOMImplementationRegistry.newInstance();
//            DOMImplementationLS domImplementationLS = (DOMImplementationLS) domImplementationRegistry.getDOMImplementation("LS");
//            LSSerializer writer = domImplementationLS.createLSSerializer();
//
//            writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
//            writer.getDomConfig().setParameter("comments", Boolean.TRUE);
////            String result = writer.writeToString(element);
//
//            LSOutput createLSOutput = domImplementationLS.createLSOutput();
//            createLSOutput.setByteStream(out);
//            writer.write(doc2, createLSOutput);
//
////            t.transform(source, result);
//        } catch (Exception e) {
//            throw new IOException(e);
//        } finally {
//            Thread.currentThread().setContextClassLoader(orig);
//        }
//    }
//
//    private static void collectCDATASections(Node node, Set<String> cdataQNames) {
//        if (node instanceof CDATASection) {
//            Node parent = node.getParentNode();
//            if (parent != null) {
//                String uri = parent.getNamespaceURI();
//                if (uri != null) {
//                    cdataQNames.add("{" + uri + "}" + parent.getNodeName()); //NOI18N
//                } else {
//                    cdataQNames.add(parent.getNodeName());
//                }
//            }
//        }
//
//        NodeList children = node.getChildNodes();
//        for (int i = 0; i < children.getLength(); i++) {
//            collectCDATASections(children.item(i), cdataQNames);
//        }
//    }
//
//    @FunctionalInterface
//    public static interface LayerModifier<Result> {
//
//        public Result modifyLayer(LayerFileSystemImpl fs) throws Exception;
//    }
}
