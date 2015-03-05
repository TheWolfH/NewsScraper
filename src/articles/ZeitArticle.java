package articles;
import java.io.IOException;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.fasterxml.jackson.annotation.*;

public class ZeitArticle extends Article {

	@JsonCreator
	public ZeitArticle(@JsonProperty("href") String url, @JsonProperty("title") String title,
			@JsonProperty("subtitle") String subtitle,
			@JsonProperty("release_date") Date publicationDate) {
		super(url, title);
		this.subtitle = subtitle;
		this.publicationDate = publicationDate;
	}

	@Override
	public void populateData() throws IOException {
		Document doc = Jsoup.connect(this.url).timeout(60000).get();
		Element articleBody = doc.select(".article-body").first();
		
		this.fullText = articleBody.text();
		this.fullTextHTML = articleBody.html();
	}

}
