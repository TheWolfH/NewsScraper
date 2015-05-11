package application.articles;

import java.util.Locale;

import framework.articles.ScrapedArticle;

public class WeltOnlineArticle extends ScrapedArticle {

	public WeltOnlineArticle(String url, String title) {
		super(url, title);
	}

	public WeltOnlineArticle(String url, String title, String keyword) {
		super(url, title, keyword);
	}

	@Override
	protected String getSubtitleSelector() {
		return "div#main p#artAbstract";
	}

	@Override
	protected String getPublicationDateSelector() {
		return "div.timestamp span.time";
	}

	@Override
	protected String getPublicationDateFormat() {
		return "dd.MM.yy";
	}

	@Override
	protected Locale getLocale() {
		return Locale.GERMANY;
	}

	@Override
	protected String getFullTextSelector() {
		return "div#main div.groupWrapper div.storyBody";
	}

}
