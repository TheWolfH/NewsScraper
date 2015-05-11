package application.articles;

import java.util.Locale;

import framework.articles.ScrapedArticle;

public class TagesspiegelArticle extends ScrapedArticle {

	public TagesspiegelArticle(String url, String title) {
		super(url, title);
	}

	public TagesspiegelArticle(String url, String title, String keyword) {
		super(url, title, keyword);
	}

	@Override
	protected String getSubtitleSelector() {
		return "article p.hcf-teaser";
	}

	@Override
	protected String getPublicationDateSelector() {
		return "article span.date";
	}

	@Override
	protected String getPublicationDateFormat() {
		return "dd.MM.yyyy HH:mm 'Uhr'";
	}

	@Override
	protected Locale getLocale() {
		return Locale.GERMANY;
	}

	@Override
	protected String getFullTextSelector() {
		return "article > p:not(.hcf-teaser)";
	}

}
