package helpers;

import fetchers.*;

public enum DataSource {
	GUARDIAN("The Guardian", new GuardianFetcher()),
	SPIEGELONLINE("Spiegel Online", new SpiegelOnlineScraper()),
	TELEGRAPH("The Telegraph", new TelegraphScraper()),
	ZEIT("Die Zeit", new ZeitFetcher());
	
	private String name;
	private Fetcher fetcher;
	
	DataSource(String name, Fetcher fetcher) {
		this.name = name;
		this.fetcher = fetcher;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Fetcher getFetcher() {
		return this.fetcher;
	}
}
