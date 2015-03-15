package gui;

import helpers.DataSource;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import articles.Article;

public class Wrapper {

	public Wrapper() {

	}

	public static Map<DataSource, Map<String, Article>> searchArticles(String[] keywords, Date fromDate,
			Date toDate, List<DataSource> desiredSources) {
		Map<DataSource, Map<String, Article>> result = new HashMap<DataSource, Map<String, Article>>();

		for (DataSource source : DataSource.values()) {
			if (desiredSources.contains(source)) {
				result.put(source, source.getFetcher().searchArticles(keywords, fromDate, toDate));
			}
		}
		
		return result;
	}

	public static Map<DataSource, Map<String, Article>> searchArticles(String[] keywords, Date fromDate,
			Date toDate) {
		return searchArticles(keywords, fromDate, toDate, Arrays.asList(DataSource.values()));
	}

}
