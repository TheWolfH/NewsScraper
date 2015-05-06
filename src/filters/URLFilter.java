package filters;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A PrePopulatingArticleFilter implementation used to filter out articles based
 * on the their URL. If the URL of the article matches one of the Regular
 * Expressions passed when constructing the filter, the filter returns
 * {@code false} so that the calling method will exclude this article.
 * 
 * @author Jan Helge Wolf
 *
 */
public class URLFilter implements PrePopulatingArticleFilter {
	/**
	 * the prefixes to be excluded
	 */
	protected final List<Pattern> badRegexes;

	/**
	 * Constructs a URLFilter with the given {@code badRegexes}.
	 * 
	 * @param badRegexes
	 *            the Regular Expressions that will lead to a URL being rejected
	 */
	public URLFilter(String... badRegexes) {
		this(Arrays.asList(badRegexes));
	}

	/**
	 * Constructs a URLFilter with Regular Expressions in {@code badRegexes}.
	 * 
	 * @param badRegexes
	 *            the Regular Expressions that will lead to a URL being rejected
	 */
	public URLFilter(List<String> badRegexes) {
		// As RegEx matching can be quite costly and these RegExes will be
		// executed multiple times, patterns are pre-compiled
		// (see http://openbook.rheinwerk-verlag.de/javainsel9/
		// javainsel_04_007.htm#mj10b152411c18f403a01181de6805ffab)
		//
		// In order to speed up the compile process, compile in parallel and
		// exclude multiple instances of the same pattern by calling distinct()
		// Make stream to unordered in order to speed up distinct() call
		this.badRegexes = badRegexes.stream().unordered().parallel()
				.distinct()
				.map(str -> Pattern.compile(str))
				.collect(Collectors.toList());
	}

	@Override
	public boolean test(String t) {
		// In order to further speed up the matching process, match in parallel
		// by creating a (parallel) stream on the patterns and check whether
		// none of the patterns match
		return this.badRegexes.stream().parallel().noneMatch(p -> p.matcher(t).matches());
	}

}
