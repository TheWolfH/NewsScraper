package fetchers;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import articles.Article;

public abstract class Fetcher {
	protected String baseURL;

	/**
	 * Asynchronously populates all fields on the articles in the given
	 * {@code set} by calling the {@link articles.Article#populateData()} method
	 * on each article.
	 * 
	 * @param set
	 *            the set of articles to be populated
	 */
	protected void populateArticleData(Set<Article> set) {
		// ExecutorService to asynchronously get article fullTexts
		ExecutorService fullTextFetcher = Executors.newFixedThreadPool(8);
		Set<Future<Void>> futures = new HashSet<Future<Void>>();

		// Iterate over all articles found and asynchronously populate fullText
		// fields
		for (final Article article : set) {
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
			}
			catch (InterruptedException e) {
				// Exception due to interruption
				// TODO add generic exception?
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
	 * @return a Set of {@link articles.Article} objects representing newspaper
	 *         articles
	 */
	public abstract Set<Article> searchArticles(String[] keywords, Date fromDate, Date toDate);

	/**
	 * Helper method used by {@link #searchArticles(String[], Date, Date)} to
	 * search for articles matching the given conditions. More specifically, it
	 * returns the http(s) query address that needs to be called to obtain at
	 * least the url and the title of the articles number {@code offset} to
	 * {@code offset+limit} (in a zero-based counting) which contain the
	 * {@code keyword} and where published between {@code fromDate} and
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
