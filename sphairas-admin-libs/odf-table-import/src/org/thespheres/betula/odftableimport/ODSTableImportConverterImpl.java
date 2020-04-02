/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.odftableimport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import org.odftoolkit.odfdom.doc.OdfDocument;
import org.openide.util.NbBundle;
import org.thespheres.betula.tableimport.TableImportConverter;
import org.thespheres.betula.tableimport.csv.XmlCsvFile;
import org.thespheres.betula.tableimport.csv.XmlCsvParserSettings;

/**
 *
 * @author boris.heithecker
 */
class ODSTableImportConverterImpl extends TableImportConverter {

    static final String STYLESHEET = "openOfficeCalcExport.xsl";
    private final TransformerFactory factory;
    private final Templates templates;
    private final JAXBContext jaxb;

    ODSTableImportConverterImpl() throws Exception {
        super(ODSFileDataObject.MIME, NbBundle.getMessage(ODSFileDataObject.class, "ODSFileDataObject.displayName"));
        factory = TransformerFactory.newInstance();
        final InputStream xslt = ODSTableImportConverterImpl.class.getResourceAsStream(STYLESHEET);
        templates = factory.newTemplates(new StreamSource(xslt));
        jaxb = JAXBContext.newInstance(XmlCsvParserSettings.class, XmlCsvFile.class, XmlCsvFile.XmlCsvFiles.class);
    }

    @Override
    public XmlCsvFile[] load(final File file, final String ignored) throws IOException {
        final DOMResult res;
        try {
            res = doConvert(file, null, null);
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            } else {
                throw new IOException(ex);
            }
        }
        final Object um;
        try {
            um = jaxb.createUnmarshaller().unmarshal(new DOMSource(res.getNode()));
        } catch (JAXBException jaxbex) {
            throw new IOException(jaxbex);
        }
        if (um instanceof XmlCsvFile) {
            return new XmlCsvFile[]{(XmlCsvFile) um};
        }
        return ((XmlCsvFile.XmlCsvFiles) um).getFiles();
    }

    protected DOMResult doConvert(final File file, final Map<String, String> params, final URIResolver uriResolver) throws Exception {
//        OdfPackage aInputPkg = OdfPackage.loadPackage(aInputFile);
        final OdfDocument d = OdfDocument.loadDocument(file);
        final DOMSource source = new DOMSource(d.getContentDom());
        //class ODFURIResolver implements URIResolver  by Oracle
//        URIResolver aURIResolver = new ODFURIResolver(aInputPkg, aInputFile.toURI().toString(), "content.xml");
        //
        final DOMResult result = new DOMResult();

//        boolean bError = runXSLT(aStyleSheetFile, aParams, aInputSource, aOutputResult,
//                aTransformerFactoryClassName, aURIResolver);
        final Transformer transformer = templates.newTransformer();

        if (params != null && !params.isEmpty()) {
            params.entrySet().forEach(e -> transformer.setParameter(e.getKey(), e.getValue()));
        }
        if (uriResolver != null) {
            transformer.setURIResolver(uriResolver);
        }
        transformer.transform(source, result);

        return result;
    }

//    private boolean runXSLT(File aStyleSheetFile,
//            List<XSLTParameter> aParams,
//            InputSource aInputInputSource, Result aOutputTarget,
//            String aTransformerFactoryClassName,
//            URIResolver aURIResolver) {
//        InputStream aStyleSheetInputStream = null;
//        try {
//            aStyleSheetInputStream = new FileInputStream(aStyleSheetFile);
//        } catch (FileNotFoundException e) {
//            return true;
//        }
//
//        InputSource aStyleSheetInputSource = new InputSource(aStyleSheetInputStream);
//        aStyleSheetInputSource.setSystemId(aStyleSheetFile.getAbsolutePath());
//
//        XMLReader aStyleSheetXMLReader = null;
//        XMLReader aInputXMLReader = null;
//        try {
//            aStyleSheetXMLReader = XMLReaderFactory.createXMLReader();
//            aInputXMLReader = XMLReaderFactory.createXMLReader();
//        } catch (SAXException e) {
//            return true;
//        }
//
//        aStyleSheetXMLReader.setErrorHandler(new SAXErrorHandler(aLogger));
//        aInputXMLReader.setErrorHandler(new SAXErrorHandler(aLogger));
//        aInputXMLReader.setEntityResolver(new ODFEntityResolver(aLogger));
//
//        Source aStyleSheetSource = new SAXSource(aStyleSheetXMLReader, aStyleSheetInputSource);
//        Source aInputSource = new SAXSource(aInputXMLReader, aInputInputSource);
//
//        return false;
//    }
}
