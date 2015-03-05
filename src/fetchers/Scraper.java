package fetchers;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import articles.Article;

public abstract class Scraper extends Fetcher {
	Document searchResult;

	public Set<Article> searchArticles(String[] keywords, Date fromDate, Date toDate,
			int articlesPerPage) {
		// Article set to be returned
		Set<Article> set = new HashSet<Article>();

		for (String keyword : keywords) {
			// Set limit and offset for pagination, initialize articleElements
			// object
			int limit = articlesPerPage;
			int offset = 0;
			Elements articleElements = null;

			do {
				try {
					// Do not raise offset in first loop invocation
					if (articleElements != null) {
						offset += limit;
					}

					// Parse HTML content
					String searchUrl = this.getSearchURL(keyword, fromDate, toDate, offset, limit);
					this.searchResult = Jsoup.connect(searchUrl).timeout(60000).get();
					articleElements = this.searchResult.select(this.getSearchResultsSelector());

					// Exit loop when no more articles are found
					if (articleElements.size() < 1) {
						break;
					}

					// Iterate over articleElements, generate Article objects
					// and add them to set
					for (Element articleElement : articleElements) {
						String url = this.getUrlFromSearchResult(articleElement);
						String title = this.getTitleFromSearchResult(articleElement);

						set.add(this.createArticleFromUrlAndTitle(url, title));
					}

					// Exit loop if less than limit articles were found (end of
					// results)
					if (articleElements.size() < limit) {
						break;
					}

					System.out.println("page");
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} while (true);
		}

		this.populateArticleData(set);

		return set;
	}

	protected String getUrlFromSearchResult(Element articleElement) {
		return articleElement.select(this.getUrlSelector()).attr("abs:href");
	}

	protected String getTitleFromSearchResult(Element articleElement) {
		return articleElement.select(this.getTitleSelector()).text();
	}

	/**
	 * 
	 * @return
	 */
	protected abstract Article createArticleFromUrlAndTitle(String url, String title);

	protected abstract String getSearchResultsSelector();

	protected abstract String getUrlSelector();

	protected abstract String getTitleSelector();

}
