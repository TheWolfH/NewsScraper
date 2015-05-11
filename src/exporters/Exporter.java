package exporters;

import framework.articles.Article;
import framework.helpers.ConfigReader;
import framework.helpers.LoggerGenerator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import application.helpers.DataSource;
import application.wrappers.Wrapper;

public class Exporter {
	protected Connection con;
	protected Map<DataSource, Map<String, Article>> result;
	protected final Logger log = LoggerGenerator.getLogger();

	public Exporter(Map<DataSource, Map<String, Article>> result) throws SQLException {
		this.result = result;

		// Connect to database
		this.con = DriverManager.getConnection("jdbc:sqlite:database.db");
		this.con.setAutoCommit(false);
		this.con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

		this.setupDatabase();
	}

	protected void setupDatabase() throws SQLException {
		Statement setup = this.con.createStatement();

		// article table
		setup.addBatch("CREATE TABLE IF NOT EXISTS article (id INTEGER PRIMARY KEY, "
				+ "url TEXT UNIQUE NOT NULL, title TEXT NOT NULL, subtitle TEXT, "
				+ "publicationDate DATETIME, fullText TEXT, fullTextHTML TEXT, source TEXT);");
		setup.addBatch("CREATE INDEX IF NOT EXISTS article_idx_source ON article (source);");
		setup.addBatch("CREATE INDEX IF NOT EXISTS article_idx_publicationDate ON article (publicationDate);");
		
		// article_keywords table
		setup.addBatch("CREATE TABLE IF NOT EXISTS article_keywords (id INTEGER PRIMARY KEY, "
				+ "keyword TEXT, article_id INTEGER REFERENCES article (id) "
				+ "DEFERRABLE INITIALLY DEFERRED)");
		setup.addBatch("CREATE INDEX IF NOT EXISTS article_keywords_idx_keyword ON article_keywords (keyword);");
		setup.addBatch("CREATE INDEX IF NOT EXISTS article_keywords_idx_article_id ON article_keywords (article_id);");
		
		// Empty tables
		setup.addBatch("DELETE FROM article;");
		setup.addBatch("DELETE FROM article_keywords;");

		setup.executeBatch();
		this.con.commit();

		// Cannot be set within transaction
		this.con.setAutoCommit(true);
		setup.execute("VACUUM;");
		setup.execute("PRAGMA foreign_keys = ON;");
		this.con.setAutoCommit(false);
	}

	public void readArticles() throws SQLException {
		// Prepare statement for INSERTing article rows
		PreparedStatement insertArticle = this.con.prepareStatement("INSERT INTO article "
				+ "(url, title, subtitle, publicationDate, fullText, fullTextHTML, source) "
				+ "VALUES (?, ?, ?, datetime(?), ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

		// Prepare statement for INSERTing article_keywords rows
		PreparedStatement insertArticleKeyword = this.con
				.prepareStatement("INSERT INTO article_keywords " + "(keyword, article_id) "
						+ "VALUES (?, ?)");

		// Initialize date formatter
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

		// Helper variables
		int affectedRows = 0;
		long articleId = 0;

		// Iterate over data sources, get articles for each data source
		for (DataSource source : this.result.keySet()) {
			Map<String, Article> articles = this.result.get(source);

			// Iterate over articles, insert into database
			for (Article article : articles.values()) {
				// TODO decide how to handle "incomplete" article objects
				Date publicationDate = article.getPublicationDate();

				insertArticle.setString(1, article.getUrl());
				insertArticle.setString(2, article.getTitle());
				insertArticle.setString(3, article.getSubtitle());
				insertArticle.setString(4,
						(publicationDate == null ? null : formatter.format(publicationDate)));
				insertArticle.setString(5, article.getFullText());
				insertArticle.setString(6, article.getFullTextHTML());
				insertArticle.setString(7, source.getName());

				// Check number of affected rows (must be 1)
				affectedRows = insertArticle.executeUpdate();

				if (affectedRows != 1) {
					// TODO Custom exception
					throw new SQLException();
				}

				// Retrieve id of inserted article for article keywords
				ResultSet generatedKeys = insertArticle.getGeneratedKeys();

				if (generatedKeys.next()) {
					articleId = generatedKeys.getLong(1);
				}
				else {
					// TODO Custom exception
					throw new SQLException();
				}

				// Iterate over article keywords and insert into database
				if (article.getKeywords() != null) {
					for (String keyword : article.getKeywords()) {
						insertArticleKeyword.setString(1, keyword);
						insertArticleKeyword.setLong(2, articleId);

						insertArticleKeyword.executeUpdate();
					}
				}

				this.con.commit();
			}
		}
	}

	public static void main(String[] args) {		
		if (args.length < 3) {
			throw new IllegalArgumentException(
					"At least three arguments (fromDate, toDate, keywords...) expected");
		}

		Date start = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Date fromDate = null;
		Date toDate = null;
		try {
			fromDate = format.parse(args[0] + " 00:00:00");// format.parse("2013-01-01");
			toDate = format.parse(args[1] + " 23:59:59");// format.parse("2014-12-31");
		}
		catch (ParseException e) {
			e.printStackTrace();
		}

		String[] keywords = new String[args.length - 2];

		for (int i = 2; i < args.length; i++) {
			keywords[i - 2] = args[i];
		}

		String usedDataSources = ConfigReader.getConfig().getProperty(
				"General.DataSource.usedDataSources");
		Map<DataSource, Map<String, Article>> articles;

		if (usedDataSources.equals("ALL")) {
			articles = Wrapper.searchArticles(keywords, fromDate, toDate);
		}
		else {
			List<DataSource> sources = new ArrayList<DataSource>();

			for (String usedDataSource : usedDataSources.split(",\\s*")) {
				DataSource source = DataSource.valueOf(usedDataSource);
				sources.add(source);
			}

			articles = Wrapper.searchArticles(keywords, fromDate, toDate, sources);
		}

		try {
			Exporter export = new Exporter(articles);
			export.readArticles();
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Date end = new Date();
		System.out.println(end.getTime() - start.getTime());
	}
}
