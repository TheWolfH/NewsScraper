package fetchers;

import helpers.ConnectionHelper;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import results.ApiResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import articles.*;

/**
 * Base class for all fetcher classes that obtain their results by the usage of
 * a dedicated API. By implementing (at least) all abstract methods provided by
 * this class and the {@link Fetcher} class, users can implement the search for
 * an arbitrary news provider. This class not only lays out the methods needed
 * in order to implement your own news provider, but provides rich pre-defined
 * functionality, namely the
 * {@link #searchArticles(String[], Date, Date, Class, String, int)} method,
 * which in most cases can simply be called with the appropriate parameters by
 * subclasses. However, in case the default functionality needs to be
 * customized, the {@link Fetcher#searchArticles(String[], Date, Date)} method
 * can also be implemented independently.
 * 
 * @author Jan Helge Wolf
 * 
 */
public abstract class ApiFetcher extends Fetcher {
	/**
	 * The API key needed to access the respective API. Should be set in the
	 * constructor of the respective subclass and should be part of
	 * {@link Fetcher#baseURL}, which is used by
	 * {@link Fetcher#getSearchURL(String, Date, Date, int, int)} to build the
	 * search url.
	 */
	protected String apiKey;

	/**
	 * Template method returning a {@link java.util.Map} of
	 * {@link articles.Article} objects representing articles that contain one
	 * or more of the strings in {@code keywords} and that were published on or
	 * after {@code fromDate} and on or before {@code toDate}. The method takes
	 * the urls provided by {@link #getSearchURL(String, Date, Date, int, int)},
	 * parses the JSON output into an instance of {@code resultClass} and
	 * extracts the articles using the {@link results.ApiResult#getArticles()}
	 * method. As many APIs impose a limit on the number of articles returned
	 * per call, pagination is supported via the {@code limit} parameter, which
	 * represents the number of articles per page and is passed into the
	 * {@link #getSearchURL(String, Date, Date, int, int)} method.
	 * 
	 * @param keywords
	 *            the keywords to be searched for
	 * @param fromDate
	 *            the earliest date an article may have been published on to be
	 *            returned
	 * @param toDate
	 *            the latest date an article may have been published on to be
	 *            returned
	 * @param resultClass
	 *            the class representing the result of a call to the respective
	 *            API
	 * @param rootElement
	 *            the name of the JSON property the result is wrapped into (e.g.
	 *            "response") or {@code null} if the result is not wrapped
	 * @param limit
	 *            the number of articles returned per API call
	 * @return a Map of {@link articles.Article} objects representing newspaper
	 *         articles mapped to their url
	 */
	protected Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate,
			Class<? extends ApiResult> resultClass, String rootElement, int limit) {
		this.log.info("Start fetching base url " + this.baseURL);

		// Article set to be returned
		Map<String, Article> articles = new HashMap<String, Article>();

		// Iterate over keywords, get API result for each keyword and add
		// articles to set
		for (String keyword : keywords) {
			this.log.info("Start fetching for keyword " + keyword);

			// Initialize offset (for pagination) and ApiResult object
			int offset = 0;
			ApiResult result = null;

			do {
				// Iterate over pagination until reaching last page
				try {
					// Do not raise offset in first loop invocation
					if (result != null) {
						offset += limit;
					}

					// Get output of API call
					String output = ConnectionHelper.getURLContent(this.getSearchURL(keyword,
							fromDate, toDate, offset, limit));

					// Convert API output to ZeitResult object
					ObjectMapper mapper = new ObjectMapper();

					// If rootElement is set, adjust mapping process to reflect
					// root element in JSON response
					if (rootElement == null) {
						result = mapper.readValue(output, resultClass);
					}
					else {
						ObjectReader reader = mapper.reader(resultClass).withRootName(rootElement);
						result = reader.readValue(output);
					}

					// Iterate over articles and add them to set
					for (Article article : result.getArticles()) {
						if (!articles.containsKey(article.getUrl())) {
							// Article has not been found yet: add keyword to
							// article, add article to map
							article.addKeyword(keyword);
							articles.put(article.getUrl(), article);
						}
						else {
							// Article was already found before, add keyword to
							// Article object in map
							articles.get(article.getUrl()).addKeyword(keyword);
						}
					}
				}
				catch (JsonProcessingException e) {
					// Problem when processing JSON output (malformed JSON or
					// JSON objects could not be mapped to object): Call hook
					// method which returns true if loop should be aborted
					// TODO
				}
				catch (IOException e) {
					// Low-level I/O exception (timeout etc.) - no sensible way
					// to handle here
					this.log.severe("IOException when processing url "
							+ this.getSearchURL(keyword, fromDate, toDate, offset, limit) + ": "
							+ e.getMessage());
				}
			} while (offset + limit < result.getNumArticles());
		}

		this.log.info("Finished fetching base url " + this.baseURL);
		
		// Process articles by filtering and populating, then return
		return this.processArticles(articles, fromDate, toDate);
	}
}
