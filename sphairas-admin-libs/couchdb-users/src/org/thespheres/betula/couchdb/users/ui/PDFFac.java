/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.couchdb.users.ui;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.plutext.jaxb.xslfo.Block;
import org.plutext.jaxb.xslfo.ExternalGraphic;
import org.plutext.jaxb.xslfo.Root;
import org.thespheres.betula.couchdb.users.impl.GenDBTask;
import org.thespheres.betula.listprint.PDFFactory;
import org.thespheres.betula.listprint.XSLFOException;
import org.thespheres.betula.listprint.builder.RootBuilder;

/**
 *
 * @author boris.heithecker
 */
class PDFFac implements PDFFactory {

    private final GenDBTask env;
    private final Path path;

    PDFFac(GenDBTask env, Path save) {
        this.env = env;
        this.path = save;
    }

    @Override
    public Root createRoot() throws XSLFOException {
        final String creds = env.getCoucDBUser().getUser() + ":" + env.getCoucDBUser().getPassword();
        final String baseUrl = env.getCouchDBAdminInstance().getBaseUrl();
        final String url = creds + "@" + baseUrl + env.getUserDB();
        Block f = new Block();
//        f.getContent().add(creds);
        f.getContent().add("URL: " + url);
        f.getContent().add("\n\r");
        f.getContent().add("Benutzername: " + env.getCoucDBUser().getUser());
        f.getContent().add("\n\r");
        f.getContent().add("Passwort: " + env.getCoucDBUser().getPassword());
        final String displayName = "CouchDB-Zugangsdaten"; //UIUtilities.findDisplayName(env.getDataObject());
        final RootBuilder rb = new RootBuilder(displayName, f);

        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            BitMatrix m = writer.encode(url, BarcodeFormat.QR_CODE, 300, 300, hints);
            MatrixToImageWriter.writeToStream(m, "PNG", baos);
        } catch (WriterException | IOException ex) {
            throw new XSLFOException(ex);
        } finally {
            try {
                baos.close();
            } catch (IOException ex) {
                throw new XSLFOException(ex);
            }
        }

        final String img = Base64.getEncoder().encodeToString(baos.toByteArray());
        final Block add = new Block();
        ExternalGraphic eg = new ExternalGraphic();
        String src = "url('data:image/png;base64," + img + "')";
        eg.setSrc(src);
        add.getContent().add(eg);
        rb.addFlow(add);
        return rb.build();
    }

    @Override
    public OutputStream getOutputStream(String mimeType) throws IOException {
        return Files.newOutputStream(path);
    }

}
