package helpers;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class ConnectionHelper {
	public static String getURLContent(URL url) throws IOException {
		Scanner scanner = new Scanner(url.openStream(), "UTF-8");
		String out = scanner.useDelimiter("\\A").next();
		scanner.close();
		
		return out;
	}
	
	public static String getURLContent(String url) throws IOException {
		return ConnectionHelper.getURLContent(new URL(url.replaceAll(" ", "%20")));
	}
}
