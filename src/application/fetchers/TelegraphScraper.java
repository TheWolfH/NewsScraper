package application.fetchers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import application.articles.TelegraphArticle;
import framework.articles.Article;
import framework.fetchers.PredictiveScraper;

public class TelegraphScraper extends PredictiveScraper {

	public TelegraphScraper() {
		this.baseURL = "http://www.telegraph.co.uk/template/ver1-0/templates/fragments/otsn/results.jsp?fq[]=type:Article&sort=recent&paging=true&ajax=true";
	}

	@Override
	public Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate) {
		return super.searchArticles(keywords, fromDate, toDate, 20);
	}

	@Override
	protected String getSearchURL(String keyword, Date fromDate, Date toDate, int offset, int limit) {
		SimpleDateFormat fromDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat toDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.'999Z'");
		StringBuilder sb = new StringBuilder(this.baseURL);

		sb.append("&queryText=");
		sb.append(keyword);

		sb.append("&range=");
		sb.append(fromDateFormatter.format(fromDate));

		sb.append("&rangeTo=");
		sb.append(toDateFormatter.format(toDate));
		
		sb.append("&p=");
		sb.append((int) (offset / limit + 1));

		sb.append("&limit=");
		sb.append(limit);

		return sb.toString();
	}

	@Override
	protected String getSearchResultsSelector() {
		return "ul.searchresults li.searchresult";
	}

	@Override
	protected Article createArticle(String url, String title) {
		return new TelegraphArticle(url, title);
	}
	
	@Override
	protected Article createArticle(String url, String title, String keyword) {
		return new TelegraphArticle(url, title, keyword);
	}
	
	@Override
	protected String getUrlSelector() {
		return "div h3 a";
	}

	@Override
	protected String getTitleSelector() {
		return "div h3 a";
	}
}
