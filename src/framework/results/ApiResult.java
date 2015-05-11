package framework.results;
import java.util.List;

import com.fasterxml.jackson.annotation.*;

import framework.articles.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ApiResult {
	/**
	 * @return the articles
	 */
	public abstract List<? extends Article> getArticles();

	/**
	 * @return the numArticles
	 */
	public abstract int getNumArticles();
}
