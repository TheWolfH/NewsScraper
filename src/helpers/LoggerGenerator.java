package helpers;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerGenerator {
	private static LoggerGenerator generator = new LoggerGenerator();
	private Logger logger;

	private LoggerGenerator() {
		try {
			// Configure handler
			Handler handler = new FileHandler("log.txt", false);
			handler.setEncoding("UTF-8");
			handler.setFormatter(new SimpleFormatter());
			handler.setLevel(Level.ALL);

			// Create logger and add handler to it
			this.logger = Logger.getLogger("Log");
			this.logger.addHandler(handler);
			this.logger.setUseParentHandlers(false);
		}
		catch (IOException e) {
		}
	}

	public static LoggerGenerator getLoggerGenerator() {
		return generator;
	}

	public Logger getLogger() {
		return this.logger;
	}
}
