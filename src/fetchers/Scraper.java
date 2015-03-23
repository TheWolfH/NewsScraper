package fetchers;

import helpers.ConfigReader;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import articles.Article;

/**
 * Base class for all fetcher classes that obtain their results by scraping the
 * search page of the respective news provider's website. By implementing (at
 * least) all abstract methods provided by this class and the {@link Fetcher}
 * class, users can implement the search for an arbitrary news provider.
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
	 * to bet set.
	 */
	public Scraper() {
		this.userAgent = ConfigReader.getConfig().getProperty("Scraper.searchArticles.userAgent",
				null);
	}

	/**
	 * Template method returning a {@link java.util.Set} of
	 * {@link articles.Article} objects representing articles that contain one
	 * or more of the strings in {@code keywords} and that were published on or
	 * after {@code fromDate} and on or before {@code toDate}. This method uses
	 * {@link #getSearchURL(String, Date, Date, int, int)} to iterate over the
	 * pagination of the search function of the scraped news site, calls the
	 * returned url and retrieves all search results using the selector provided
	 * by {@link #getSearchResultsSelector()}. These results are iterated over,
	 * extracting the url and the title using the
	 * {@link #getUrlFromSearchResult(Element)} and
	 * {@link #getTitleFromSearchResult(Element)} methods and creating objects
	 * of a subclass of {@link articles.Article} using the
	 * {@link #createArticle(String, String)} factory method. All the methods
	 * mentioned above can be overridden in subclasses in order to customize the
	 * behavior.
	 * 
	 * @param keywords
	 *            the keywords to search for
	 * @param fromDate
	 *            the earliest date an article may have been published on to be
	 *            found by the returned query
	 * @param toDate
	 *            the latest date an article may have been published on to be
	 *            found by the returned query
	 * @param articlesPerPage
	 *            the number of articles per search page
	 * @return the articles matching the conditions above
	 */
	protected Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate,
			int articlesPerPage) {
		this.log.info("Start scraping base url " + this.baseURL);

		// Article set to be returned
		Map<String, Article> articles = new HashMap<String, Article>();

		for (String keyword : keywords) {
			this.log.info("Start scraping for keyword " + keyword);

			// Set limit and offset for pagination, initialize articleElements
			// object
			int limit = articlesPerPage;
			int offset = 0;
			Elements articleElements = null;

			// Iterate over pagination
			do {
				try {
					// Do not raise offset in first loop invocation
					if (articleElements != null) {
						offset += limit;
					}

					// Parse HTML content
					String searchUrl = this.getSearchURL(keyword, fromDate, toDate, offset, limit);
					Document searchResult = Jsoup.connect(searchUrl).timeout(60000)
							.userAgent(this.userAgent).get();
					articleElements = searchResult.select(this.getSearchResultsSelector());

					// Exit loop when no more articles are found
					if (articleElements.size() < 1) {
						this.log.info("No more articles found, stopped scraping for keyword "
								+ keyword);
						break;
					}

					// Iterate over articleElements, generate Article objects
					// and add them to set
					for (Element articleElement : articleElements) {
						String url = this.getUrlFromSearchResult(articleElement);
						String title = this.getTitleFromSearchResult(articleElement);

						if (!articles.containsKey(url)) {
							// Article has not been found yet: create Article
							// object, add keyword to article, add article to
							// map
							articles.put(url, this.createArticle(url, title, keyword));
						}
						else {
							// Article was already found before, add keyword to
							// Article object in map
							articles.get(url).addKeyword(keyword);
						}

					}

					// Call hook if less articles are found than expected
					// Hook returns true if loop should be aborted
					if (articleElements.size() < limit) {
						if (this.lessArticlesThanExpectedHook(articlesPerPage,
								articleElements.size(), keyword)) {
							break;
						}
					}
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					this.log.severe("IOException when processing url "
							+ this.getSearchURL(keyword, fromDate, toDate, offset, limit) + ": "
							+ e.getMessage());
				}
			} while (true);
		}

		this.log.info("Finished scraping base url " + this.baseURL);

		// Process articles by filtering and populating, then return
		return this.processArticles(articles, fromDate, toDate);
	}

	/**
	 * Hook method called from
	 * {@link #searchArticles(String[], Date, Date, int)} whenever a search
	 * result page contains less than {@code articlesPerPage}. The method is
	 * passed the number of articles that were expected ({@code articlesPerPage}
	 * from said method), the number of articles actually encountered, and
	 * keyword currently processed. Returns a boolean indicating whether
	 * searching for the current keyword shall be aborted ({@code true}) or not
	 * ({@code false}). The default implementation logs an event with severity
	 * {@code Level.INFO} and returns {@code true}. Can be overridden by
	 * subclasses to implement different behavior, mainly if the search
	 * functionality does not reliably return a fixed number of results per
	 * page.
	 * 
	 * @param expectedArticles
	 *            the number of articles that were expected to be found
	 * @param actualArticles
	 *            the number of articles actually found
	 * @param keyword
	 *            the current keyword being processed
	 * @return whether to abort the current keyword loop ({@code true}) or not (
	 *         {@code false})
	 */
	protected boolean lessArticlesThanExpectedHook(int expectedArticles, int actualArticles,
			String keyword) {
		this.log.info("Found less articles than expected, stopped scraping for keyword " + keyword);

		return true;
	}

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
	 * Returns the selector used by
	 * {@link #searchArticles(String[], Date, Date, int)} to identify the HTML
	 * elements that hold a single search result (e.g., a single article) on a
	 * page containing multiple such results. For example, if the results were
	 * displayed in an unordered list as such
	 * 
	 * <pre>
	 * {@code
	 * <body>
	 * <ul class="results">
	 *  <li class="result">...</li>
	 *  <li class="result">...</li>
	 *  ...
	 * </ul>
	 * </body>
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
