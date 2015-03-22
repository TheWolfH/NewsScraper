package fetchers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import articles.Article;
import articles.SpiegelArticle;

public class SpiegelScraper extends Scraper {

	public SpiegelScraper() {
		this.baseURL = "http://www.spiegel.de/suche/index.html?quellenGroup=SP";
	}

	@Override
	protected Article createArticle(String url, String title) {
		return new SpiegelArticle(url, title);
	}

	@Override
	protected Article createArticle(String url, String title, String keyword) {
		return new SpiegelArticle(url, title, keyword);
	}

	@Override
	protected String getSearchResultsSelector() {
		return "#content-main .column-wide.spSearchPage .search-teaser";
	}

	@Override
	protected String getUrlSelector() {
		return "a";
	}

	@Override
	protected String getTitleSelector() {
		return ".headline";
	}

	@Override
	public Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate) {
		return super.searchArticles(keywords, fromDate, toDate, 20);
	}

	@Override
	protected String getSearchURL(String keyword, Date fromDate, Date toDate, int offset, int limit) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
		StringBuilder sb = new StringBuilder(this.baseURL);

		sb.append("&suchbegriff=");
		sb.append(keyword);

		sb.append("&fromDate=");
		sb.append(formatter.format(fromDate));

		sb.append("&toDate=");
		sb.append(formatter.format(toDate));

		sb.append("&pageNumber=");
		sb.append(((int) offset / limit) + 1);

		sb.append("&offsets=");
		sb.append(offset);

		return sb.toString();
	}

	/**
	 * Call super method for logging, return false so that the current keyword
	 * loop is not aborted (Spiegel search does not reliably return 20 results
	 * per page).
	 * 
	 * @see Scraper#lessArticlesThanExpectedHook(int, int, String)
	 */
	@Override
	protected boolean lessArticlesThanExpectedHook(int expectedArticles, int actualArticles,
			String keyword) {
		this.log.fine("Found less articles than expected (" + actualArticles + "/"
				+ expectedArticles + "), continue searching");

		return false;
	}
}
