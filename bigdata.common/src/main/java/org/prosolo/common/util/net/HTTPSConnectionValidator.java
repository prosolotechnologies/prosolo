package org.prosolo.common.util.net;

import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;
//import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * @author Zoran Jeremic Oct 16, 2014
 *
 */

public class HTTPSConnectionValidator {
	public static void checkIfHttpsConnection(HttpURLConnection connection) {
		if (connection instanceof HttpsURLConnection) {
			try {
				TrustManager[] tm = { new RelaxedX509TrustManager() };
				SSLContext sslContext = SSLContext.getInstance("SSL");
				sslContext.init(null, tm, new java.security.SecureRandom());
				SSLSocketFactory sf = sslContext.getSocketFactory();
				((HttpsURLConnection) connection).setSSLSocketFactory(sf);

			} catch (java.security.GeneralSecurityException e) {
				System.out.println("GeneralSecurityException: " + e.getLocalizedMessage());
			}
		}
	}
}
