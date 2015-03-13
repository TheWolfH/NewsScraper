package articles;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class SpiegelOnlineArticle extends ScrapedArticle {

	public SpiegelOnlineArticle(String url, String title) {
		super(url, title);
	}
	
	public SpiegelOnlineArticle(String url, String title, String keyword) {
		super(url, title, keyword);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see articles.ScrapedArticle#getPublicationDateFromDocument()
	 */
	@Override
	protected Date getPublicationDateFromDocument(Document doc) throws ParseException {
		// Initialize variables
		Elements elements;
		String publicationDateString;
		SimpleDateFormat dateFormatter;

		// Normal case: time element with itemprop and datetime attributes
		elements = doc.select("time[itemprop=\"datePublished\"][datetime]");

		if (!elements.isEmpty()) {
			publicationDateString = elements.first().attr("datetime");
			dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return dateFormatter.parse(publicationDateString);
		}

		// Second case: span element with itemprop and content attributes
		elements = doc.select("span[itemprop=\"datePublished\"][content]");

		if (!elements.isEmpty()) {
			publicationDateString = elements.first().attr("content");
			dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ");
			return dateFormatter.parse(publicationDateString);
		}

		// Third case: simple span element, fetch date from text
		elements = doc.select(".module-box .article-function-box-wide span");
		
		if (!elements.isEmpty()) {
			publicationDateString = elements.first().text();
			dateFormatter = new SimpleDateFormat("dd.MM.yyyy – HH:mm 'Uhr'");
			return dateFormatter.parse(publicationDateString);
		}

		// Fallback
		return super.getPublicationDateFromDocument(doc);
	}

	@Override
	protected String getSubtitleSelector() {
		return "#content-main p.article-intro";
	}

	@Override
	protected String getPublicationDateSelector() {
		return "#content-main li.article-function-date time";
	}

	@Override
	protected String getPublicationDateFormat() {
		return "EEE, dd.MM.yyyy – HH:mm 'Uhr'";
	}

	@Override
	protected Locale getLocale() {
		return Locale.GERMANY;
	}

	@Override
	protected String getFullTextSelector() {
		return "#content-main .article-section";
	}
}
