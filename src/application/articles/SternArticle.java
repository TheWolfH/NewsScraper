package application.articles;

import java.util.Locale;

import framework.articles.ScrapedArticle;

public class SternArticle extends ScrapedArticle {

	public SternArticle(String url, String title) {
		super(url, title);
	}

	public SternArticle(String url, String title, String keyword) {
		super(url, title, keyword);
	}

	@Override
	protected String getSubtitleSelector() {
		return "p#div_article_intro";
	}

	@Override
	protected String getPublicationDateSelector() {
		return "div#main.pageArticle div.datePublished";
	}

	@Override
	protected String getPublicationDateFormat() {
		return "d. MMM yyyy, HH:mm";
	}

	@Override
	protected Locale getLocale() {
		return Locale.GERMANY;
	}

	@Override
	protected String getFullTextSelector() {
		// 1st line: standard case
		// 2nd line: Pro/Contra articles (e.g.
		// http://www.stern.de/politik/deutschland/pro-und-contra-asyl-fuer-snowden-2068514.html)
		// 3rd line: Interview style (e.g.
		// http://www.stern.de/politik/deutschland/stroebele-zum-abhoerskandal-ich-bestreite-dass-anschlaege-in-deutschland-verhindert-wurden-2041646.html)
		return "div#main.pageArticle div[itemprop=\"mainContentOfPage\"] span[itemprop=\"articleBody\"],"
				+ "div#main.pageArticle div.boxTabProContra ~ div.boxContent,"
				+ "div#main.pageArticle div#div_module_xl7 div.moduleHookContainer:nth-child(1) ~ *";
	}

}
