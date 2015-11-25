package com.template.project.core.request.ssl;

import android.content.Context;

import com.template.project.core.R;
import com.template.project.core.request.websocket.WsServerType;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

public class SslSocketFactory implements SocketFactory, LayeredSocketFactory {

	private SSLContext sslContext = null;

	public SslSocketFactory(Context mCtx, WsServerType wsServerType) {
		
		// load up the key store
		final String STORETYPE = "BKS";
		final String STOREPASSWORD = "Your keystore password";
		final String KEYPASSWORD = "Your key password";
		final String keyMgrAlgo = KeyManagerFactory.getDefaultAlgorithm();

		InputStream iStream = null;
		
		try {
			int blumKeyStore = 0;
			switch (wsServerType) {
				case PROD:
					blumKeyStore = R.raw.prod_sample_key;
					break;

				case BETA:
					blumKeyStore = R.raw.beta_sample_key;
					break;

				case DEV:
					blumKeyStore = R.raw.dev_sample_key;
					break;
			}

			KeyStore keyStore = KeyStore.getInstance(STORETYPE);
			iStream = mCtx.getResources().openRawResource(blumKeyStore);
			keyStore.load(iStream, STOREPASSWORD.toCharArray());

			TrustManager[] trustManagers = { new CertX509TrustManager(keyStore) };
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(keyMgrAlgo);
			kmf.init(keyStore, KEYPASSWORD.toCharArray());

			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), trustManagers, null);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { iStream.close();
			} catch (Exception e) {}
		}
	}

	private SSLContext getSSLContext() throws IOException {
		return this.sslContext;
	}

	/**
	 * @see SocketFactory#connectSocket(Socket,
	 *      String, int, InetAddress, int,
	 *      HttpParams)
	 */
	public Socket connectSocket(Socket sock, String host, int port,
			InetAddress localAddress, int localPort, HttpParams params)

	throws IOException, UnknownHostException, ConnectTimeoutException {
		int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
		int soTimeout = HttpConnectionParams.getSoTimeout(params);
		InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
		SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock : createSocket());

		if ((localAddress != null) || (localPort > 0)) {
			// we need to bind explicitly
			if (localPort < 0) {
				localPort = 0; // indicates "any"
			}
			InetSocketAddress isa = new InetSocketAddress(localAddress,
					localPort);
			sslsock.bind(isa);
		}

		sslsock.connect(remoteAddress, connTimeout);
		sslsock.setSoTimeout(soTimeout);
		return sslsock;
	}

	/**
	 * @see SocketFactory#createSocket()
	 */
	public Socket createSocket() throws IOException {
		return getSSLContext().getSocketFactory().createSocket();
	}

	/**
	 * @see SocketFactory#isSecure(Socket)
	 */
	public boolean isSecure(Socket socket) throws IllegalArgumentException {
		return true;
	}

	/**
	 * @see LayeredSocketFactory#createSocket(Socket,
	 *      String, int, boolean)
	 */
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
	}

	// -------------------------------------------------------------------
	// javadoc in org.apache.http.conn.scheme.SocketFactory says :
	// Both Object.equals() and Object.hashCode() must be overridden
	// for the correct operation of some connection managers
	// -------------------------------------------------------------------

	public boolean equals(Object obj) {
		return ((obj != null) && obj.getClass().equals(
				SslSocketFactory.class));
	}

	public int hashCode() {
		return SslSocketFactory.class.hashCode();
	}

}
