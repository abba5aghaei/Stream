package com.abba5aghaei.stream.aspect;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import com.abba5aghaei.stream.tool.Toast;

public class HTTPS {

	public static String post(String destination, String params) {
		return request(destination, params, "POST");
	}

	public static String get(String destination, String params) {
		return request(destination, params, "GET");
	}

	private static String request(String destination, String params, String method) {
		String json = "";
		try {
			URL url = new URL("https://"+destination);
			trustAll();
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod(method);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", String.valueOf(params.length()));
			connection.setDoOutput(true);
			connection.getOutputStream().write(params.getBytes());
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null)
				json += line;
			reader.close();
			if (json.charAt(0) != '{')
				json = json.substring(json.indexOf("{"), json.length());
		}
		catch (Exception e) {
			Toast.make("Connection failed");
			try {
				json = "{'message': 'Failed'}";
			}
			catch (Exception ex) {}
		}
		return json;
	}

	private static void trustAll() {
		try {
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			SSLContext context;
			context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[] { new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			} }, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
		}
		catch (NoSuchAlgorithmException | KeyManagementException e) {
			Toast.make(e.getMessage());
		}
	}
}