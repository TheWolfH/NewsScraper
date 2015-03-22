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
	 * @param keyword
	 *            the keywords to add to the keywords set
	 */
	public ScrapedArticle(String url, String title, String keyword) {
		super(url, title, keyword);
	}

	/**
	 * Template method populating all empty properties of this article.
	 * 
	 * All fields except for url and title are populated by first fetching the
	 * article content from the server, parsing it and then extracting the
	 * relevant data using the {@code getXYZFromDocument(Document)} methods. If
	 * any String value obtained from these methods is empty (string.length() ==
	 * 0), {@code null} is set instead of the empty string.
	 * 
	 * Can be overridden in subclasses if different behavior is necessary.
	 * Should only be overridden if for some reason it is not necessary to fetch
	 * the article content from the respective server. In all other cases, the
	 * {@code getXYZFromDocument()} methods should be overridden.
	 * 
	 * @throws IOException
	 *             in case of any problems fetching the HTML document from the
	 *             server
	 */
	@Override
	public void populateData() throws IOException {
		this.log.finest(Thread.currentThread() + " starts populating article data for " + this.url);

		// Perform hook method
		this.beforePopulatingDataHook();

		try {
			Document doc = Jsoup.connect(this.url).timeout(60000).get();
			String value;

			// Populate fields
			if (this.subtitle == null) {
				value = this.getSubtitleFromDocument(doc);
				this.subtitle = (value.length() == 0 ? null : value);
			}

			if (this.fullText == null) {
				value = this.getFullTextFromDocument(doc);
				this.fullText = (value.length() == 0 ? null : value);
			}

			if (this.fullTextHTML == null) {
				value = this.getFullTextHTMLFromDocument(doc);
				this.fullTextHTML = (value.length() == 0 ? null : value);
			}

			if (this.publicationDate == null) {
				try {
					this.publicationDate = this.getPublicationDateFromDocument(doc);
				}
				catch (ParseException e) {
					// In case of parsing failure: publicationDate stays null,
					// log warning
					this.log.warning("Unable to parse publication date for article with url "
							+ this.url);
				}
			}
		}
		catch (IOException e) {
			// In case of any error: log warning
			this.log.warning("Unable to connect to " + this.url
					+ ", populating article data failed: " + e.getLocalizedMessage());
		}

		// Perform hook method
		this.afterPopulatingDataHook();

		// Debugging log entry
		this.log.finest(Thread.currentThread() + " finished populating article data for "
				+ this.url);
	}

	/**
	 * Template method returning the subtitle of this article by applying the
	 * selector provided by {@link #getSubtitleSelector()} on {@code doc}
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
	 * the selector provided by {@link #getFullTextSelector()} on {@code doc}
	 * and extracting all text in the matched element(s).
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
	 * {@code doc} and extracting all HTML in the matched element(s).
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
	 * on {@code doc} and extracting all text in the matched element(s). This
	 * text is then parsed by a {@link java.text.SimpleDateFormat} initialized
	 * with the format string provided by {@link #getPublicationDateFormat()}
	 * and the locale provided by {@link #getLocale()}.
	 * 
	 * Can be overridden in subclasses if different behavior is necessary.
	 * 
	 * @param doc
	 *            the document to extract the subtitle from
	 * 
	 * @return the publication date of this article as a {@link java.util.Date}
	 *         object
	 * 
	 * @throws ParseException
	 *             if the text selected as described above cannot be parsed by
	 *             the SimpleDateFormat generated as described above
	 */
	protected Date getPublicationDateFromDocument(Document doc) throws ParseException {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(this.getPublicationDateFormat(),
				this.getLocale());

		String dateString = doc.select(this.getPublicationDateSelector()).text();
		return dateFormatter.parse(dateString);
	}

	/**
	 * This method is called by {@link #populateData()} before fetching the
	 * article content and populating the article data. By default, it does not
	 * perform any actions, but subclasses can override this method. This might
	 * for example be used to artificially slow down the concurrent execution of
	 * that method to keep requests from being denied due to an excess of
	 * requests per second.
	 */
	protected void beforePopulatingDataHook() {

	}

	/**
	 * This method is called by {@link #populateData()} after fetching the
	 * article content and populating the article data, just before returning.
	 * By default, it does not perform any actions, but subclasses can override
	 * this method. This might for example be used to perform plausibility
	 * checks on the fetched values.
	 */
	protected void afterPopulatingDataHook() {

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
