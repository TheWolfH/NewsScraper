package fetchers;

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
 * Base class for all Scraper classes used to scrape data sources with
 * predictable search functionalities. 'Predictable' in the sense of this
 * framework means that all HTTP addresses that need to be called throughout the
 * scraping process (including the pagination of the leveraged search
 * functionality) can be determined beforehand. In cases where the next HTTP
 * address to call must be extracted from the current search page (e.g. if the
 * search URL contain unpredictable components), {@link ReactiveScraper} is the
 * class to extend.
 * 
 * @author Jan Helge Wolf
 * 
 */
public abstract class PredictiveScraper extends Scraper {

	/**
	 * Constructs a PredictiveScraper. This constructor must be invoked by any
	 * subclasses in order to guarantee the correct User Agent to be set.
	 */
	public PredictiveScraper() {
		super();
	}

	/**
	 * Template method returning a {@link java.util.Map} of
	 * {@link articles.Article} objects representing articles that contain one
	 * or more of the strings in {@code keywords} and that were published on or
	 * after {@code fromDate} and on or before {@code toDate}, mapped to their
	 * URL. This method uses {@link #getSearchURL(String, Date, Date, int, int)}
	 * to iterate over the pagination of the search function of the scraped news
	 * site, calls the returned URL and retrieves all search results using the
	 * selector provided by {@link #getSearchResultsSelector()}. These results
	 * are iterated over, extracting the URL and the title using the
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
			while (true) {
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
			}
		}

		this.log.info("Finished scraping base url " + this.baseURL);

		// Process articles by filtering and populating, then return
		return this.processArticles(articles, fromDate, toDate);
	}

	/**
	 * Helper method used by {@link #searchArticles(String[], Date, Date, int)}
	 * to search for articles matching the given conditions. More specifically,
	 * it returns the http(s) query address that needs to be called to obtain at
	 * least the url and the title of the articles number {@code offset} to
	 * {@code offset+limit} (in a zero-based counting) which contain the
	 * {@code keyword} and were published between {@code fromDate} and
	 * {@code toDate}.
	 * 
	 * @param keyword
	 *            the keyword to search for
	 * @param fromDate
	 *            the earliest date an article may have been published on to be
	 *            found by the returned query
	 * @param toDate
	 *            the latest date an article may have been published on to be
	 *            found by the returned query
	 * @param offset
	 *            the number of the first article to be found by the returned
	 *            query
	 * @param limit
	 *            the number of articles to be returned by the returned query
	 * @return the http(s) query address that will return the articles as per
	 *         the above mentioned conditions
	 */
	protected abstract String getSearchURL(String keyword, Date fromDate, Date toDate, int offset,
			int limit);

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
}
