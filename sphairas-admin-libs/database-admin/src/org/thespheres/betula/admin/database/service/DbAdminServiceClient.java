/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.service;

import java.net.URL;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import org.thespheres.betula.services.WebProvider;

/**
 *
 * @author boris.heithecker
 */
public class DbAdminServiceClient {

    private final static QName DBADMINSERVICE_QNAME = new QName("http://dbadmin.service.betula.thespheres.org/", "DbAdminService");
    private final String endpoint;
    private final WebProvider.SSL ssl;
    private static Service service;

    //We need a shared singleton Service, otherwise we get classloader issues
    protected Service getSharedService() {
        class ServiceImpl extends Service {

            private ServiceImpl() {
                super(getWsdlLoacation(), DBADMINSERVICE_QNAME);
            }

        }
        synchronized (DbAdminServiceClient.class) {
            if (service == null) {
                service = new ServiceImpl();
            }
        }
        return service;
    }

    private static URL getWsdlLoacation() {
        return DbAdminServiceClient.class.getResource("/META-INF/wsit-client.xml");
    }

    public DbAdminServiceClient(String endpointAddress, WebProvider.SSL ssl) {
        this.endpoint = endpointAddress;
        this.ssl = ssl;
    }

    public DbAdminService getBetulaServicePort() {
        DbAdminService port;
//        BetulaWebService port = super.getPort(new QName("http://web.service.betula.thespheres.org/", "BetulaServicePort"), BetulaWebService.class);
        synchronized (DbAdminServiceClient.class) {//without sycnchronization --> LinkageError if two WebServices
            port = getSharedService().getPort(new QName("http://dbadmin.service.betula.thespheres.org/", "DbAdminServicePort"), DbAdminService.class);
        }
        configureBinding((BindingProvider) port);
        return port;
    }

    private void configureBinding(BindingProvider port) {
        port.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
        //            BindingProviderProperties.CONNECT_TIMEOUT
//                    BindingProviderProperties.REQUEST_TIMEOUT;
//        port.getRequestContext().put("com.sun.xml.ws.request.timeout", 300000);
//        port.getRequestContext().put("com.sun.xml.ws.connect.timeout", 3000);
//                System.setProperty("sun.net.client.defaultConnectTimeout", "5000");
//        System.setProperty("sun.net.client.defaultReadTimeout", "20000");
        port.getRequestContext().put("ocm.sun.xml.internal.ws.request.timeout", 600 * 1000);
        port.getRequestContext().put("com.sun.xml.internal.ws.connect.timeout", 3000);
        final SSLSocketFactory sf = ssl.getSSLContext().getSocketFactory();
        //glassfish
        port.getRequestContext().put("com.sun.xml.ws.transport.https.client.SSLSocketFactory", sf);
        //no glassfish
        port.getRequestContext().put("com.sun.xml.internal.ws.transport.https.client.SSLSocketFactory", sf); //ssl.getSSLContext().getSocketFactory());
    }

}
