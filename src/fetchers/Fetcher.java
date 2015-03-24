package fetchers;

import filters.PostPopulatingArticleFilter;
import filters.PrePopulatingArticleFilter;
import helpers.ConfigReader;
import helpers.LoggerGenerator;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import articles.Article;

/**
 * Abstract base class for all fetcher classes. Entry point is the
 * {@link #searchArticles(String[], Date, Date)} method, which performs the
 * action of actually searching for articles by keywords and the given time
 * frame. In order to implement a new news provider, one of the direct
 * subclasses of this class must be extended, implementing at least all abstract
 * methods provided. The exact methods to be implemented vary on whether the
 * provider's articles are accessed via a dedicated API (ApiFetcher) or by
 * leveraging the site's common search functionality (Scraper). The provided
 * {@link #populateArticleData(Map)} is leveraged by the
 * {@code searchArticles(...)} methods pre-defined in the direct subclasses, but
 * can also be used when implementing your own version of
 * {@link #searchArticles(String[], Date, Date)}.
 * 
 * @author Jan Helge Wolf
 * 
 */
public abstract class Fetcher {
	/**
	 * The constant part of the url leading to the search functionality on the
	 * website of the news provider. Must be set in the constructor of the
	 * respective subclass and should contain all constant parts of the url,
	 * including the protocol, hostname, path, and all constant query parts
	 * (e.g. ordering, filters in order to search for only one publication of
	 * this publisher etc.). {@link #getSearchURL(String, Date, Date, int, int)}
	 * should use this property as the basis for the complete search url.
	 */
	protected String baseURL;

	/**
	 * Internal logging utility. Can and should be used by subclasses to provide
	 * feedback to the user in case of any errors.
	 */
	protected final Logger log = LoggerGenerator.getLogger();

