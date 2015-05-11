package application.fetchers;

import java.util.Date;
import java.util.Map;

import org.jsoup.nodes.Document;

import application.articles.DailyExpressArticle;
import framework.articles.Article;
import framework.fetchers.ReactiveScraper;
import framework.filters.PostPopulatingArticleFilter;
import framework.filters.PublicationDateFilter;

public class DailyExpressScraper extends ReactiveScraper {
	// Due to the very special nature of the Daily Express search functionality,
	// this property is needed to check whether the keyword search page has
	// already been fetched (see #getNextSearchURL())
	protected boolean fetched;

	public DailyExpressScraper() {
		super();
		this.baseURL = "http://www.express.co.uk/search/";
		this.fetched = false;
	}

	@Override
	protected String getFirstSearchURL(String keyword, Date fromDate, Date toDate, int limit) {
		this.fetched = false;

		StringBuilder sb = new StringBuilder(this.baseURL);
		sb.append(keyword);
		sb.append("?s=");
		sb.append(keyword);

		return sb.toString();
	}

	// As the Daily Mail search functionality depends heavily on Ajax calls,
	// ReactiveScraper.getNextSearchURL() must be overwritten. This method
	// extracts the number of total matches and appends it as the offset ('o')
	// parameter to the search URL. The call made to this URL then displays all
	// search framework.results on one page. Due to the fact that even on that page, a
	// 'load more' button is present (which can only be gotten rid off by
	// performing an Ajax call, which Jsoup is not capable of), this.fetched is
	// used to track whether this is the first time this method has been called.
	// If that's not the case, null is returned, signaling that all framework.results have
	// been obtained. #getFirstSearchURL() resets that value to false so that
	// for each keyword, getNextSearchURL() can be successfully called exaxtly
	// once.
	@Override
	protected String getNextSearchURL(Document doc) {
		if (this.fetched) {
			return null;
		}

		// Get keyword and number of framework.results for URL construction, set
		// this.fetched
		this.fetched = true;
		String numResults = doc.select("form#search_form p.hint b").text().trim();
		String keyword = doc.select("form#search_form p.hint em").text().trim();

		StringBuilder sb = new StringBuilder(this.baseURL);
		sb.append(keyword);
		sb.append("?s=");
		sb.append(keyword);
		sb.append("&o=");
		sb.append(numResults);

		return sb.toString();
	}

	// Is not needed due to #getNextSearchURL() being overriden
	@Override
	protected String getNextPageSelector() {
		return null;
	}

	@Override
	protected Article createArticle(String url, String title) {
		return new DailyExpressArticle(url, title);
	}

	@Override
	protected Article createArticle(String url, String title, String keyword) {
		return new DailyExpressArticle(url, title, keyword);
	}

	@Override
	protected String getSearchResultsSelector() {
		return "section.search-results a.result-item";
	}

	@Override
	protected String getUrlSelector() {
		return "a.result-item";
	}

	@Override
	protected String getTitleSelector() {
		return "h4.post-title";
	}

	@Override
	public Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate) {
		return super.searchArticles(keywords, fromDate, toDate, 10);
	}

	// As the Daily Mail search functionality does not allow specifying a date
	// range, apply date filter after populating the articles
	@Override
	protected PostPopulatingArticleFilter getPostPopulatingArticleFilter(Date fromDate, Date toDate) {
		return new PublicationDateFilter(false, fromDate, toDate);
	}

}
