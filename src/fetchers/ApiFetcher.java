package fetchers;

import helpers.ConnectionHelper;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import results.ApiResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import articles.*;

public abstract class ApiFetcher extends Fetcher {
	protected String apiKey;

	/** TODO handle limit and offset, mention in javadoc
	 * Template method returning a {@link java.util.Set} of
	 * {@link articles.Article} objects representing articles that contain one
	 * or more of the strings in {@code keywords} and that were published on or
	 * after {@code fromDate} and on or before {@code toDate}.
	 * 
	 * 
	 * @param keywords
	 * @param fromDate
	 * @param toDate
	 * @param resultClass
	 * @param rootElement
	 * @return
	 */
	protected Set<Article> searchArticles(String[] keywords, Date fromDate, Date toDate,
			Class<? extends ApiResult> resultClass, String rootElement) {
		// Article set to be returned
		Set<Article> set = new HashSet<Article>();

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
						set.add(article);
					}
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} while (offset + limit < result.getNumArticles() && offset < 100 /* false */);
		}

		this.populateArticleData(set);

		return set;
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
	protected Set<Article> searchArticles(String[] keywords, Date fromDate, Date toDate,
			Class<? extends ApiResult> resultClass) {
		return this.searchArticles(keywords, fromDate, toDate, resultClass, null);
	}
}
