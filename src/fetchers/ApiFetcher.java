package fetchers;

import helpers.ConnectionHelper;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import results.ApiResult;

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
 * {@link #searchArticles(String[], Date, Date, Class, String)} method, which in
 * most cases can simply be called with the appropriate parameters by
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
	 * TODO handle numberPerPage, mention in javadoc Template method returning a
	 * {@link java.util.Set} of {@link articles.Article} objects representing
	 * articles that contain one or more of the strings in {@code keywords} and
	 * that were published on or after {@code fromDate} and on or before
	 * {@code toDate}.
	 * 
	 * 
	 * @param keywords
	 * @param fromDate
	 * @param toDate
	 * @param resultClass
	 * @param rootElement
	 * @return
	 */
	protected Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate,
			Class<? extends ApiResult> resultClass, String rootElement) {
		// Article set to be returned
		Map<String, Article> articles = new HashMap<String, Article>();

		// Iterate over keywords, get API result for each keyword and add
		// articles to set
		for (String keyword : keywords) {
			// Set limit and offset for pagination, initialize ApiResult object
			int limit = 10;
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
						articles.put(article.getUrl(), article);
					}
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} while (offset + limit < result.getNumArticles() && offset < 100 /* false */);
		}

		this.populateArticleData(articles);

		return articles;
	}

	/**
	 * Wrapper method for
	 * {@link #searchArticles(String[], Date, Date, Class, String)} in case the
	 * JSON delivered when performing the query returned by
	 * {@link fetchers.Fetcher#getSearchURL(String, Date, Date, int, int)} does
	 * not wrap the results in a root element.
	 * 
	 * @param keywords
	 * @param fromDate
	 * @param toDate
	 * @param resultClass
	 * @return
	 */
	protected Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate,
			Class<? extends ApiResult> resultClass) {
		return this.searchArticles(keywords, fromDate, toDate, resultClass, null);
	}
}
