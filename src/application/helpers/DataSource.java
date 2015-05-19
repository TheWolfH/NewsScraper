package application.helpers;

import application.fetchers.DailyExpressScraper;
import application.fetchers.DailyMailScraper;
import application.fetchers.GuardianFetcher;
import application.fetchers.MirrorScraper;
import application.fetchers.SpiegelOnlineScraper;
import application.fetchers.SpiegelScraper;
import application.fetchers.SternScraper;
import application.fetchers.SueddeutscheScraper;
import application.fetchers.TagesspiegelScraper;
import application.fetchers.TelegraphScraper;
import application.fetchers.ZeitFetcher;
import framework.fetchers.*;

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
	DAILYEXPRESS("Daily Express", new DailyExpressScraper()),
	SUEDDEUTSCHE("Süddeutsche Zeitung", new SueddeutscheScraper());

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
