package framework.filters;

import java.util.Map;
import java.util.function.Predicate;

import framework.articles.Article;

/**
 * An interface for framework.filters meant to be applied by {@link framework.fetchers.Fetcher} objects after
 * populating the articles, before returning them. As at this point all
 * properties of the article are supposed (but not guaranteed) to be known, so
 * that the filtering can be based on any of the getters provided by the
 * {@link Article} class. As {@code null} articles do not provide any value, all
 * {@code PostPopulatingArticleFilter}s will reject any such entries.
 * 
 * @author Jan Helge Wolf
 *
 */
public interface PostPopulatingArticleFilter extends Predicate<Map.Entry<String, Article>> {

}
