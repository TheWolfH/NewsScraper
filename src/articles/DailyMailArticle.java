package articles;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class DailyMailArticle extends ScrapedArticle {

	public DailyMailArticle(String url, String title) {
		super(url, title);
	}

	public DailyMailArticle(String url, String title, String keyword) {
		super(url, title, keyword);
	}

	@Override
	protected String getSubtitleSelector() {
		return "div.article-text > h1 + ul.mol-bullets-with-font";
	}

	@Override
	protected String getPublicationDateSelector() {
		return "div.article-text span.article-timestamp";
	}

	@Override
	protected String getPublicationDateFormat() {
		return "HH:mm zzz, d MMM yyyy";
	}

	@Override
	protected Locale getLocale() {
		return Locale.UK;
	}

	@Override
	protected String getFullTextSelector() {
		// Get everything below div.article-text, but exclude author and
		// timestamp
		// details, "most read/watched" stuff, social media buttons and comments
		return "div.article-text h1 ~ p:not(.author-section):not([class*=\"byline\"]),"
				+ "div.article-text h1 ~ div:not(.column-content.cleared)"
				+ ":not(#most-read-news-wrapper):not(#most-watched-videos-wrapper):not(.article-reader-comments)"
				+ ":not(#articleIconLinksContainer):not(#taboola-below-main-column)";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * articles.ScrapedArticle#getPublicationDateFromDocument(org.jsoup.nodes
	 * .Document)
	 * 
	 * Basically a copy of ScrapedArticle.getPublicationDateFromDocument(), but
	 * regards some Daily Mail specialties: First, there may be more than one
	 * article timestamp (published OR created) AND/OR updated, therefore only
	 * the first timestamp can be used. Second, timestamps are wrapped into a
	 * <span class="article-timestamp"><span>LABEL:</span> TIMESTAMP</span>
	 * object. Therefore, instead of .text(), .ownText() must be used in order
	 * to only fetch the actual Date without the label around it.
	 */
	@Override
	protected Date getPublicationDateFromDocument(Document doc) throws ParseException {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(this.getPublicationDateFormat(),
				this.getLocale());

		try {
			// Normal case
			Element element = doc.select(this.getPublicationDateSelector()).first();

			if (element != null) {
				return dateFormatter.parse(element.ownText());
			}

			// (Costly) fallback: Search for object that contains
			// "Last updated at"
			// Caution: different time pattern!
			element = doc.select("div.article-text h1 ~ p:contains(Last updated at").first();

			if (element != null) {
				dateFormatter = new SimpleDateFormat("HH:mm d MMM yyyy", this.getLocale());
				return dateFormatter.parse(element.ownText().replaceAll("Last updated at ", ""));
			}
		}
		catch (ParseException e) {
			// In case of parsing failure: log warning
			this.log.warning("Unable to parse publication date for article with url " + this.url);
		}

		// No timestamp found or parsing exception: return null
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see articles.ScrapedArticle#beforePopulatingDataHook()
	 * 
	 * Artificially slows down the (massively concurrent) execution of
	 * populateData() to avoid requests from being denied by the server as a
	 * method of preventing DOS attacks. In order to distribute the requests
	 * more evenly, the execution is delayed by a random number between 0 and
	 * 5000 millisesonds.
	 */
	@Override
	protected void beforePopulatingDataHook() {
		try {
			Thread.sleep(new Random().nextInt(4000));
		}
		catch (InterruptedException e) {
			// No need to do anything here
		}
	}
}
