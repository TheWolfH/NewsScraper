package results;
import java.util.List;
import articles.*;

import com.fasterxml.jackson.annotation.*;

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
