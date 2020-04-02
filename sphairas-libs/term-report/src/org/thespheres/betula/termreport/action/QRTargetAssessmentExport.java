/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.action;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.plutext.jaxb.xslfo.Block;
import org.plutext.jaxb.xslfo.ExternalGraphic;
import org.plutext.jaxb.xslfo.Root;
import org.plutext.jaxb.xslfo.SimplePageMaster;
import org.thespheres.betula.Unit;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.listprint.Formatter;
import org.thespheres.betula.listprint.builder.RootBuilder;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.ui.util.ContainerUtil;
import org.thespheres.betula.services.ui.util.TargetAssessmentExport;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

/**
 *
 * @author boris.heithecker
 */
public class QRTargetAssessmentExport extends TargetAssessmentExport {

    private Path path;

    public QRTargetAssessmentExport(String provider, TargetAssessment<Grade, ?> assessment, Unit unit, DocumentId target, TermSchedule ts, NamingResolver nr, LocalProperties lp) {
        super(provider, assessment, unit, target, ts, nr, lp);
    }

    public void setFile(Path path) {
        this.path = path;
    }

    @Messages("QRTargetAssessmentExport.message.success={0} geschrieben")
    @Override
    protected void write(Container container) throws IOException {
        Formatter.getDefault().transform(createRoot(container), Files.newOutputStream(path), "application/pdf");
        final String msg = NbBundle.getMessage(QRTargetAssessmentExport.class, "QRTargetAssessmentExport.message.success", path.toString());
        StatusDisplayer.getDefault().setStatusText(msg);
    }
    //        tae.getHints().putAll(td.getProcessorHints());

    public Root createRoot(Container container) throws IOException {
        Block f = new Block();
        f.getContent().add(unit.getDisplayName());
        String displayName = "Display"; //UIUtilities.findDisplayName(env.getDataObject());
        RootBuilder rb = new RootBuilder(displayName, f) {
            @Override
            protected SimplePageMaster createSimplePageMaster() {
                SimplePageMaster spm = new SimplePageMaster();
                spm.setMasterName("master");
                spm.setPageHeight("29.7cm");
                spm.setPageWidth("21.0cm");
                spm.setMarginTop("2.0cm");
                spm.setMarginBottom("1.0cm");
                spm.setMarginLeft("2.0cm");
                spm.setMarginRight("1.7cm");
                return spm;
            }
        };

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (final XZOutputStream outxz = new XZOutputStream(baos, new LZMA2Options(LZMA2Options.PRESET_MAX))) {
            ContainerUtil.getJAXB().createMarshaller().marshal(container, outxz);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        } finally {
            baos.close();
        }

        String encode = Base64.getEncoder().encodeToString(baos.toByteArray());

        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        try {
            BitMatrix m = writer.encode(encode, BarcodeFormat.QR_CODE, 300, 300, hints);
//            BitMatrix m = writer.encode(encode, BarcodeFormat.QR_CODE, 500, 500, hints);
            MatrixToImageWriter.writeToStream(m, "PNG", baos2);
        } catch (WriterException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            baos2.close();
        }
        final String img = Base64.getEncoder().encodeToString(baos2.toByteArray());
        final Block add = new Block();
        ExternalGraphic eg = new ExternalGraphic();
        String src = "url('data:image/png;base64," + img + "')";
        eg.setSrc(src);
        add.getContent().add(eg);
        rb.addFlow(add);
        return rb.build();
    }

}
