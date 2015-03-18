package fetchers;

import java.util.Date;
import java.util.Map;

import articles.Article;
import articles.TagesspiegelArticle;

public class TagesspiegelScraper extends Scraper {

	public TagesspiegelScraper() {
		this.baseURL = "http://www.tagesspiegel.de/suchergebnis/artikel/";
	}

	@Override
	protected Article createArticle(String url, String title) {
		return new TagesspiegelArticle(url, title);
	}

	@Override
	protected Article createArticle(String url, String title, String keyword) {
		return new TagesspiegelArticle(url, title, keyword);
	}

	@Override
	protected String getSearchResultsSelector() {
		return "div.hcf-result > ul.hcf-teaser-list > li.hcf-teaser:not(.hcf-hidden):has(h2)";
	}

	@Override
	protected String getUrlSelector() {
		return "h2 a";
	}

	@Override
	protected String getTitleSelector() {
		return "h2 a span.hcf-headline";
	}

	@Override
	public Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate) {
		return super.searchArticles(keywords, fromDate, toDate, 20);
	}

	@Override
	@SuppressWarnings("deprecation")
	protected String getSearchURL(String keyword, Date fromDate, Date toDate, int offset, int limit) {
		StringBuilder sb = new StringBuilder(this.baseURL);

		sb.append("?sw=");
		sb.append(keyword);
		
		sb.append("&search-fromday=");
		sb.append(fromDate.getDate());
		sb.append("&search-frommonth=");
		sb.append(fromDate.getMonth()+1);
		sb.append("&search-fromyear=");
		sb.append(fromDate.getYear()+1900);
		
		sb.append("&search-today=");
		sb.append(toDate.getDate());
		sb.append("&search-tomonth=");
		sb.append(toDate.getMonth()+1);
		sb.append("&search-toyear=");
		sb.append(toDate.getYear()+1900);		
		
		sb.append("&p9049616=");
		sb.append(((int) offset / limit) + 1);

		return sb.toString();
	}

}
