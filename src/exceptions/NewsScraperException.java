package exceptions;

public abstract class NewsScraperException extends Exception {

	public NewsScraperException() {
		super();
	}

	public NewsScraperException(String message) {
		super(message);
	}

	public NewsScraperException(Throwable cause) {
		super(cause);
	}

	public NewsScraperException(String message, Throwable cause) {
		super(message, cause);
	}
}