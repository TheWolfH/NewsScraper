package articles;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Article {
	protected String url;
	protected String title;
	protected String subtitle;
	protected Date publicationDate;
	protected String fullText;
	protected String fullTextHTML;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Article other = (Article) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		}
		else if (!url.equals(other.url))
			return false;
		return true;
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

}
