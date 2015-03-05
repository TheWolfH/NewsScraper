package results;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import articles.Article;
import articles.GuardianArticle;

public class GuardianResult extends ApiResult {
	@JsonProperty("results")
	protected List<GuardianArticle> articles;
	@JsonProperty("total")
	protected int numArticles;
	
	public GuardianResult() {

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
