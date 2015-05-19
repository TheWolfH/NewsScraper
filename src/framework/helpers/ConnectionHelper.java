package framework.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to read entire web pages' HTML.
 * 
 * @author Jan Helge Wolf
 *
 */
public class ConnectionHelper {
	/**
	 * Returns the entire source code of the page specified by {@code url}. When
	 * making the HTTP request, the headers passed in {@code headers} (Map
	 * linking header name to header content) are added.
	 * 
	 * @param url
	 *            the URL to call
	 * @param headers
	 *            the headers to pass when making the HTTP request, or
	 *            {@code null}
	 * @return the entire HTML of the specified URL
	 * @throws IOException
	 *             in case of any networking error
	 */
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

	/**
	 * Proxy method for {@link #getURLContent(URL, Map)}, creates URL object
	 * from passed string, no headers are set when making the HTTP request.
	 * 
	 * @see #getURLContent(URL, Map)
	 * @param url the URL to call
	 * @return the entire HTML of the specified page
	 * @throws IOException in case of any networking error
	 */
	public static String getURLContent(String url) throws IOException {
		return ConnectionHelper.getURLContent(new URL(url.replaceAll(" ", "%20")), null);
	}

	/**
	 * Proxy method for {@link #getURLContent(URL, Map)}, creates URL object
	 * from passed string, setting the appropriate headers when making the HTTP request.
	 * 
	 * @see #getURLContent(URL, Map)
	 * @param url the URL to call
	 * @param headers the headers to pass when making the HTTP request, or
	 *            {@code null}
	 * @return the entire HTML of the specified page
	 * @throws IOException in case of any networking error
	 */
	public static String getURLContent(String url, Map<String, String> headers) throws IOException {
		return ConnectionHelper.getURLContent(new URL(url.replaceAll(" ", "%20")), headers);
	}

	/**
	 * Proxy method for {@link #getURLContent(URL, Map)}, creates URL object
	 * from passed string, setting the appropriate header when making the HTTP request.
	 * 
	 * @param url the URL to call
	 * @param headerName the name of the header to set
	 * @param headerValue the value of the header to set
	 * @return the entire HTML of the specified page
	 * @throws IOException in case of any networking error
	 */
	public static String getURLContent(String url, String headerName, String headerValue)
			throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(headerName, headerValue);
		return ConnectionHelper.getURLContent(new URL(url.replaceAll(" ", "%20")), headers);
	}
}
