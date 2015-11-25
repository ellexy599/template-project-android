package com.template.project.core.request.websocket;

import android.content.Context;

import com.template.project.core.AppConfiguration;
import com.template.project.core.request.ssl.SslSocketFactory;
import com.template.project.core.utils.LogMe;

import org.apache.http.conn.scheme.SocketFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class WsClient extends WebSocketClient {

    private static final String TAG = WsClient.class.getSimpleName();

    private static WsClient sWsClient;
    private static WsServerType sWsServerType = WsServerType.DEV;
    private static Context sContext;
    private static String sUserAgent;

    static {
        sUserAgent = System.getProperty("http.agent");
    }

    public WsClient(URI serverUri, Draft draft, Map<String, String> headers) {
        super(serverUri, draft, headers, 0);
    }

    public static WsClient getInstance(Context ctx) {
        sContext = ctx;
        try {
            URI uri = new URI(AppConfiguration.HOST);
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", sUserAgent);
            if (sWsClient == null) {
                sWsClient = new WsClient(uri, new Draft_17(), headers);
                // Initialize with default SSL keystore to be used
                SocketFactory socketFactory = new SslSocketFactory(ctx, sWsServerType);
                sWsClient.setSocket(socketFactory.createSocket());
            }
        } catch (Exception e) {
            LogMe.e(TAG, "WsClient newInstance ERROR: " + e.toString());
            sWsClient = null;
        }
        return sWsClient;
    }

    /** Change the server type Wsclient will connect to. */
    public WsClient setServerType(WsServerType wsServerType) {
        sWsServerType = wsServerType;
        SocketFactory socketFactory = new SslSocketFactory(sContext, sWsServerType);
        try {
            sWsClient.setSocket(socketFactory.createSocket());
        } catch (Exception e) {
            LogMe.e(TAG, "WsClient setServerType ERROR: " + e.toString());
        }
        return sWsClient;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }

}
