package helpers;

import fetchers.*;

public enum DataSource {
	GUARDIAN("The Guardian", new GuardianFetcher()),
	SPIEGELONLINE("Spiegel Online", new SpiegelOnlineScraper()),
	SPIEGEL("Der Spiegel", new SpiegelScraper()),
	TELEGRAPH("The Telegraph", new TelegraphScraper()),
	ZEIT("Die Zeit", new ZeitFetcher()),
	TAGESSPIEGEL("Der Tagesspiegel", new TagesspiegelScraper()),
	STERN("Stern", new SternScraper()),
	MIRROR("Mirror", new MirrorScraper()),
	DAILYMAIL("Daily Mail Online", new DailyMailScraper()),
	WELTONLINE("Die Welt Online", new WeltOnlineScraper())
	/*WELTONLINE("Die Welt Online", new WeltOnlineScraper())*/;
	
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
