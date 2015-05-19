package application.fetchers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import application.articles.MirrorArticle;
import framework.articles.Article;
import framework.fetchers.PredictiveScraper;
import framework.filters.PrePopulatingArticleFilter;
import framework.filters.URLSuffixFilter;

public class MirrorScraper extends PredictiveScraper {

	public MirrorScraper() {
		this.baseURL = "http://www.mirror.co.uk/search/advanced.do?destinationSectionId=219&publicationName=mirror";
	}

	@Override
	protected Article createArticle(String url, String title) {
		return new MirrorArticle(url, title);
	}

	@Override
	protected Article createArticle(String url, String title, String keyword) {
		return new MirrorArticle(url, title, keyword);
	}

	@Override
	protected String getSearchResultsSelector() {
		return "div.search-results div.article:not(.no-results)";
	}

	@Override
	protected String getUrlSelector() {
		return "h3 a";
	}

	@Override
	protected String getTitleSelector() {
		return "h3 a";
	}

	@Override
	public Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate) {
		return super.searchArticles(keywords, fromDate, toDate, 50);
	}

	@Override
	protected String getSearchURL(String keyword, Date fromDate, Date toDate, int offset, int limit) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		StringBuilder sb = new StringBuilder(this.baseURL);

		sb.append("&searchString=");
		sb.append(keyword);

		sb.append("&dateRange=");
		sb.append(formatter.format(fromDate));
		sb.append(" TO ");
		sb.append(formatter.format(toDate));

		sb.append("&pageLength=");
		sb.append(limit);

		sb.append("&pageNumber=");
		sb.append(((int) offset / limit) + 1);

		sb.append("&sortString=");
		sb.append("publishdate");
		sb.append("&sortOrder=");
		sb.append("desc");

		return sb.toString();
	}

	// Provide filter preventing useless articles whose URL end with ".ece"
	// (e.g. http://www.mirror.co.uk/incoming/article2038719.ece) from being
	// regarded
	@Override
	protected PrePopulatingArticleFilter getPrePopulatingArticleFilter() {
		return new URLSuffixFilter(".ece");
	}

}
