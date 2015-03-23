package articles;

import helpers.LoggerGenerator;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.*;

/**
 * This class is the uniform data model of the NewsScraper prototype,
 * representing a single article. At construction time, both the URL and the
 * title of the article must be known.
 * 
 * Subclasses need to implement the {@link #populateData()} method, which tries
 * to fill all other properties with the respective values.
 * 
 * Concrete articles for data sources processed via scraping will normally
 * extend the {@link ScrapedArticle} class, which provides a pre-defined
 * implementation of that method. As this implementation relies on fetching the
 * article content from the respective server, it might be executed
 * concurrently. Although no Article object should be processed by more than one
 * thread at a time, the Article class is designed to be thread-safe in order to
 * prevent any program errors in case the mentioned guideline is not adhered to.
 * 
 * @author Jan Helge Wolf
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Article {
	/**
	 * The url pointing to the web version of this article. Final as it is used
	 * for identification.
	 */
	protected final String url;

	/**
	 * The title (headline) of this article. Final as it has to be set at
	 * construction time and there's no reason to ever change it.
	 */
	protected final String title;

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
	 * The keywords this article was found by. Final as there's no need to ever
	 * replace it.
	 */
	protected final Set<String> keywords;

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
	 * Although only the url and the title are guaranteed to be set at
	 * construction time, exact behavior of this method depends on the subclass.
	 * While using an API might provide all relevant fields immediately, in most
	 * cases it will be necessary to fetch the actual HTML content of the
	 * article to populate the remaining fields. In the former case, this method
	 * might not need to perform any actions, while in the latter case it
	 * fetches the article from the respective server and extracts the relevant
	 * information from it. In order to save network bandwidth and increase
	 * performance, subclasses <b>must</b> check whether any of the property of
	 * this article is still {@code null} before performing any network actions.
	 * After the the successful returning of this method, all Article properties
	 * publicly available via getter methods either hold the correct value or
	 * {@code null}, if no valid value could be retrieved. Empty strings are
	 * considered invalid values in the sense of the preceding sentence.
	 * 
	 * @throws IOException
	 *             in case of any problems fetching the HTML document from the
	 *             server
	 */
	public abstract void populateData() throws IOException;

	/**
	 * @return the url
	 */
	public synchronized String getUrl() {
		return this.url;
	}

	/**
	 * @return the title
	 */
	public synchronized String getTitle() {
		return this.title;
	}

	/**
	 * @return the subtitle
	 */
	public synchronized String getSubtitle() {
		return this.subtitle;
	}

	/**
	 * @return the publicationDate
	 */
	public synchronized Date getPublicationDate() {
		// Return a cloned version of our publication date (or null) in order to
		// prevent clients from tampering with our publication date
		return (this.publicationDate == null ? null : (Date) this.publicationDate.clone());
	}

	/**
	 * @return the fullText
	 */
	public synchronized String getFullText() {
		return this.fullText;
	}

	/**
	 * @return the fullTextHTML
	 */
	public synchronized String getFullTextHTML() {
		return this.fullTextHTML;
	}

	/**
	 * @return the keywords
	 */
	public synchronized Set<String> getKeywords() {
		// Return a new HashSet in order to prevent clients from modifying our
		// keywords
		return new HashSet<String>(this.keywords);
	}

	/**
	 * Adds a keyword to the keywords set
	 * 
	 * @param keyword
	 *            the keyword to add
	 */
	public synchronized void addKeyword(String keyword) {
		this.keywords.add(keyword);
	}
}
