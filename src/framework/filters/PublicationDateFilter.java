package framework.filters;

import java.util.Date;
import java.util.Map;

import framework.articles.Article;

/**
 * A PostPopulatingArticleFilter implementation used to filter out articles
 * based on their publicationDate property. The specific behavior is defined by
 * the values passed to the constructor. This filter is especially useful when a
 * data source does not allow the specification of date boundaries in the search
 * query.
 * 
 * @author Jan Helge Wolf
 *
 */
public class PublicationDateFilter implements PostPopulatingArticleFilter {
	/**
	 * whether to exclude articles with a {@code null} publicationDate (true) or
	 * not (false)
	 */
	protected boolean filterNull;

	/**
	 * the earliest accepted date
	 */
	protected Date earliestDate;

	/**
	 * the latest accepted date
	 */
	protected Date latestDate;

	/**
	 * Constructs a {@code PublicationDateFilter} with the given properties.
	 * 
	 * @param filterNull
	 *            whether to disallow articles with a {@code null}
	 *            publicationDate (true) or not (false)
	 * @param earliestDate
	 *            the earliest accepted date by this filter, or {@code null} to
	 *            set no earliest data
	 * @param latestDate
	 *            the latest accepted date by this filter, or {@code null} to
	 *            set no latest date
	 */
	public PublicationDateFilter(boolean filterNull, Date earliestDate, Date latestDate) {
		this.filterNull = filterNull;
		this.earliestDate = earliestDate;
		this.latestDate = latestDate;
	}

	@Override
	public boolean test(Map.Entry<String, Article> t) {
		Article article = t.getValue();

		// Disallow null articles (see PostPopulatingArticleFilter
		// documentation)
		if (article == null) {
			return false;
		}

		Date publicationDate = article.getPublicationDate();

		if (publicationDate == null) {
			if (this.filterNull) {
				// publicationDate is null and nulls are prohibited
				return false;
			}

			// publicationDate is null and nulls are allowed: no more testing
			// possible
			return true;
		}

		// If this.earliestDate is set, compare with publicationDate
		if (this.earliestDate != null && publicationDate.before(this.earliestDate)) {
			return false;
		}

		// If this.latestDate is set, compare with publicationDate
		if (this.latestDate != null && publicationDate.after(this.latestDate)) {
			return false;
		}

		// No conflicts found
		return true;
	}
}
