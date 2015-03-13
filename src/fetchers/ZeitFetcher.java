package fetchers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import exporters.Exporter;
import results.ZeitResult;
import articles.*;

public class ZeitFetcher extends ApiFetcher {

	public ZeitFetcher() {
		this.apiKey = helpers.Api.ZEIT.getKey();
		this.baseURL = "http://api.zeit.de/content?api_key=" + this.apiKey;
	}

	@Override
	public Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate) {
		return super.searchArticles(keywords, fromDate, toDate, ZeitResult.class);
	}

	@Override
	protected String getSearchURL(String keyword, Date fromDate, Date toDate, int offset, int limit) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		StringBuilder sb = new StringBuilder(this.baseURL);

		// Keyword
		sb.append("&q=");
		sb.append(keyword);

		// Date range
		sb.append(" AND release_date:[");
		sb.append(formatter.format(fromDate));
		sb.append(" TO ");
		sb.append(formatter.format(toDate));
		sb.append("]");

		// Pagination
		sb.append("&offset=");
		sb.append(offset);

		sb.append("&limit=");
		sb.append(limit);

		// Order by publication date
		sb.append("&sort=release_date desc");

		// Indicate fields to be returned in order to speed up query
		// (see http://developer.zeit.de/docs/)
		sb.append("&fields=href,title,subtitle,release_date");

		return sb.toString();
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

		ZeitFetcher fetcher = new ZeitFetcher();
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
