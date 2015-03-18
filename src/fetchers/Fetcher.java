package fetchers;

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
 * {@link #populateArticleData(Set)} is leveraged by the
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
	protected final Logger log = LoggerGenerator.getLoggerGenerator().getLogger();

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
		ExecutorService fullTextFetcher = Executors.newFixedThreadPool(32);
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

	/**
	 * Helper method used by {@link #searchArticles(String[], Date, Date)} to
	 * search for articles matching the given conditions. More specifically, it
	 * returns the http(s) query address that needs to be called to obtain at
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
}
