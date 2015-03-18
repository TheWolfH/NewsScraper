package exporters;

import gui.Wrapper;
import helpers.DataSource;
import helpers.LoggerGenerator;

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

import articles.Article;

public class Exporter {
	protected Connection con;
	protected Map<DataSource, Map<String, Article>> result;
	protected final Logger log = LoggerGenerator.getLoggerGenerator().getLogger();

	public Exporter(Map<DataSource, Map<String, Article>> result) throws SQLException {
		this.result = result;

		// Connect to database
		this.con = DriverManager.getConnection("jdbc:sqlite:database.db");
		this.con.setAutoCommit(false);
		this.con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

		this.setupDatabase();
	}

	protected void setupDatabase() throws SQLException {
		Statement createTables = this.con.createStatement();

		createTables.addBatch("PRAGMA foreign_keys = ON;");
		createTables.addBatch("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, "
				+ "url TEXT UNIQUE NOT NULL, title TEXT NOT NULL, subtitle TEXT, "
				+ "publicationDate DATETIME, fullText TEXT, fullTextHTML TEXT, source TEXT);");
		createTables
				.addBatch("CREATE TABLE IF NOT EXISTS article_keywords (id INTEGER PRIMARY KEY, "
						+ "keyword TEXT, article_id INTEGER REFERENCES articles (id) "
						+ "DEFERRABLE INITIALLY DEFERRED)");
		createTables.addBatch("DELETE FROM articles;");
		createTables.addBatch("DELETE FROM article_keywords;");

		createTables.executeBatch();
		this.con.commit();

		this.con.setAutoCommit(true);
		createTables.execute("VACUUM;");
		this.con.setAutoCommit(false);
	}

	public void readArticles() throws SQLException {
		// Prepare statement for INSERTing article rows
		PreparedStatement insertArticle = this.con.prepareStatement("INSERT INTO articles "
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
				insertArticle.setString(1, article.getUrl());
				insertArticle.setString(2, article.getTitle());
				insertArticle.setString(3, article.getSubtitle());
				insertArticle.setString(4, formatter.format(article.getPublicationDate()));
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
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		Date fromDate = null;
		Date toDate = null;
		try {
			fromDate = format.parse(args[0]);// format.parse("2013-01-01");
			toDate = format.parse(args[1]);// format.parse("2014-12-31");
		}
		catch (ParseException e) {
			e.printStackTrace();
		}

		String[] keywords = new String[args.length - 2];

		for (int i = 2; i < args.length; i++) {
			keywords[i - 2] = args[i];
		}

		List<DataSource> sources = new ArrayList<DataSource>();
		sources.add(DataSource.SPIEGELONLINE);

		Map<DataSource, Map<String, Article>> articles = Wrapper.searchArticles(keywords, fromDate,
				toDate , sources );

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
