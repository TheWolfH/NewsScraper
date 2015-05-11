package application.articles;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jsoup.nodes.Document;

import framework.articles.ScrapedArticle;

public class SueddeutscheArticle extends ScrapedArticle {

	public SueddeutscheArticle(String url, String title) {
		super(url, title);
	}

	public SueddeutscheArticle(String url, String title, String keyword) {
		super(url, title, keyword);
	}

	@Override
	protected String getSubtitleSelector() {
		return "article section.body p.article.entry-summary";
	}

	// Override in order to leverage the fact that Sueddeutsche makes use
	// of the <time> element and its datetime attribute, in which the
	// publication is stored in SQL format.
	@Override
	protected Date getPublicationDateFromDocument(Document doc) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(this.getPublicationDateFormat(),
				this.getLocale());
		String publicationDateString = doc.select(this.getPublicationDateSelector()).attr("datetime");
		
		return formatter.parse(publicationDateString);
	}
	
	@Override
	protected String getPublicationDateSelector() {
		return "article section.header time";
	}

	@Override
	protected String getPublicationDateFormat() {
		return "yyyy-MM-dd HH:mm:ss";
	}

	@Override
	protected Locale getLocale() {
		return Locale.GERMANY;
	}

	@Override
	protected String getFullTextSelector() {
		return "article section.body p:not([class]), article section.body ul, article section.body h3";
	}

}
