package articles;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jsoup.nodes.Document;

public class DailyExpressArticle extends ScrapedArticle {

	public DailyExpressArticle(String url, String title) {
		super(url, title);
	}

	public DailyExpressArticle(String url, String title, String keyword) {
		super(url, title, keyword);
	}

	@Override
	protected String getSubtitleSelector() {
		return "article header h3";
	}

	// Override in order to leverage the fact that the Daily Express makes use
	// of the <time> element and its datetime attribute, in which the
	// publication is stored in ISO format.
	@Override
	protected Date getPublicationDateFromDocument(Document doc) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(this.getPublicationDateFormat(),
				this.getLocale());
		String publicationDateString = doc.select(this.getPublicationDateSelector()).attr("datetime");
		
		return formatter.parse(publicationDateString);
	}

	@Override
	protected String getPublicationDateSelector() {
		return "article header div.dates time";
	}

	@Override
	protected String getPublicationDateFormat() {
		return "yyyy-MM-dd'T'HH:mm:ssX";
	}

	@Override
	protected Locale getLocale() {
		return Locale.UK;
	}

	@Override
	protected String getFullTextSelector() {
		return "article div.ctx_content section:not(.related-articles)";
	}

}
