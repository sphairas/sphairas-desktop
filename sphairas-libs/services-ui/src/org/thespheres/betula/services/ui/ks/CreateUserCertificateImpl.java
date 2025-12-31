/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.ks;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

/**
 *
 * @author boris.heithecker
 */
public class CreateUserCertificateImpl {

    private final KeyPair keyPair;
//    private final sun.security.x509.X500Name x500Name;
    private final X500Name x500Name;
//    static final String SIGNATURE_ALGORITH = "MD5WithRSA";
    static final String KEY_ALGORITHM = "RSA";
    static final long VALIDITIY_MONTHS = 1l;
// UPDATED: MD5 is insecure. Updated to SHA256.
    static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";
    static final long VALIDITY_MONTHS = 1L;

    public CreateUserCertificateImpl(X500Principal principal) throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        kpg.initialize(4096);
        keyPair = kpg.generateKeyPair();
        x500Name = X500Name.getInstance(principal.getEncoded());
    }

//    public Certificate getSelfCertificate() throws IOException {
//        try {
//            return getSelfCertificateImpl();
//        } catch (Exception ex) {
//            throw new IOException(ex);
//        }
//    }
    // Like above, plus a CertificateExtensions argument, which can be null.
//    private X509Certificate _getSelfCertificateImpl() throws CertificateException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
//        LocalDate ldn = LocalDate.now();
//        LocalDate ldv = ldn.plusMonths(VALIDITIY_MONTHS);
//        CertificateValidity interval = new CertificateValidity(Date.from(ldn.atStartOfDay(ZoneId.systemDefault()).toInstant()),
//                Date.from(ldv.atStartOfDay(ZoneId.systemDefault()).toInstant()));
//        X509CertInfo info = new X509CertInfo();
//        // Add all mandatory attributes
//        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
//        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new java.util.Random().nextInt() & 0x7fffffff));
//        AlgorithmId algID = AlgorithmId.get(SIGNATURE_ALGORITH);
//        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algID));
//        info.set(X509CertInfo.SUBJECT, x500Name);
//        info.set(X509CertInfo.KEY, new CertificateX509Key(keyPair.getPublic()));
//        info.set(X509CertInfo.VALIDITY, interval);
//        info.set(X509CertInfo.ISSUER, x500Name);
//        X509CertImpl cert = new X509CertImpl(info);
//        cert.sign(keyPair.getPrivate(), SIGNATURE_ALGORITH);
    ////                // update und neu zeichnen
////        algo = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
////        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
////        cert = new X509CertImpl(info);
////        cert.sign(pair.getPrivate(), kpg.getAlgorithm());
//        return (X509Certificate) cert;
//    }

    private X509Certificate getSelfCertificate() throws Exception {
        LocalDate ldn = LocalDate.now();
        LocalDate ldv = ldn.plusMonths(VALIDITY_MONTHS);

        Date notBefore = Date.from(ldn.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date notAfter = Date.from(ldv.atStartOfDay(ZoneId.systemDefault()).toInstant());
        BigInteger serial = new BigInteger(64, new SecureRandom());

        // 1. Create the Certificate Builder
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                x500Name, // Issuer (Self)
                serial, // Serial
                notBefore, // Not Before
                notAfter, // Not After
                x500Name, // Subject
                keyPair.getPublic() // Public Key
        );

        // 2. Build the Content Signer
        ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
                .build(keyPair.getPrivate());

        // 3. Build the Certificate Holder
        X509CertificateHolder holder = builder.build(signer);

        // 4. Convert to standard Java X509Certificate
        return new JcaX509CertificateConverter().getCertificate(holder);
    }

    // Changed return type from sun...PKCS10 to Bouncy Castle equivalent
//    public PKCS10CertificationRequest getCertRequest() throws IOException {
//        try {
//            return getCertRequestImpl();
//        } catch (OperatorCreationException ex) {
//            throw new IOException(ex);
//        }
//    }

//    public PKCS10 getCertRequest() throws IOException {
//        try {
//            return getCertRequestImpl();
//        } catch (NoSuchAlgorithmException | InvalidKeyException | CertificateException | SignatureException ex) {
//            throw new IOException(ex);
//        }
//    }
//    public void _writeCertRequest(final Path out) throws IOException {
//        try {
//            final PKCS10 pkcs10 = getCertRequestImpl();
//            final ByteArrayOutputStream bs = new ByteArrayOutputStream();
//            final PrintStream ps = new PrintStream(bs, true, "utf-8");
//            try {
//                pkcs10.print(ps);
//            } catch (SignatureException ex) {
//                throw new IOException(ex);
//            }
//            Files.write(out, bs.toByteArray());
//        } catch (NoSuchAlgorithmException | InvalidKeyException | CertificateException | SignatureException ex) {
//            throw new IOException(ex);
//        }
//    }
    public void writeCertRequest(final Path out) throws OperatorCreationException, IOException {
        String pemString = PKCS10ToString(getCertRequest());
        Files.write(out, pemString.getBytes("UTF-8"));
    }

    public PKCS10CertificationRequest getCertRequest() throws OperatorCreationException {
        // 1. Create CSR Builder
        JcaPKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(
                x500Name,
                keyPair.getPublic()
        );

        // 2. Create Signer
        ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
                .build(keyPair.getPrivate());

        // 3. Build CSR
        return builder.build(signer);
    }

//    private PKCS10 _getCertRequestImpl() throws NoSuchAlgorithmException, InvalidKeyException, CertificateException, IOException, SignatureException {
//        PKCS10 req = new PKCS10(keyPair.getPublic());
//        Signature signature = Signature.getInstance(SIGNATURE_ALGORITH);
//        signature.initSign(keyPair.getPrivate());
//        req.encodeAndSign(x500Name, signature);
//        return req;
//    }
//    public static String _PKCS10ToString(PKCS10 pkcs10) throws IOException {
//        final ByteArrayOutputStream bs = new ByteArrayOutputStream();
//        final PrintStream ps = new PrintStream(bs, true, "utf-8");
//        try {
//            pkcs10.print(ps);
//        } catch (SignatureException ex) {
//            throw new IOException(ex);
//        }
//        return bs.toString("utf-8");
//    }
// Updated to accept BC object
    public static String PKCS10ToString(PKCS10CertificationRequest pkcs10) throws IOException {
        StringWriter sw = new StringWriter();
        // Use Bouncy Castle PEMWriter to handle encoding cleanly
        try (PemWriter pw = new PemWriter(sw)) {
            pw.writeObject(new PemObject("NEW CERTIFICATE REQUEST", pkcs10.getEncoded()));
        }
        return sw.toString();
    }

    public PrivateKeyEntry getEntry() throws Exception {
        return new PrivateKeyEntry(keyPair.getPrivate(), new Certificate[]{getSelfCertificate()});
    }
}
