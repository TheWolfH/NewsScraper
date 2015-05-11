package application.results;

import java.util.List;

import application.articles.GuardianArticle;

import com.fasterxml.jackson.annotation.JsonProperty;

import framework.articles.Article;
import framework.results.ApiResult;

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
