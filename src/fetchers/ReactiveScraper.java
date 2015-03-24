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
 * unpredictable search functionalities. 'Unpredictable' in the sense of this
 * framework means that the HTTP addresses that need to be called throughout the
 * scraping process (including the pagination of the leveraged search
 * functionality) cannot be determined beforehand. Instead, the URL of the next
 * search page is iteratively extracted from the current search page. In cases
 * where the search functionality produces predictable URLs,
 * {@link PredictiveScraper} should be used.
 * 
 * @author Jan Helge Wolf
 * 
 */
public abstract class ReactiveScraper extends Scraper {

	/**
	 * Constructs a ReactiveScraper. This constructor must be invoked by any
	 * subclasses in order to guarantee the correct User Agent to be set.
	 */
	public ReactiveScraper() {
		super();
	}

	/**
	 * Template method returning a {@link java.util.Map} of
	 * {@link articles.Article} objects representing articles that contain one
	 * or more of the strings in {@code keywords} and that were published on or
	 * after {@code fromDate} and on or before {@code toDate}, mapped to their
	 * URL. This method uses {@link #getFirstSearchURL(String, Date, Date, int)}
	 * and GETNEXTSEARCHURL to iterate over the pagination of the search
	 * function of the scraped news site, calls the returned URL and retrieves
	 * all search results using the selector provided by
	 * {@link #getSearchResultsSelector()}. These results are iterated over,
	 * extracting the URL and the title using the
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
	public Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate,
			int articlesPerPage) {
		this.log.info("Start scraping base url " + this.baseURL);

		// Article set to be returned
		Map<String, Article> articles = new HashMap<String, Article>();

		for (String keyword : keywords) {
			this.log.info("Start scraping for keyword " + keyword);

			// Set limit and offset for pagination, initialize articleElements
			// object
			// int limit = articlesPerPage;
			// int offset = 0;
			String searchUrl = this.getFirstSearchURL(keyword, fromDate, toDate, articlesPerPage);
			Document searchResult = null;
			Elements articleElements = null;

			// Iterate over pagination
			while (searchUrl != null) {
				try {
					// Do not raise offset in first loop invocation
					// if (articleElements != null) {
					// offset += limit;
					// }

					// Parse HTML content
					// String searchUrl = this.getSearchURL(keyword, fromDate,
					// toDate, offset, limit);
					searchResult = Jsoup.connect(searchUrl).timeout(60000)
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
					if (articleElements.size() < articlesPerPage) {
						// if
						// (this.lessArticlesThanExpectedHook(articlesPerPage,
						// articleElements.size(), keyword)) {
						// break;
						// }
					}
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					this.log.severe("IOException when processing url " + searchUrl + ": "
							+ e.getMessage());
				}
				// Done processing this search page: get next search page URL
				// Returns null if no 'next' link can be found, breaking the
				// loop
				searchUrl = this.getNextSearchURL(searchResult);
			}
		}

		this.log.info("Finished scraping base url " + this.baseURL);

		// Process articles by filtering and populating, then return
		return this.processArticles(articles, fromDate, toDate);
	}

	/**
	 * Helper method used by {@link #searchArticles(String[], Date, Date)} to
	 * search for articles. By examining the response of the previous call to
	 * the search page, it determines the URL of the next page of the
	 * pagination. When the end of the pagination has been reached, {@code null}
	 * is returned.
	 * 
	 * By default, this method uses {@link #getNextPageSelector()} to determine
	 * the element pointing to the next page of the pagination, and returns the
	 * absolute URL in the {@code href} property of the first matched element,
	 * or {@code null} if no such element can be found. This behavior can be
	 * overridden in subclasses.
	 * 
	 * @param doc
	 *            the response of the previous query
	 * @return the URL of the next page to fetch, or {@code null} if the end of
	 *         the pagination has been reached
	 */
	protected String getNextSearchURL(Document doc) {
		if (doc == null) {
			return null;
		}

		Element element = doc.select(this.getNextPageSelector()).first();
		
		if (element == null) {
			return null;
		}
		
		return element.attr("abs:href");
	}

	/**
	 * Helper method used by {@link #searchArticles(String[], Date, Date)} to
	 * search for articles matching the given conditions. More specifically, it
	 * returns the http(s) query address pointing to the first {@code limit}
	 * articles which contain the {@code keyword} and were published between
	 * {@code fromDate} and {@code toDate}. All subsequent URLs are generated by
	 * {@link #getNextSearchURL(Document)}.
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
	protected abstract String getFirstSearchURL(String keyword, Date fromDate, Date toDate,
			int limit);

	/**
	 * Returns the selector used by {@link #getNextSearchURL(Document)} to
	 * determine the element holding the link to the next page of the
	 * pagination. For example, if the pagination was displayed in a structure
	 * as such
	 * 
	 * <pre>
	 * {@code
	 * <div class="pagination">
	 *  <span class="page back"><a href="...">...</a></span>
	 *  <span class="page"><a href="...">...</a>.</span>
	 *  ...
	 *  <span class="page next"><a href="...">...</a></span>
	 *  ...
	 * </div>
	 * }
	 * </pre>
	 * 
	 * an appropriate selector would be {@code div.pagination span.page.next a}.
	 * 
	 * @return
	 */
	protected abstract String getNextPageSelector();

}
