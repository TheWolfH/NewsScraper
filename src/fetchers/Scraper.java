package fetchers;

import java.io.IOException;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
	 * {@link #createArticleFromUrlAndTitle(String, String)} factory method. All
	 * the methods mentioned above can be overridden in subclasses in order to
	 * customize the behavior.
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
	protected Set<Article> searchArticles(String[] keywords, Date fromDate, Date toDate,
			int articlesPerPage) {
		// Article set to be returned
		Set<Article> set = new HashSet<Article>();

		for (String keyword : keywords) {
			// Set limit and offset for pagination, initialize articleElements
			// object
			int limit = articlesPerPage;
			int offset = 0;
			Elements articleElements = null;

			do {
				try {
					// Do not raise offset in first loop invocation
					if (articleElements != null) {
						offset += limit;
					}

					// Parse HTML content
					String searchUrl = this.getSearchURL(keyword, fromDate, toDate, offset, limit);
					Document searchResult = Jsoup.connect(searchUrl).timeout(60000).get();
					articleElements = searchResult.select(this.getSearchResultsSelector());

					// Exit loop when no more articles are found
					if (articleElements.size() < 1) {
						break;
					}

					// Iterate over articleElements, generate Article objects
					// and add them to set
					for (Element articleElement : articleElements) {
						String url = this.getUrlFromSearchResult(articleElement);
						String title = this.getTitleFromSearchResult(articleElement);

						set.add(this.createArticleFromUrlAndTitle(url, title));
					}

					// Exit loop if less than limit articles were found (end of
					// results)
					if (articleElements.size() < limit) {
						break;
					}

					System.out.println("page");
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} while (true);
		}

		this.populateArticleData(set);

		return set;
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
	protected abstract Article createArticleFromUrlAndTitle(String url, String title);

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
