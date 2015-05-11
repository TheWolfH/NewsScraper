package application.articles;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import framework.articles.ScrapedArticle;

public class MirrorArticle extends ScrapedArticle {

	public MirrorArticle(String url, String title) {
		super(url, title);
	}

	public MirrorArticle(String url, String title, String keyword) {
		super(url, title, keyword);
	}

	@Override
	protected String getSubtitleSelector() {
		return "div.article-page div.article div[itemprop*=\"alternativeHeadline\"]";
	}

	@Override
	protected String getPublicationDateSelector() {
		return "div.article-page div.article [data-type=\"pub-date\"]";
	}

	@Override
	protected String getPublicationDateFormat() {
		return "HH:mm, d MMM yyyy";
	}

	@Override
	protected Locale getLocale() {
		return Locale.UK;
	}

	@Override
	protected String getFullTextSelector() {
		return "div.article-page div.article div[itemprop=\"articleBody\"]";
	}

	/**
	 * Overrides {@link ScrapedArticle#getPublicationDateFromDocument(Document)}
	 * in order to leverage the fact that Mirror articles possess a {@code time}
	 * element whose {@code datetime} property is the publication date of the
	 * article in RFC 822 form. In case no such element can be found, the
	 * overridden method of the superclass is called.
	 * 
	 * @see ScrapedArticle#getPublicationDateFromDocument(Document)
	 */
	@Override
	protected Date getPublicationDateFromDocument(Document doc) throws ParseException {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZZZ");
		
		// Normal case: time element with itemprop and datetime attributes
		Elements elements = doc.select("time[itemprop=\"datePublished\"][datetime]");
		
		if (!elements.isEmpty()) {
			String publicationDateString = elements.first().attr("datetime");
			return dateFormatter.parse(publicationDateString);
		}
		
		// Fallback
		return super.getPublicationDateFromDocument(doc);
	}
}
