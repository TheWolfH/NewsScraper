package filters;

import java.util.function.Predicate;

/**
 * An interface for filters meant to be applied by {@link Fetcher} objects after
 * collecting articles, but before populating them. As at this point only the
 * URL and the title of the article are guaranteed to be known and the title
 * normally does provide any useful information, the filtering is done solely on
 * basis of the URL.
 * 
 * @author Jan Helge Wolf
 *
 */
public interface PrePopulatingArticleFilter extends Predicate<String> {

}