	/**
	 * Asynchronously populates all fields on the articles in the given
	 * {@code set} by calling the {@link articles.Article#populateData()} method
	 * on each article.
	 * 
	 * @param articles
	 *            the set of articles to be populated
	 */
	protected void populateArticleData(Map<String, Article> articles) {
		// ExecutorService to asynchronously get article fullTexts
		int numThreads = Integer.parseInt(ConfigReader.getConfig().getProperty(
				"Fetcher.populateArticleData.numThreads", "32"));
		ExecutorService fullTextFetcher = Executors.newFixedThreadPool(numThreads);
		Set<Future<Void>> futures = new HashSet<Future<Void>>();

		// Iterate over all articles found and asynchronously populate fullText
		// fields
		for (final Article article : articles.values()) {
			Future<Void> future = fullTextFetcher.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					// TODO Auto-generated method stub
					article.populateData();
					return null;
				}
			});

			futures.add(future);
		}

		// Synchronize and close all threads
		for (Future<Void> future : futures) {
			try {
				future.get();
			}
			catch (ExecutionException e) {
				// Exception thrown by article.populateData()
				// TODO add error logging
				this.log.warning("Exception thrown when trying to populate article data: "
						+ e.getCause().toString());
			}
			catch (InterruptedException e) {
				// Exception due to interruption
				// TODO add generic exception?
				e.printStackTrace();
			}
		}

		try {
			fullTextFetcher.shutdown();
			fullTextFetcher.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Processes the passed articles by first filtering them using
	 * {@link #applyPrePopulatingFilter(Map)}, then calling
	 * {@link #populateArticleData(Map)} and then filtering again using
	 * {@link #applyPostPopulatingFilter(Map, Date, Date)}.
	 * 
	 * @param articles
	 *            the articles to process
	 * @param fromDate
	 *            the {@code fromDate} parameter that is passed on to
	 *            {@link #applyPrePopulatingFilter(Map)}
	 * @param toDate
	 *            the {@code fromDate} parameter that is passed on to
	 *            {@link #applyPostPopulatingFilter(Map, Date, Date)}
	 * @return the processed articles
	 */
	protected Map<String, Article> processArticles(Map<String, Article> articles, Date fromDate,
			Date toDate) {
		this.log.info("Start processing articles for base url " + this.baseURL);

		// Filter articles by URL in order not to populate undesired articles
		// (save network calls)
		this.log.fine("Number of articles before PrePopulatingFilter: " + articles.size());
		articles = this.applyPrePopulatingFilter(articles);
		this.log.fine("Number of articles after PrePopulatingFilter: " + articles.size());

		// Populate articles with additional data
		this.log.info("Start populating article data for base url " + this.baseURL);
		this.populateArticleData(articles);
		this.log.info("Finished populating article data for base url " + this.baseURL);

		// Filter articles by all properties
		this.log.fine("Number of articles before PostPopulatingFilter: " + articles.size());
		articles = this.applyPostPopulatingFilter(articles, fromDate, toDate);
		this.log.fine("Number of articles after PostPopulatingFilter: " + articles.size());

		// Return articles
		this.log.info("Finished processing article for base url " + this.baseURL
				+ ", returning articles");
		return articles;
	}

	/**
	 * This method is called by {@link #processArticles(Map, Date, Date)} after
	 * collecting articles, but before populating them. It uses the
	 * PrePopulatingArticleFilter provided by
	 * {@link #getPrePopulatingArticleFilter()} to filter out undesired articles
	 * based on their URL. By default, {@link #getPrePopulatingArticleFilter()}
	 * returns {@code null} so that the articles are not filtered. Subclasses
	 * can override {@link #getPrePopulatingArticleFilter()} to, for example,
	 * filter out articles with specific URL suffixes using a
	 * {@link filters.URLSuffixFilter}.
	 * 
	 * @param articles
	 *            the articles collected by
	 *            {@link #searchArticles(String[], Date, Date)}
	 * @return the desired articles to be populated
	 */
	protected Map<String, Article> applyPrePopulatingFilter(Map<String, Article> articles) {
		PrePopulatingArticleFilter filter = this.getPrePopulatingArticleFilter();

		// null returned: no filtering
		if (filter == null) {
			return articles;
		}

		// Get all keys (URLs) from the article map, apply provided filter, then
		// use remaining URLs to get the respective Article objects from
		// the old map in order to build the new map, which is then returned
		return articles.keySet().parallelStream().filter(filter)
				.collect(Collectors.toMap(url -> url, url -> articles.get(url)));
	}

	/**
	 * Returns the PrePopulatingArticleFilter used by
	 * {@link #applyPrePopulatingFilter(Map)}. By default, {@code null} is
	 * returned so that no filter is applied. Can be overridden by subclasses to
	 * enable filtering based on the URL of the article.
	 * 
	 * @return the PrePopulatingArticleFilter used to filter out undesired
	 *         articles
	 */
	protected PrePopulatingArticleFilter getPrePopulatingArticleFilter() {
		return null;
	}

	/**
	 * This method is called by {@link #processArticles(Map, Date, Date)} after
	 * populating all collected articles, but before returning them. It uses the
	 * PostPopulatingArticleFilter provided by
	 * {@link #getPostPopulatingArticleFilter(Date, Date)} to filter out
	 * undesired articles based on their properties. By default,
	 * {@link #getPostPopulatingArticleFilter(Date, Date)} returns {@code null}
	 * so that the articles are not filtered. Subclasses can override
	 * {@link #getPostPopulatingArticleFilter(Date, Date)} to, for example,
	 * filter out articles with certain publication dates. This is particularly
	 * useful with data sources that do not allow the specification of a valid
	 * date range in their search queries.
	 * 
	 * @param articles
	 *            the articles collected by
	 *            {@link #searchArticles(String[], Date, Date)}
	 * @param fromDate
	 *            the {@code fromDate} parameter passed to
	 *            {@link #searchArticles(String[], Date, Date)}
	 * @param toDate
	 *            the {@code toDate} parameter passed to
	 *            {@link #searchArticles(String[], Date, Date)}
	 * @return the desired articles
	 */
	protected Map<String, Article> applyPostPopulatingFilter(Map<String, Article> articles,
			Date fromDate, Date toDate) {
		PostPopulatingArticleFilter filter = this.getPostPopulatingArticleFilter(fromDate, toDate);

		// null returned: no filtering
		if (filter == null) {
			return articles;
		}

		// Get all Map.Entry objects from the article map, apply provided
		// filter, then build new map, which is then returned
		return articles.entrySet().parallelStream().filter(filter)
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
	}

	/**
	 * Returns the PostPopulatingArticleFilter used by
	 * {@link #applyPostPopulatingFilter(Map, Date, Date)}. By default,
	 * {@code null} is returned so that no filter is applied. Can be overridden
	 * by subclasses to enable filtering based on the URL of the article.
	 * 
	 * @param fromDate
	 *            the {@code fromDate} parameter passed to
	 *            {@link #searchArticles(String[], Date, Date)}
	 * @param toDate
	 *            the {@code toDate} parameter passed to
	 *            {@link #searchArticles(String[], Date, Date)}
	 * 
	 * @return the PostPopulatingArticleFilter used to filter out undesired
	 *         articles
	 */
	protected PostPopulatingArticleFilter getPostPopulatingArticleFilter(Date fromDate, Date toDate) {
		return null;
	}

	/**
	 * Searches for articles containing any of element of {@code keywords} that
	 * were published between {@code fromDate} and {@code toDate}, inclusive.
	 * Returns a {@link java.util.Set} of {@link articles.Article} elements
	 * matching the above conditions.
	 * 
	 * @param keywords
	 *            the keywords to be searched for
	 * @param fromDate
	 *            the earliest date an article may have been published on to be
	 *            returned
	 * @param toDate
	 *            the latest date an article may have been published on to be
	 *            returned
	 * @return a Map of {@link articles.Article} objects representing newspaper
	 *         articles mapped to their url
	 */
	public abstract Map<String, Article> searchArticles(String[] keywords, Date fromDate,
			Date toDate);
}
