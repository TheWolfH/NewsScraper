package results;

import java.util.List;

import articles.Article;
import articles.ZeitArticle;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZeitResult extends ApiResult {
	@JsonProperty("matches")
	protected List<ZeitArticle> articles;
	@JsonProperty("found")
	protected int numArticles;

	public ZeitResult() {

	}

	@Override
	public List<? extends Article> getArticles() {
		return this.articles;
	}

	@Override
	public int getNumArticles() {
		return this.numArticles;
	}
}
