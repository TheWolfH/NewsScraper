package fetchers;

import java.util.Date;
import java.util.Map;

import articles.Article;
import articles.DailyMailArticle;

public class DailyMailScraper extends Scraper {

	public DailyMailScraper() {
		this.baseURL = "http://www.dailymail.co.uk/home/search.html?sel=site&type=article";
	}

	@Override
	protected Article createArticle(String url, String title) {
		return new DailyMailArticle(url, title);
	}

	@Override
	protected Article createArticle(String url, String title, String keyword) {
		return new DailyMailArticle(url, title, keyword);
	}

	@Override
	protected String getSearchResultsSelector() {
		return "div#search div.sch-results div.sch-result";
	}

	@Override
	protected String getUrlSelector() {
		return "div.sch-res-content h3.sch-res-title a";
	}

	@Override
	protected String getTitleSelector() {
		return "div.sch-res-content h3.sch-res-title a";
	}

	@Override
	public Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate) {
		return super.searchArticles(keywords, fromDate, toDate, 50);
	}

	@Override
	protected String getSearchURL(String keyword, Date fromDate, Date toDate, int offset, int limit) {
		StringBuilder sb = new StringBuilder(this.baseURL);
		
		sb.append("&searchPhrase=");
		sb.append(keyword);
		
		sb.append("&size=");
		sb.append(limit);
		
		sb.append("&offset=");
		sb.append(offset);
		
		sb.append("&sort=");
		sb.append("recent");
		
		sb.append("&days=");
		sb.append("all");
		
		return sb.toString();
	}

}
