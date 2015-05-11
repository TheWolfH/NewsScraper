package application.articles;

import java.util.Locale;

import framework.articles.ScrapedArticle;

public class TelegraphArticle extends ScrapedArticle {

	public TelegraphArticle(String url, String title) {
		super(url, title);
	}

	public TelegraphArticle(String url, String title, String keyword) {
		super(url, title, keyword);
	}
	
	@Override
	protected String getSubtitleSelector() {
		return ".twoThirds.gutter .storyhead h2";
	}

	@Override
	protected String getPublicationDateSelector() {
		return ".twoThirds.gutter p.publishedDate";
	}

	@Override
	protected String getPublicationDateFormat() {
		return "h:mma z dd MMM yyyy";
	}

	@Override
	protected Locale getLocale() {
		return Locale.UK;
	}

	@Override
	protected String getFullTextSelector() {
		return ".twoThirds.gutter #mainBodyArea div[class$=\"Par\"], .twoThirds.gutter #mainBodyArea div.body";
	}
}
