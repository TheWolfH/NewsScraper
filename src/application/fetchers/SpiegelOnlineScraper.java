package application.fetchers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import application.articles.SpiegelOnlineArticle;
import framework.articles.Article;
import framework.fetchers.PredictiveScraper;

public class SpiegelOnlineScraper extends PredictiveScraper {

	public SpiegelOnlineScraper() {
		this.baseURL = "http://www.spiegel.de/suche/index.html?quellenGroup=SPOX";
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

	@Override
	protected String getSearchResultsSelector() {
		return "#content-main .column-wide.spSearchPage .search-teaser";
	}

	@Override
	protected Article createArticle(String url, String title) {
		return new SpiegelOnlineArticle(url, title);
	}

	@Override
	protected Article createArticle(String url, String title, String keyword) {
		return new SpiegelOnlineArticle(url, title, keyword);
	}
	
	@Override
	protected String getUrlSelector() {
		return "a";
	}

	@Override
	protected String getTitleSelector() {
		return ".headline";
	}
}
