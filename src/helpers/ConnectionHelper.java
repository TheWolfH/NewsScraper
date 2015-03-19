package helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class ConnectionHelper {
	public static String getURLContent(URL url, Map<String, String> headers) throws IOException {
		URLConnection con = url.openConnection();

		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				con.addRequestProperty(entry.getKey(), entry.getValue());
			}
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

		StringBuilder sb = new StringBuilder();
		String line;

		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}

		reader.close();
		return sb.toString();
	}

	public static String getURLContent(String url) throws IOException {
		return ConnectionHelper.getURLContent(new URL(url.replaceAll(" ", "%20")), null);
	}
	
	public static String getURLContent(String url, Map<String, String> headers) throws IOException {
		return ConnectionHelper.getURLContent(new URL(url.replaceAll(" ", "%20")), headers);
	}
	
	public static String getURLContent(String url, String headerName, String headerValue) throws IOException {
		Map<String, String> headers = new HashMap<String,String>();
		headers.put(headerName, headerValue);
		return ConnectionHelper.getURLContent(new URL(url.replaceAll(" ", "%20")), headers);
	}
}
