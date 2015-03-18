package articles;

import java.util.Locale;

public class SpiegelArticle extends ScrapedArticle {

	public SpiegelArticle(String url, String title) {
		super(url, title);
	}

	public SpiegelArticle(String url, String title, String keyword) {
		super(url, title, keyword);
	}

	@Override
	protected String getSubtitleSelector() {
		return "#content-main p.author ~ p > strong";
	}

	@Override
	protected String getPublicationDateSelector() {
		return "div#spShortDate";
	}

	@Override
	protected String getPublicationDateFormat() {
		return "dd.MM.yyyy";
	}

	@Override
	protected Locale getLocale() {
		return Locale.GERMANY;
	}

	@Override
	protected String getFullTextSelector() {
		return "#content-main div.artikel";
	}

}
