package articles;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Article {
	protected String url;
	protected String title;
	protected String subtitle;
	protected Date publicationDate;
	protected String fullText;
	protected String fullTextHTML;
	protected Set<String> keywords;

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
	 * @return void
	 * @throws IOException
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
