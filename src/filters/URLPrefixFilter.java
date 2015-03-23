package filters;

/**
 * A PrePopulatingArticleFilter implementation used to filter out articles based
 * on the prefix of their URL. If the URL of the article starts with one of the
 * strings passed when constructing the filter (as determined by
 * {@link String#startsWith(String)}), the filter returns {@code false} so that
 * the calling method will exclude this article.
 * 
 * @author Jan Helge Wolf
 *
 */
public class URLPrefixFilter implements PrePopulatingArticleFilter {
	/**
	 * the prefixes to be excluded
	 */
	protected String[] badPrefixes;

	/**
	 * Constructs a URLPrefixFilter with the given {@code badPrefixes}.
	 * 
	 * @param badPrefixes
	 *            the prefixes that will lead to a URL being rejected
	 */
	public URLPrefixFilter(String... badPrefixes) {
		this.badPrefixes = badPrefixes;
	}

	@Override
	public boolean test(String t) {
		for (String badPrefix : this.badPrefixes) {
			if (t.endsWith(badPrefix)) {
				return false;
			}
		}

		return true;
	}

}
