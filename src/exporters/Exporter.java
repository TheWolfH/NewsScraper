package exporters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Map;

import articles.Article;

public class Exporter {
	public Exporter(Map<String, Article> articles) {
		try {
			Connection con = DriverManager.getConnection("jdbc:sqlite:database.db");
			con.setAutoCommit(false);
			con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

			this.setupDatabase(con);
			this.readArticles(con, articles);
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void setupDatabase(Connection con) throws SQLException {
		Statement createTables = con.createStatement();

		createTables.addBatch("PRAGMA foreign_keys = ON;");
		createTables.addBatch("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, "
				+ "url TEXT, title TEXT, subtitle TEXT, publicationDate DATETIME, "
				+ "fullText TEXT, fullTextHTML TEXT);");
		createTables
				.addBatch("CREATE TABLE IF NOT EXISTS article_keywords (id INTEGER PRIMARY KEY, "
						+ "keyword TEXT, article_id INTEGER REFERENCES articles (id) "
						+ "DEFERRABLE INITIALLY DEFERRED)");
		createTables.addBatch("DELETE FROM articles;");
		createTables.addBatch("DELETE FROM article_keywords;");

		createTables.executeBatch();

	}

	public void readArticles(Connection con, Map<String, Article> articles) throws SQLException {
		PreparedStatement insertArticle = con.prepareStatement("INSERT INTO articles "
				+ "(url, title, subtitle, publicationDate, fullText, fullTextHTML) "
				+ "VALUES (?, ?, ?, datetime(?), ?, ?)", Statement.RETURN_GENERATED_KEYS);
		PreparedStatement insertArticleKeyword = con
				.prepareStatement("INSERT INTO article_keywords " + "(keyword, article_id) "
						+ "VALUES (?, ?)");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

		int affectedRows = 0;
		long articleId = 0;

		for (Article article : articles.values()) {
			insertArticle.setString(1, article.getUrl());
			insertArticle.setString(2, article.getTitle());
			insertArticle.setString(3, article.getSubtitle());
			insertArticle.setString(4, formatter.format(article.getPublicationDate()));
			insertArticle.setString(5, article.getFullText());
			insertArticle.setString(6, article.getFullTextHTML());

			affectedRows = insertArticle.executeUpdate();

			if (affectedRows != 1) {
				// TODO Custom exception
				throw new SQLException();
			}

			ResultSet generatedKeys = insertArticle.getGeneratedKeys();

			if (generatedKeys.next()) {
				articleId = generatedKeys.getLong(1);
			}
			else {
				// TODO Custom exception
				throw new SQLException();
			}

			if (article.getKeywords() != null) {
				for (String keyword : article.getKeywords()) {
					insertArticleKeyword.setString(1, keyword);
					insertArticleKeyword.setLong(2, articleId);

					insertArticleKeyword.executeUpdate();
				}
			}

			con.commit();
		}
	}

	public static void main(String[] args) {
		Exporter e = new Exporter(null);
	}
}
