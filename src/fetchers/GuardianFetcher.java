package fetchers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import exporters.Exporter;
import results.GuardianResult;
import articles.Article;

public class GuardianFetcher extends ApiFetcher {

	public GuardianFetcher() {
		this.apiKey = helpers.Api.GUARDIAN.getKey();
		this.baseURL = "http://content.guardianapis.com/search?api-key=" + this.apiKey;
	}

	@Override
	protected String getSearchURL(String keyword, Date fromDate, Date toDate, int offset, int limit) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		StringBuilder sb = new StringBuilder(this.baseURL);

		// Keyword
		sb.append("&q=");
		sb.append(keyword);

		// Date range
		sb.append("&from-date=");
		sb.append(formatter.format(fromDate));
		sb.append("&to-date=");
		sb.append(formatter.format(toDate));

		// Pagination: Guardian API uses pages (1-based counting) instead of offsets
		sb.append("&page=");
		sb.append((int) (offset / limit + 1));

		sb.append("&page-size=");
		sb.append(limit);
		
		// Order by publication date
		sb.append("&order-by=newest");
		sb.append("&use-date=published");

		// Indicate additional fields (subtitle and fullText)
		sb.append("&show-fields=trailText,body");

		return sb.toString();
	}

	@Override
	public Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate) {
		return super.searchArticles(keywords, fromDate, toDate, GuardianResult.class, "response");
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

		GuardianFetcher fetcher = new GuardianFetcher();
		Map<String, Article> articles = fetcher.searchArticles(new String[] { "Snowden" },
				fromDate, toDate);

		Exporter exporter = new Exporter(articles);

		System.out.println(articles.size());
		Date end = new Date();
		System.out.println(end.getTime() - start.getTime());
	}

}
