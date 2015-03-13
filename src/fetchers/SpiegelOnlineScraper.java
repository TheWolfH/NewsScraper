package fetchers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import exporters.Exporter;
import articles.Article;
import articles.SpiegelOnlineArticle;

public class SpiegelOnlineScraper extends Scraper {

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
		sb.append((int) offset / limit);

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

	public static void main(String[] args) {
		Date start = new Date();
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

		SpiegelOnlineScraper fetcher = new SpiegelOnlineScraper();
		Map<String, Article> articles = fetcher.searchArticles(new String[] { "Snowden", "NSA" },
				fromDate, toDate);

		/*
		 * for (Article article : articles) {
		 * System.out.println(article.getFullText().substring(0, 100)); }
		 */

		Exporter exporter = new Exporter(articles);

		System.out.println(articles.size());
		Date end = new Date();
		System.out.println(end.getTime() - start.getTime());
	}
}
