package fetchers;

import java.util.Date;
import java.util.Map;

import org.jsoup.nodes.Element;

import filters.PostPopulatingArticleFilter;
import filters.PublicationDateFilter;
import articles.Article;
import articles.SueddeutscheArticle;

public class SueddeutscheScraper extends PredictiveScraper {

	public SueddeutscheScraper() {
		super();
		this.baseURL = "http://suche.sueddeutsche.de/query/";
	}

	@Override
	protected String getSearchURL(String keyword, Date fromDate, Date toDate, int offset, int limit) {
		StringBuilder sb = new StringBuilder(this.baseURL);

		sb.append(keyword);

		// Articles only (no image galleries etc.), sort by publication date
		sb.append("/nav/§documenttype:Artikel/sort/-docdatetime");

		sb.append("/page/");
		sb.append(((int) offset / limit) + 1);

		return sb.toString();
	}

	@Override
	protected Article createArticle(String url, String title) {
		return new SueddeutscheArticle(url, title);
	}

	@Override
	protected Article createArticle(String url, String title, String keyword) {
		return new SueddeutscheArticle(url, title, keyword);
	}

	@Override
	protected String getSearchResultsSelector() {
		return "div#sitecontent.search div.content div.teaser";
	}

	@Override
	protected String getUrlSelector() {
		return "a.entry-title";
	}

	// Sueddeutsche search result structure:
	// <div class="teaser">
	// <a class="entry-title">
	// <strong>Overline</strong>
	// Actual headline
	// <span class="department">Department</span>
	// </a>
	// </div>
	//
	// In order to fetch the actual headline without the overline and, most
	// importantly, the headline, ownText() must be used instead of text()
	@Override
	protected String getTitleFromSearchResult(Element articleElement) {
		return articleElement.select(this.getTitleSelector()).first().ownText();
	}

	@Override
	protected String getTitleSelector() {
		return "a.entry-title";
	}

	@Override
	public Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate) {
		return super.searchArticles(keywords, fromDate, toDate, 15);
	}

	// As Sueddeutsche does not allow specifying a date range, apply date filter
	// after populating articles
	@Override
	protected PostPopulatingArticleFilter getPostPopulatingArticleFilter(Date fromDate, Date toDate) {
		return new PublicationDateFilter(false, fromDate, toDate);
	}

}
