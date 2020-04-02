/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ws.api;

import com.sun.xml.ws.developer.JAXWSProperties;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.web.ContextCredentials;
import org.thespheres.betula.services.ws.BetulaWebService;

/**
 *
 * @author boris.heithecker
 */
public class BetulaServiceClient {

    private final static QName BETULASERVICE_QNAME = new QName("http://web.service.betula.thespheres.org/", "BetulaService");
    private final String endpoint;
    private final WebProvider.SSL ssl;
    private static Service service;
    static final java.lang.String JAXWS_HOSTNAME_VERIFIER = "com.sun.xml.internal.ws.transport.https.client.hostname.verifier";

    //We need a shared singleton Service, otherwise we get classloader issues
    protected Service getSharedService() {
        class ServiceImpl extends Service {

            private ServiceImpl() {
                super(getWsdlLoacation(), BETULASERVICE_QNAME);
            }

        }
        synchronized (BetulaServiceClient.class) {
            if (service == null) {
                service = new ServiceImpl();
            }
        }
        return service;
    }

    private static URL getWsdlLoacation() {
        return BetulaServiceClient.class.getResource("/META-INF/wsit-client.xml");
    }

    public BetulaServiceClient(String endpointAddress, WebProvider.SSL ssl) { //URL wsdlLocation) {
//        super(getWsdlLoacation(), BETULASERVICE_QNAME);
        this.endpoint = endpointAddress;
        this.ssl = ssl;
    }

    public BetulaWebService getBetulaServicePort() {
        return getBetulaServicePort(null);
    }

    public BetulaWebService getBetulaServicePort(ContextCredentials creds) {
        BetulaWebService port;
//        BetulaWebService port = super.getPort(new QName("http://web.service.betula.thespheres.org/", "BetulaServicePort"), BetulaWebService.class);
        synchronized (BetulaServiceClient.class) {//without sycnchronization --> LinkageError if two WebServices

//            port = getSharedService().getPort(new QName("http://web.service.betula.thespheres.org/", "BetulaServicePort"), BetulaWebService.class, new UsesJAXBContextFeature(new BetulaJAXBContextFactory()));
            port = getSharedService().getPort(new QName("http://web.service.betula.thespheres.org/", "BetulaServicePort"), BetulaWebService.class);
        }
        configureBinding((BindingProvider) port);
        if (creds != null) {
            if (creds.getUsername() != null) {
                ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, creds.getUsername());
            }
            final char[] pw;
            if ((pw = creds.getPassword()) != null) {
                ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, String.valueOf(pw));
                for (int i = 0; i < pw.length;) {
                    pw[i++] = 0;
                }
            }
//            List<Handler> hc = ((BindingProvider) port).getBinding().getHandlerChain();
        }
        return port;
    }

    private void configureBinding(BindingProvider port) {
        port.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
        //            BindingProviderProperties.CONNECT_TIMEOUT
//                    BindingProviderProperties.REQUEST_TIMEOUT;
        port.getRequestContext().put("com.sun.xml.ws.request.timeout", 300000);
        port.getRequestContext().put("com.sun.xml.ws.connect.timeout", 3000);
//                System.setProperty("sun.net.client.defaultConnectTimeout", "5000");
//        System.setProperty("sun.net.client.defaultReadTimeout", "20000");
        port.getRequestContext().put("ocm.sun.xml.internal.ws.request.timeout", 300000);
        port.getRequestContext().put("com.sun.xml.internal.ws.connect.timeout", 3000);
        if (secureEndpoint() && ssl != null) {
            final SSLSocketFactory sf = ssl.getSSLContext().getSocketFactory();
            //glassfish
            port.getRequestContext().put("com.sun.xml.ws.transport.https.client.SSLSocketFactory", sf);
            //no glassfish
            port.getRequestContext().put("com.sun.xml.internal.ws.transport.https.client.SSLSocketFactory", sf); //ssl.getSSLContext().getSocketFactory());
            final HostnameVerifier hostnameVerifier = ssl.getHostnameVerifier();
            if (hostnameVerifier != null) {
                String HOSTNAME_VERIFIER = JAXWSProperties.HOSTNAME_VERIFIER;
                String SSL_SOCKET_FACTORY = JAXWSProperties.SSL_SOCKET_FACTORY;
//                port.getRequestContext().put(JAXWS_HOSTNAME_VERIFIER, hostnameVerifier);
                port.getRequestContext().put(JAXWSProperties.HOSTNAME_VERIFIER, hostnameVerifier);
            }
        }
    }

    protected boolean secureEndpoint() {
        return endpoint.startsWith("https");
    }

//    void isGF() {
//        Modules m;
//    }
}
