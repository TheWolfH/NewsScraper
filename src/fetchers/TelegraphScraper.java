package fetchers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import articles.Article;
import articles.TelegraphArticle;

public class TelegraphScraper extends Scraper {

	public TelegraphScraper() {
		this.baseURL = "http://www.telegraph.co.uk/template/ver1-0/templates/fragments/otsn/results.jsp?fq[]=type:Article&sort=recent&paging=true&ajax=true";
	}

	@Override
	public Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate) {
		return super.searchArticles(keywords, fromDate, toDate, 20);
	}

	@Override
	protected String getSearchURL(String keyword, Date fromDate, Date toDate, int offset, int limit) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		StringBuilder sb = new StringBuilder(this.baseURL);

		sb.append("&queryText=");
		sb.append(keyword);

		sb.append("&range=");
		sb.append(formatter.format(fromDate));

		sb.append("&rangeTo=");
		sb.append(formatter.format(toDate));
		
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

	public static void main(String[] args) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		Date fromDate = null;
		Date toDate = null;
		try {
			fromDate = format.parse("2013-01-01");
			toDate = format.parse("2014-12-31");
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		
		TelegraphScraper scraper = new TelegraphScraper();
		scraper.searchArticles(new String[] { "Snowden" }, fromDate, toDate);
	}
}
