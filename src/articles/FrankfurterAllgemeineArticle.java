package articles;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jsoup.nodes.Document;

public class FrankfurterAllgemeineArticle extends ScrapedArticle {

	public FrankfurterAllgemeineArticle(String url, String title) {
		super(url, title);
	}

	public FrankfurterAllgemeineArticle(String url, String title, String keyword) {
		super(url, title, keyword);
	}

	@Override
	protected String getSubtitleSelector() {
		return "div.Artikel div.FAZArtikelEinleitung [itemprop=\"description\"]";
	}

	// Override in order to leverage the fact that FAZ.net makes use
	// of the itemprop=datePublished attribute and its content attribute, in
	// which the publication is stored in RFC 822 format.
	@Override
	protected Date getPublicationDateFromDocument(Document doc) throws ParseException {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(this.getPublicationDateFormat(),
				this.getLocale());

		String dateString = doc.select(this.getPublicationDateSelector()).attr("content");
		return dateFormatter.parse(dateString);
	}

	@Override
	protected String getPublicationDateSelector() {
		return "div.Artikel [itemprop=\"datePublished\"]";
	}

	@Override
	protected String getPublicationDateFormat() {
		return "yyyy-MM-dd'T'HH:mm:ssZ";
	}

	@Override
	protected Locale getLocale() {
		return Locale.GERMANY;
	}

	@Override
	protected String getFullTextSelector() {
		return "div.Artikel [itemprop=\"articleBody\"]";
	}

}
