package framework.helpers;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Helper class to read properties from NewsScraper.properties file.
 * 
 * @author Jan Helge Wolf
 *
 */
public class ConfigReader {
	public final static String FILENAME = "NewsScraper.properties";
	private static Properties props;
	
	private ConfigReader() {
	}

	public static Properties getConfig() {
		if (props == null) {
			props = new Properties();
			
			try {
				props.load(new FileReader(FILENAME));
			}
			catch (IOException e) {
				Logger log = LoggerGenerator.getLogger();
				log.severe("Failed to load " + FILENAME + " properties file");
			}
		}
		
		return props;
	}
}
