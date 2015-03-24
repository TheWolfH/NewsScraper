package fetchers;

import helpers.ConfigReader;

import java.util.Date;

import org.jsoup.nodes.Element;

import articles.Article;

/**
 * Base class for all fetcher classes that obtain their results by scraping the
 * search page of the respective news provider's website. Users should implement
 * one of the direct subclasses of this class in order to integrate their own
 * data source into the framework.
 * 
 * @author Jan Helge Wolf
 * 
 */
public abstract class Scraper extends Fetcher {
	/**
	 * The User Agent header to use when performing any HTTP requests. The
	 * header is read from config.
	 */
	protected String userAgent;

	/**
	 * Constructs a Scraper, setting {@link #userAgent} to the property
	 * Scraper.searchArticles.userAgent read from config. This constructor must
	 * be invoked by any subclasses in order to guarantee the correct User Agent
	 * to be set.
	 */
	public Scraper() {
		this.userAgent = ConfigReader.getConfig().getProperty("Scraper.searchArticles.userAgent",
				null);
	}
	
	/**
	 * Factory method called by
	 * {@link #searchArticles(String[], Date, Date, int)} to create an object of
	 * a subclass of {@link articles.Article}. Must be implemented by subclasses
	 * by returning an object of the respective XYZArticle class with the passed
	 * {@code url} and {@code title}.
	 * 
	 * @param url
	 *            the url of the Article object to create
	 * @param title
	 *            the title of the Article object to create
	 * @return an object of the respective XYZArticle class
	 */
	protected abstract Article createArticle(String url, String title);

	/**
	 * Factory method called by
	 * {@link #searchArticles(String[], Date, Date, int)} to create an object of
	 * a subclass of {@link articles.Article}. Must be implemented by subclasses
	 * by returning an object of the respective XYZArticle class with the passed
	 * {@code url}, {@code title} and {@code keyword}.
	 * 
	 * @param url
	 *            the url of the Article object to create
	 * @param title
	 *            the title of the Article object to create
	 * @param keyword
	 *            the keyword to add to the keywords set of the article
	 * @return an object of the respective XYZArticle class
	 */
	protected abstract Article createArticle(String url, String title, String keyword);
	
	/**
	 * Template method called by
	 * {@link #searchArticles(String[], Date, Date, int)} to extract the url
	 * from an {@link org.jsoup.nodes.Element} containing information about one
	 * search result displayed on the news site's search page. The default
	 * implementation returns the absolute url contained in the {@code href}
	 * attribute of the first element matched by the selector provided by
	 * {@link #getUrlSelector()}. Can be overridden in subclasses to modify
	 * behavior.
	 * 
	 * @param articleElement
	 *            an HTML element containing information about a single search
	 *            result
	 * @return the url of the passed search result
	 */
	protected String getUrlFromSearchResult(Element articleElement) {
		return articleElement.select(this.getUrlSelector()).attr("abs:href");
	}

	/**
	 * Template method called by
	 * {@link #searchArticles(String[], Date, Date, int)} to extract the title
	 * from an {@link org.jsoup.nodes.Element} containing information about one
	 * search result displayed on the news site's search page. The default
	 * implementation returns the text content of all elements matched by the
	 * selector provided by {@link #getTitleSelector()}. Can be overridden in
	 * subclasses to modify behavior.
	 * 
	 * @param articleElement
	 *            an HTML element containing information about a single search
	 *            result
	 * @return the title of the passed search result
	 */
	protected String getTitleFromSearchResult(Element articleElement) {
		return articleElement.select(this.getTitleSelector()).text();
	}
	
	/**
	 * Returns the selector used by
	 * {@link #searchArticles(String[], Date, Date, int)} to identify the HTML
	 * elements that hold a single search result (e.g., a single article) on a
	 * page containing multiple such results. For example, if the results were
	 * displayed in an unordered list as such
	 * 
	 * <pre>
	 * {@code
	 * <ul class="results">
	 *  <li class="result">...</li>
	 *  <li class="result">...</li>
	 *  ...
	 * </ul>
	 * }
	 * </pre>
	 * 
	 * an appropriate selector would be {@code ul.results li.result}.
	 * 
	 * @return the selector used to identify the articles found by the search
	 *         performed
	 */
	protected abstract String getSearchResultsSelector();

	/**
	 * Returns the selector used by
	 * {@link #searchArticles(String[], Date, Date, int)} to identify the HTML
	 * element whose {@code href} attribute holds the article url. For example,
	 * if a single article within the search results was displayed as such
	 * 
	 * <pre>
	 * {@code
	 * <li class="result">
	 *  <div class="link">
	 *   <a href="/articles/article12345.html">
	 *    <span class="headline">News article 12345</span>
	 *   </a>
	 *  </div>
	 * </li>
	 * }
	 * </pre>
	 * 
	 * an appropriate selector would be {@code li.result div.link a}.
	 * 
	 * @return the selector used to identify the link pointing to the article
	 */
	protected abstract String getUrlSelector();

	/**
	 * Returns the selector used by
	 * {@link #searchArticles(String[], Date, Date, int)} to identify the HTML
	 * element(s) whose text is the article title. For example, if a single
	 * article within the search results was displayed as such
	 * 
	 * <pre>
	 * {@code
	 * <li class="result">
	 *  <div class="link">
	 *   <a href="/articles/article12345.html">
	 *    <span class="headline">News article 12345</span>
	 *   </a>
	 *  </div>
	 * </li>
	 * }
	 * </pre>
	 * 
	 * an appropriate selector would be {@code li.result div.link span.headline}
	 * .
	 * 
	 * @return the selector used to identify the article title
	 */
	protected abstract String getTitleSelector();

}
