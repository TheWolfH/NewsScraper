package articles;

import java.util.Locale;

public class TagesspiegelArticle extends ScrapedArticle {

	public TagesspiegelArticle(String url, String title) {
		super(url, title);
		if(url.length()<50) {
			System.out.println(url);
		}
	}

	public TagesspiegelArticle(String url, String title, String keyword) {
		super(url, title, keyword);
		if(url.length()<50) {
			System.out.println(url);
		}
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
