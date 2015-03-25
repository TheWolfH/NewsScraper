package fetchers;

import java.util.Date;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import articles.Article;
import articles.FrankfurterAllgemeineArticle;

public class FrankfurterAllgemeineScraper extends PredictiveScraper {

	public FrankfurterAllgemeineScraper() {
		super();

		// String for use with String.format due to the special format of the
		// search URL
		this.baseURL = "http://www.faz.net/suche/s%4$d.html?BTyp=redaktionelleInhalte&chkBoxType_2=on&sort=date"
				+ "&query=%1$s&from=%2$td.%2$tm.%2$tY&to=%3$td.%3$tm.%3$tY&resultsPerPage=%5$d";
	}

	@Override
	protected String getSearchURL(String keyword, Date fromDate, Date toDate, int offset, int limit) {
		int pageNumber = ((int) offset / limit) + 1;
		String s = String.format(this.baseURL, keyword, fromDate, toDate, pageNumber, limit);
		return s;
	}

	@Override
	protected Article createArticle(String url, String title) {
		return new FrankfurterAllgemeineArticle(url, title);
	}

	@Override
	protected Article createArticle(String url, String title, String keyword) {
		return new FrankfurterAllgemeineArticle(url, title, keyword);
	}

	@Override
	protected String getSearchResultsSelector() {
		return "form#search div.SuchergebnisListe div.Teaser620";
	}

	// We need to perform some tricks in order to get around the FAZ paywall.
	// Recent articles can be accessed freely and are displayed in the search
	// results with a link. Older articles are not linked, one is supposed to
	// purchase a subscription. Nevertheless, the article Id can be extracted
	// from the link to the subscription page. This Id can then be used to open
	// the link to the article, which is - for whatever reason - freely
	// accessible.
	@Override
	protected String getUrlFromSearchResult(Element articleElement) {
		// First case: article is accessible via plain link
		Elements urlElements = articleElement.select(this.getUrlSelector());

		if (urlElements.size() > 0) {
			return urlElements.attr("abs:href");
		}

		// Second case: extract article Id from subscription page URL and return
		// direct article URL
		urlElements = articleElement.select("div.ArchivInfo a.ArchivLink");
		String articleId = urlElements.first().attr("href").split("=")[1];

		// For some reason, there is a dot in the article Id that's not in the
		// article URL
		articleId = articleId.replace(".", "");

		// Return article URL with parameter to enforce displaying the whole
		// article on one page
		return "http://www.faz.net/-" + articleId + ".html?printPagedArticle=true";
	}

	@Override
	protected String getUrlSelector() {
		return "a.TeaserHeadLink";
	}

	@Override
	protected String getTitleSelector() {
		return "span.headline";
	}

	@Override
	public Map<String, Article> searchArticles(String[] keywords, Date fromDate, Date toDate) {
		return super.searchArticles(keywords, fromDate, toDate, 80);
	}

}
