/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.docfile;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.spi.project.LookupProvider;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.services.ui.KeyStores;
import org.thespheres.betula.services.ui.xml.XmlBeforeSaveCallback;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author boris.heithecker
 */
public class SignContainerSupport implements XmlBeforeSaveCallback {

    public static final String ATTR_ON_SAVE_SIGN_CONTAINER_KEY_NAME = "on.save.sign.container.key.name";
    private final XMLDataObject data;

    private SignContainerSupport(XMLDataObject xmlDOb) {
        this.data = xmlDOb;
    }

    @Override
    public void run(Lookup context, Document document) throws IOException {
        final Object attr = data.getPrimaryFile().getAttribute(ATTR_ON_SAVE_SIGN_CONTAINER_KEY_NAME);
        final String name = attr instanceof String ? StringUtils.trimToNull((String) attr) : null;
        if (data.isValid() && name != null) {
            final Document d;
            try {
                d = data.getDocument();
            } catch (SAXException ex) {
                throw new IOException(ex);
            }

//            KeyStores.init();
            char[] password = null;
            Path kspath = Paths.get(KeyStores.getKeystore());
            PrivateKey key;
            X509Certificate cert;
            try {
                KeyStore kstore = KeyStore.getInstance(KeyStores.getKeystoreType());
                password = Keyring.read(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY);
                if (password == null) {
                    password = KeyStores.showUserKeyStorePasswordDialog();
                }
                kstore.load(Files.newInputStream(kspath, StandardOpenOption.READ), password);
                cert = (X509Certificate) kstore.getCertificate(name);
                key = (PrivateKey) kstore.getKey(name, password);
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | ClassCastException ex) {
                throw new IOException(ex);
            } finally {
                if (password != null) {
                    Arrays.fill(password, '0');
                }
            }

            if (key == null || cert == null) {
//Log
//Notification
                return;
            }

            DOMSignContext dsc = new DOMSignContext(key, d.getDocumentElement());

            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
            try {
                Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA1, null),
                        Collections.singletonList(fac.newTransform(Transform.ENVELOPED,
                                (TransformParameterSpec) null)), null, null);
                SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
                        (C14NMethodParameterSpec) null),
                        fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                        Collections.singletonList(ref));

                KeyInfoFactory kif = fac.getKeyInfoFactory();
                KeyValue kv = kif.newKeyValue(cert.getPublicKey());

//                X509Data xd = kif.newX509Data(Lists.newArrayList(cert.getSubjectX500Principal().getName(), cert));
                X509IssuerSerial issuer = kif.newX509IssuerSerial(cert.getIssuerX500Principal().getName(), cert.getSerialNumber());
                X509Data xd = kif.newX509Data(Lists.newArrayList(cert.getSubjectX500Principal().getName(), issuer));
                KeyInfo ki = kif.newKeyInfo(Lists.newArrayList(kv, xd));

                XMLSignature signature = fac.newXMLSignature(si, ki);
                synchronized (d) {
                    signature.sign(dsc);
                }

            } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | KeyException | MarshalException | XMLSignatureException ex) {
                throw new IOException(ex);
            }
        }
    }

    @Override
    public int position() {
        return Integer.MAX_VALUE;
    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/text/betula-document-container+xml/Lookup")
    public static class Registration implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup baseContext) {
            final XMLDataObject xmlDOb = baseContext.lookup(XMLDataObject.class);
            return xmlDOb == null ? Lookup.EMPTY : Lookups.singleton(new SignContainerSupport(xmlDOb));
        }

    }
}
