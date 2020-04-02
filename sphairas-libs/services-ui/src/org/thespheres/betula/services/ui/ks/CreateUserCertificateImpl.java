/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.ks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import sun.security.pkcs10.PKCS10;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

/**
 *
 * @author boris.heithecker
 */
public class CreateUserCertificateImpl {

    private final KeyPair keyPair;
    private final sun.security.x509.X500Name x500Name;
    static final String SIGNATURE_ALGORITH = "MD5WithRSA";
    static final String KEY_ALGORITHM = "RSA";
    static final long VALIDITIY_MONTHS = 1l;

    public CreateUserCertificateImpl(X500Principal principal) throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        kpg.initialize(4096);
        keyPair = kpg.generateKeyPair();
        x500Name = sun.security.x509.X500Name.asX500Name(principal);
    }

    public Certificate getSelfCertificate() throws IOException {
        try {
            return getSelfCertificateImpl();
        } catch (CertificateException | InvalidKeyException | SignatureException | NoSuchAlgorithmException | NoSuchProviderException ex) {
            throw new IOException(ex);
        }
    }

    // Like above, plus a CertificateExtensions argument, which can be null.
    private X509Certificate getSelfCertificateImpl() throws CertificateException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        LocalDate ldn = LocalDate.now();
        LocalDate ldv = ldn.plusMonths(VALIDITIY_MONTHS);
        CertificateValidity interval = new CertificateValidity(Date.from(ldn.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                Date.from(ldv.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        X509CertInfo info = new X509CertInfo();
        // Add all mandatory attributes
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new java.util.Random().nextInt() & 0x7fffffff));
        AlgorithmId algID = AlgorithmId.get(SIGNATURE_ALGORITH);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algID));
        info.set(X509CertInfo.SUBJECT, x500Name);
        info.set(X509CertInfo.KEY, new CertificateX509Key(keyPair.getPublic()));
        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.ISSUER, x500Name);
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(keyPair.getPrivate(), SIGNATURE_ALGORITH);
//                // update und neu zeichnen
//        algo = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
//        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
//        cert = new X509CertImpl(info);
//        cert.sign(pair.getPrivate(), kpg.getAlgorithm());
        return (X509Certificate) cert;
    }

    public PKCS10 getCertRequest() throws IOException {
        try {
            return getCertRequestImpl();
        } catch (NoSuchAlgorithmException | InvalidKeyException | CertificateException | SignatureException ex) {
            throw new IOException(ex);
        }
    }

    public void writeCertRequest(final Path out) throws IOException {
        try {
            final PKCS10 pkcs10 = getCertRequestImpl();
            final ByteArrayOutputStream bs = new ByteArrayOutputStream();
            final PrintStream ps = new PrintStream(bs, true, "utf-8");
            try {
                pkcs10.print(ps);
            } catch (SignatureException ex) {
                throw new IOException(ex);
            }
            Files.write(out, bs.toByteArray());
        } catch (NoSuchAlgorithmException | InvalidKeyException | CertificateException | SignatureException ex) {
            throw new IOException(ex);
        }
    }

    private PKCS10 getCertRequestImpl() throws NoSuchAlgorithmException, InvalidKeyException, CertificateException, IOException, SignatureException {
        PKCS10 req = new PKCS10(keyPair.getPublic());
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITH);
        signature.initSign(keyPair.getPrivate());
        req.encodeAndSign(x500Name, signature);
        return req;
    }

    public static String PKCS10ToString(PKCS10 pkcs10) throws IOException {
        final ByteArrayOutputStream bs = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(bs, true, "utf-8");
        try {
            pkcs10.print(ps);
        } catch (SignatureException ex) {
            throw new IOException(ex);
        }
        return bs.toString("utf-8");
    }

    public PrivateKeyEntry getEntry() throws IOException {
        return new PrivateKeyEntry(keyPair.getPrivate(), new Certificate[]{getSelfCertificate()});
    }
}
