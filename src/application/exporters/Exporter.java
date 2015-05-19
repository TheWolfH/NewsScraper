package application.exporters;

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

public class Exporter implements framework.helpers.Exporter {
	protected Connection con;
	protected String filename;
	protected Map<DataSource, Map<String, Article>> result;
	protected final Logger log = LoggerGenerator.getLogger();

	public Exporter(Map<DataSource, Map<String, Article>> result, String databaseFileName) throws SQLException {
		this.result = result;
		this.filename = databaseFileName;

		// Connect to database
		this.con = DriverManager.getConnection("jdbc:sqlite:" + this.filename);
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

	public void exportArticles() {
		try {
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
						throw new SQLException();
					}

					// Retrieve id of inserted article for article keywords
					ResultSet generatedKeys = insertArticle.getGeneratedKeys();

					if (generatedKeys.next()) {
						articleId = generatedKeys.getLong(1);
					}
					else {
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
		catch (SQLException e) {
			this.log.severe("SQLException when trying to export articles, stack trace follows:");

			for (StackTraceElement element : e.getStackTrace()) {
				this.log.severe(element.toString());
			}

			this.log.severe("End of stack trace");
		}
	}

	public static void main(String[] args) {
		if (args.length < 4) {
			throw new IllegalArgumentException(
					"At least three arguments (fromDate, toDate, keywords...) expected");
		}

		// Parse date strings into Date objects
		//Date start = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Date fromDate = null;
		Date toDate = null;
		try {
			fromDate = format.parse(args[1] + " 00:00:00");// format.parse("2013-01-01");
			toDate = format.parse(args[2] + " 23:59:59");// format.parse("2014-12-31");
		}
		catch (ParseException e) {
			e.printStackTrace();
		}

		// Get all keywords
		String[] keywords = new String[args.length - 3];

		for (int i = 3; i < args.length; i++) {
			keywords[i - 3] = args[i];
		}

		// Get data sources to scrape from config
		String usedDataSources = ConfigReader.getConfig().getProperty(
				"General.DataSource.usedDataSources");
		Map<DataSource, Map<String, Article>> articles;

		// Get articles from Wrapper
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

		// Export articles
		try {
			Exporter export = new Exporter(articles, args[0]);
			export.exportArticles();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		//Date end = new Date();
		//System.out.println(end.getTime() - start.getTime());
	}
}
