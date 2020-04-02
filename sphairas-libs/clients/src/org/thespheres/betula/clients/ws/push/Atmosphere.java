/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.ws.push;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.providers.grizzly.GrizzlyAsyncHttpProvider;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import javax.net.ssl.SSLContext;
import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.Event;
import org.atmosphere.wasync.Request;
import org.atmosphere.wasync.Socket;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.atmosphere.wasync.impl.AtmosphereRequest;
import org.atmosphere.wasync.impl.DefaultOptions;
import org.thespheres.betula.services.WebProvider;

/**
 *
 * @author boris.heithecker
 */
class Atmosphere {

    private static final HashMap<Key, Object> SERVICES = new HashMap<>();

    static PushNotificationServiceImpl findPushNotificationService(String provider, String servletUrl) throws IOException {
        final Key key = new Key(provider, servletUrl);
        final Object value;
        synchronized (SERVICES) {
            value = SERVICES.computeIfAbsent(key, k -> create(k));
        }
        if (value instanceof IOException) {
            throw (IOException) value;
        }
        return (PushNotificationServiceImpl) value;
    }

    private static Object create(Key key) {
        try {
            final AsyncHttpClientConfig.Builder b = new AsyncHttpClientConfig.Builder();
            boolean useSsl = key.pushUrl.startsWith("https");
            if (useSsl) {
                final WebProvider.SSL sw = WebProvider.find(key.provider, WebProvider.SSL.class);
                final SSLContext sslContext = sw.getSSLContext();
                b.setSSLContext(sslContext);
            }

            final AsyncHttpClientConfig ahcConfig = b.build();

//            final char[] p = Keyring.read(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY);
//            SSLContextConfigurator.DEFAULT_CONFIG.setKeyStorePass(p);
//            SSLContextConfigurator.DEFAULT_CONFIG.setTrustStorePass(new String(p));
            final AsyncHttpClient ahc2 = new AsyncHttpClient(new GrizzlyAsyncHttpProvider(ahcConfig), ahcConfig);

//            Arrays.fill(p, '0');
//            SSLContextConfigurator.DEFAULT_CONFIG.setKeyStorePass(p);
//            SSLContextConfigurator.DEFAULT_CONFIG.setTrustStorePass(new String(p));
            final AtmosphereClient client = ClientFactory.getDefault().newClient(AtmosphereClient.class);

            final AtmosphereRequest requestBuilder = client.newRequestBuilder()
                    .method(Request.METHOD.GET)
                    .uri(key.pushUrl)
                    //                .uri("http://xxxxxxxxxxxx.net:8080/web/primepush/document-updates/xxxxxxxxxxxxxxxxxxx")
                    //                .trackMessageLength(true)
                    //                .transport(Request.TRANSPORT.WEBSOCKET)
                    //                .transport(Request.TRANSPORT.STREAMING)

                    .cache(AtmosphereRequest.CACHE.SESSION_BROADCAST_CACHE) //Important to set!!!!!!!!!!!!                   

                    .trackMessageLength(true)
                    //                    .transport(Request.TRANSPORT.WEBSOCKET) //Remove if not works
                    .transport(Request.TRANSPORT.LONG_POLLING)
                    //                .decoder(new PaddingAndHeartbeatDecoder())

                    //                    .decoder(new DocumentMessageDecoder())
                    .build();

//        DefaultOptions o = client.newOptionsBuilder().runtime(ahc).build();
            final DefaultOptions o = client.newOptionsBuilder()
                    .waitBeforeUnlocking(2000)
                    .reconnectAttempts(3)
                    .runtime(ahc2)
                    .build();

            final Socket socket = client.create(o);
//        Socket socket = client.create();

            final PushNotificationServiceImpl ret = new PushNotificationServiceImpl();
//            socket.on(Event.MESSAGE, new OnMessage(ret));
            socket.on(Event.MESSAGE, ret);
//            socket.on(new FunctionImpl());
            socket.open(requestBuilder);
            return ret;
        } catch (IOException ioex) {
            return ioex;
        } catch (RuntimeException ex) {
            return new IOException(ex);
        }
    }

    private static class Key {

        private final String provider;
        private final String pushUrl;

        Key(String provider, String pushUrl) {
            this.provider = provider;
            this.pushUrl = pushUrl;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + Objects.hashCode(this.provider);
            hash = 59 * hash + Objects.hashCode(this.pushUrl);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;
            if (!Objects.equals(this.provider, other.provider)) {
                return false;
            }
            return Objects.equals(this.pushUrl, other.pushUrl);
        }

    }

}
