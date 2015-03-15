package articles;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class ScrapedArticle extends Article {
	/**
	 * Constructs a {@code ScrapedArticle} using the given url and title.
	 * 
	 * @param url
	 *            the url of the article
	 * @param title
	 *            the title of the article
	 */
	public ScrapedArticle(String url, String title) {
		super(url, title);
	}

	/**
	 * Constructs a {@code ScrapedArticle}, setting the {@code url} and
	 * {@code title} properties and adding the {@code keywords} to its keywords
	 * set.
	 * 
	 * @param url
	 *            the url of the article
	 * @param title
	 *            the title (headline) of the article
	 * @param keywords
	 *            the keywords to add to the keywords set
	 */
	public ScrapedArticle(String url, String title, String keyword) {
		super(url, title, keyword);
	}

	/**
	 * Template method populating all empty properties of this article.
	 * 
	 * All fields except for url and title are populated by first fetching the
	 * article content from the server, parsing it into {@link #document} and
	 * then extracting the relevant data using the {@code getXYZFromDocument()}
	 * methods.
	 * 
	 * Can be overridden in subclasses if different behavior is necessary.
	 * Should only be overridden if for some reason it is not necessary to fetch
	 * the article content from the respective server. In all other cases, the
	 * {@code getXYZFromDocument()} methods should be overridden.
	 * 
	 * @return void
	 * @throws IOException
	 *             in case of any problems fetching the HTML document from the
	 *             server
	 */
	public void populateData() throws IOException {
		this.log.finest(Thread.currentThread() + " starts populating article data for " + this.url);

		Document doc = Jsoup.connect(this.url).timeout(60000).get();

		// Populate fields
		if (this.subtitle == null) {
			this.subtitle = this.getSubtitleFromDocument(doc);
		}

		if (this.fullText == null) {
			this.fullText = this.getFullTextFromDocument(doc);
		}

		if (this.fullTextHTML == null) {
			this.fullTextHTML = this.getFullTextHTMLFromDocument(doc);
		}

		if (this.publicationDate == null) {
			try {
				this.publicationDate = this.getPublicationDateFromDocument(doc);
			}
			catch (ParseException e) {
				// In case of failure: set publicationDate to 0001-01-01
				// 00:00:00 (GMT)
				this.publicationDate = new Date(-62167392000000L);
				this.log.warning("Unable to parse date for article with url " + this.url
						+ ", set to default date (0001-01-01 00:00:00 GMT)");
				// e.printStackTrace();
			}
		}

		this.log.finest(Thread.currentThread() + " finished populating article data for " + this.url);
	}

	/**
	 * Template method returning the subtitle of this article by applying the
	 * selector provided by {@link #getSubtitleSelector()} on {@link #document}
	 * and extracting all text in the matched element(s).
	 * 
	 * Can be overridden in subclasses if different behavior is necessary.
	 * 
	 * @param doc
	 *            the document to extract the subtitle from
	 * 
	 * @return the subtitle of this article
	 */
	protected String getSubtitleFromDocument(Document doc) {
		return doc.select(this.getSubtitleSelector()).text();
	}

	/**
	 * Template method returning the complete text of this article by applying
	 * the selector provided by {@link #getFullTextSelector()} on
	 * {@link #document} and extracting all text in the matched element(s).
	 * 
	 * Can be overridden in subclasses if different behavior is necessary.
	 * 
	 * @param doc
	 *            the document to extract the fullText from
	 * 
	 * @return the complete text of this article
	 */
	protected String getFullTextFromDocument(Document doc) {
		return doc.select(this.getFullTextSelector()).text();
	}

	/**
	 * Template method returning the HTML of the complete text of this article
	 * by applying the selector provided by {@link #getFullTextSelector()} on
	 * {@link #document} and extracting all HTML in the matched element(s).
	 * 
	 * Can be overridden in subclasses if different behavior is necessary.
	 * 
	 * @param doc
	 *            the document to extract the subtitle from
	 * 
	 * @return the complete HTML of this article
	 */
	protected String getFullTextHTMLFromDocument(Document doc) {
		return doc.select(this.getFullTextSelector()).html();
	}

	/**
	 * Template method returning the date of publication of this article by
	 * applying the selector provided by {@link #getPublicationDateSelector()}
	 * on {@link #document} and extracting all text in the matched element(s).
	 * This text is then parsed by a {@link java.text.SimpleDateFormat}
	 * initialized with the format string provided by
	 * {@link #getPublicationDateFormat()} and the locale provided by
	 * {@link #getLocale()}.
	 * 
	 * Can be overridden in subclasses if different behavior is necessary.
	 * 
	 * @param doc
	 *            the document to extract the subtitle from
	 * 
	 * @return the publication date of this article as a {@link java.util.Date}
	 *         object
	 */
	protected Date getPublicationDateFromDocument(Document doc) throws ParseException {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(this.getPublicationDateFormat(),
				this.getLocale());

		String dateString = doc.select(this.getPublicationDateSelector()).text();
		return dateFormatter.parse(dateString);
	}

	/**
	 * Returns the selector used by {@link #getSubtitleFromDocument(Document)}
	 * to extract the subtitle of this article. Must be implemented by
	 * subclasses.
	 * 
	 * @return the jQuery selector used to determine the element containing the
	 *         subtitle of this article
	 */
	protected abstract String getSubtitleSelector();

	/**
	 * Returns the selector used by
	 * {@link #getPublicationDateFromDocument(Document)} to extract the
	 * publicationDate of this article. Must be implemented by subclasses.
	 * 
	 * @return the jQuery selector used to determine the element containing the
	 *         publicationDate of this article
	 */
	protected abstract String getPublicationDateSelector();

	/**
	 * Returns the date format string (as specified in
	 * {@link java.text.SimpleDateFormat}) which is used by
	 * {@link #getPublicationDateFromDocument(Document)} to extract the
	 * publicationDate of this article. Must be implemented by subclasses.
	 * 
	 * @return the date format string used to determine the element containing
	 *         the publicationDate of this article
	 */
	protected abstract String getPublicationDateFormat();

	/**
	 * Returns the {@link java.util.Locale} used by
	 * {@link #getPublicationDateFromDocument(Document)} to extract the
	 * publicationDate of this article. Should match the country and/or main
	 * language of the respective news provider. Must be implemented by
	 * subclasses.
	 * 
	 * @return the Locale used by {@link java.text.SimpleDateFormat} to parse
	 *         the publicationDate of this article
	 */
	protected abstract Locale getLocale();

	/**
	 * Returns the selector used by {@link #getFullTextFromDocument(Document)}
	 * and {@link #getFullTextHTMLFromDocument(Document)} to extract the
	 * fullText and fullTextHTML properties of this article. Must be implemented
	 * by subclasses.
	 * 
	 * @return the jQuery selector used to determine the element containing the
	 *         fullText and fullTextHTML of this article
	 */
	protected abstract String getFullTextSelector();
}
