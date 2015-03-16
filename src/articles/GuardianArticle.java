package articles;

import java.io.IOException;
import java.util.Date;

import org.jsoup.Jsoup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GuardianArticle extends Article {
	protected Fields fields;
	
	@JsonCreator
	public GuardianArticle(@JsonProperty("webUrl") String url, @JsonProperty("webTitle") String title,
			@JsonProperty("webPublicationDate") Date publicationDate, @JsonProperty("fields") Fields fields) {
		super(url, title);
		this.fields = fields;
		this.publicationDate = publicationDate;
	}

	@Override
	public void populateData() throws IOException {
		if (this.fields != null) {
			this.subtitle = this.fields.subtitle;
			this.fullTextHTML = this.fields.fullTextHTML;
			
			// Some Guardian "articles" do not possess any text (cartoons etc)
			if (this.fullTextHTML != null) {
				this.fullText = Jsoup.parse(this.fullTextHTML).text();
			}
			else {
				this.log.warning("No text provided for " + this.url);
			}
			
			this.fields = null;
		}
	}
	
	public static class Fields {
		@JsonProperty("trailText")
		protected String subtitle;
		@JsonProperty("body")
		protected String fullTextHTML;
	}

}
