package articles;

import java.io.IOException;
import java.util.Date;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

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
}
