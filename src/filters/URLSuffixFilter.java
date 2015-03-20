package filters;

/**
 * A PrePopulatingArticleFilter implementation used to filter out articles based
 * on the suffix of their URL. If the URL of the article ends with one of the
 * strings passed when constructing the filter (as determined by
 * {@link String#endsWith(String)}), the filter returns {@code false} so that
 * the calling method will exclude this article.
 * 
 * @author Jan Helge Wolf
 *
 */
public class URLSuffixFilter implements PrePopulatingArticleFilter {
	/**
	 * the suffixes to be excluded
	 */
	String[] badSuffixes;

	/**
	 * Constructs a URLSuffixFilter with the given {@code badSuffixes}.
	 * @param badSuffixes
	 */
	public URLSuffixFilter(String... badSuffixes) {
		this.badSuffixes = badSuffixes;
	}

	@Override
	public boolean test(String t) {
		for (String badSuffix : this.badSuffixes) {
			if (t.endsWith(badSuffix)) {
				return false;
			}
		}

		return true;
	}
}
