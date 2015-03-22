package articles;

import helpers.LoggerGenerator;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Article {
	/**
	 * The url pointing to the web version of this article. Immutable as it is
	 * used for identification.
	 */
	protected final String url;

	/**
	 * The title (headline) of this article
	 */
	protected String title;

	/**
	 * The subtitle (byline) of this article
	 */
	protected String subtitle;

	/**
	 * The date (and time) this article was published. Initial publishing time
	 * is preferred over last updated timestamps.
	 */
	protected Date publicationDate;

	/**
	 * The entire text of this article, stripped of all HTML and/or other markup
	 */
	protected String fullText;

	/**
	 * The entire HTML code of this article. Does not include any boilerplate
	 * code of the site (header, footer, comments section etc.), but only the
	 * actual article content.
	 */
	protected String fullTextHTML;

	/**
	 * The keywords this article was found by
	 */
	protected Set<String> keywords;

	/**
	 * Internal logging utility. Can and should be used by subclasses to provide
	 * feedback to the user in case of any errors.
	 */
	protected final Logger log = LoggerGenerator.getLogger();

	/**
	 * Constructs an Article object, setting the {@code url} and {@code title}
	 * properties.
	 * 
	 * @param url
	 *            the url of the article
	 * @param title
	 *            the title (headline) of the article
	 */
	public Article(String url, String title) {
		this.url = url;
		this.title = title;
		this.keywords = new HashSet<String>();
	}

	/**
	 * Constructs an Article object, setting the {@code url} and {@code title}
	 * properties and adding the {@code keyword} to its keywords set.
	 * 
	 * @param url
	 *            the url of the article
	 * @param title
	 *            the title (headline) of the article
	 * @param keyword
	 *            the keyword to add to the keywords set
	 */
	public Article(String url, String title, String keyword) {
		this.url = url;
		this.title = title;

		this.keywords = new HashSet<String>();
		this.keywords.add(keyword);
	}

	/**
	 * Constructs an Article object, setting the {@code url} and {@code title}
	 * properties and adding the {@code keywords} to its keywords set.
	 * 
	 * @param url
	 *            the url of the article
	 * @param title
	 *            the title (headline) of the article
	 * @param keywords
	 *            the keywords to add to the keywords set
	 */
	public Article(String url, String title, Collection<String> keywords) {
		this.url = url;
		this.title = title;

		this.keywords = new HashSet<String>();
		this.keywords.addAll(keywords);
	}

	/**
	 * Populates all empty properties of this article
	 * 
	 * When using an API, this is mainly used to populate the fullText and
	 * fullTextHTML properties. For articles fetched by scraping, all properties
	 * except for url and title are fetched here.
	 * 
	 * @throws IOException
	 *             in case of any problems fetching the HTML document from the
	 *             server
	 */
	public abstract void populateData() throws IOException;

	/**
	 * @return the url
	 */
	public String getUrl() {
		return this.url;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * @return the subtitle
	 */
	public String getSubtitle() {
		return this.subtitle;
	}

	/**
	 * @return the publicationDate
	 */
	public Date getPublicationDate() {
		return this.publicationDate;
	}

	/**
	 * @return the fullText
	 */
	public String getFullText() {
		return this.fullText;
	}

	/**
	 * @return the fullTextHTML
	 */
	public String getFullTextHTML() {
		return this.fullTextHTML;
	}

	/**
	 * @return the keywords
	 */
	public Set<String> getKeywords() {
		return this.keywords;
	}

	/**
	 * Adds a keyword to the keywords set
	 * 
	 * @param keyword
	 *            the keyword to add
	 */
	public void addKeyword(String keyword) {
		this.keywords.add(keyword);
	}
}
