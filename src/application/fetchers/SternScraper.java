package application.fetchers;

import java.util.Date;
import java.util.Map;

import application.articles.SternArticle;
import framework.articles.Article;
import framework.fetchers.PredictiveScraper;

public class SternScraper extends PredictiveScraper {

	public SternScraper() {
		this.baseURL = "http://wefind.stern.de/suche?extendedSearch=on";
	}

	@Override
	protected Article createArticle(String url, String title) {
		return new SternArticle(url, title);
	}

	@Override
	protected Article createArticle(String url, String title, String keyword) {
		return new SternArticle(url, title, keyword);
	}

	@Override
	protected String getSearchResultsSelector() {
		return "div#main div#boxArchiveContent1 div.moduleL17";
	}

	@Override
	protected String getUrlSelector() {
		return "a.h2";
	}

	@Override
	protected String getTitleSelector() {
		return "a.h2 span.boxHeadline";
	}

	@Override
	public Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate) {
		return super.searchArticles(keywords, fromDate, toDate, 10);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected String getSearchURL(String keyword, Date fromDate, Date toDate, int offset, int limit) {
		StringBuilder sb = new StringBuilder(this.baseURL);
		
		sb.append("&query=");
		sb.append(keyword);
		
		sb.append("&datehistogram=");
		sb.append("range");
		
		sb.append("&rangeFromDay=");
		sb.append(fromDate.getDate());
		sb.append("&rangeFromMonth=");
		sb.append(fromDate.getMonth() + 1);
		sb.append("&rangeFromYear=");
		sb.append(fromDate.getYear() + 1900);
		
		sb.append("&rangeToDay=");
		sb.append(toDate.getDate());
		sb.append("&rangeToMonth=");
		sb.append(toDate.getMonth() + 1);
		sb.append("&rangeToYear=");
		sb.append(toDate.getYear() + 1900);
		
		sb.append("&format=");
		sb.append("Artikel");
		
		sb.append("&pageIndex=");
		sb.append((int) offset / limit);
		
		return sb.toString();
	}

}
