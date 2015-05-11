package application.fetchers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.jsoup.nodes.Document;

import application.articles.WeltOnlineArticle;
import framework.articles.Article;
import framework.fetchers.ReactiveScraper;

public class WeltOnlineScraper extends ReactiveScraper {

	public WeltOnlineScraper() {
		this.baseURL = "http://suchen.welt.de/woa/search.do?outputs=80&wtmc=suche_main&mode=extended";
	}

	@Override
	protected Article createArticle(String url, String title) {
		return new WeltOnlineArticle(url, title);
	}

	@Override
	protected Article createArticle(String url, String title, String keyword) {
		return new WeltOnlineArticle(url, title, keyword);
	}

	@Override
	protected String getSearchResultsSelector() {
		return "div.SearchrResultList div.article";
	}

	@Override
	protected String getUrlSelector() {
		return "h4.headLine a";
	}

	@Override
	protected String getTitleSelector() {
		return "h4.headLine a";
	}

	@Override
	public Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate) {
		return super.searchArticles(keywords, fromDate, toDate, 10);
	}

	@Override
	protected String getFirstSearchURL(String keyword, Date fromDate, Date toDate, int limit) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
		StringBuilder sb = new StringBuilder(this.baseURL);

		sb.append("&search=");
		sb.append(keyword);

		sb.append("&date=");
		sb.append("period");

		sb.append("&dateFrom=");
		sb.append(formatter.format(fromDate));

		sb.append("&dateTo=");
		sb.append(formatter.format(toDate));

		sb.append("&order=");
		sb.append("date desc");

		// Does not seem to have any affect, yet included for possible future
		// compatibility
		sb.append("&length=");
		sb.append(limit);

		return sb.toString();
	}

	@Override
	protected String getNextSearchURL(Document doc) {
		String url = super.getNextSearchURL(doc);
		return (url == null ? null : url + "&outputs=80");
	}

	@Override
	protected String getNextPageSelector() {
		return "div.pagination span.page.next a";
	}

}
