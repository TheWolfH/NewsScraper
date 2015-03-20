package helpers;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerGenerator {
	private static Logger logger;

	private LoggerGenerator() {
	}

	public static Logger getLogger() {
		if (logger == null) {
			try {
				// Configure handler
				Handler handler = new FileHandler("log.txt", false);
				handler.setEncoding("UTF-8");
				handler.setFormatter(new SimpleFormatter());
				handler.setLevel(Level.ALL);

				// Create logger and add handler to it
				logger = Logger.getLogger("Log");
				logger.addHandler(handler);
				logger.setUseParentHandlers(false);
			}
			catch (IOException e) {
				// As no logging to file is possible, print stack trace
				e.printStackTrace();
			}
		}
		
		return logger;
	}
}
