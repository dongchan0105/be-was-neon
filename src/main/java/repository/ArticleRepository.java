package repository;

import model.Article;
import model.User;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ArticleRepository {

    public void save(Article article) {
        String sql = "INSERT INTO articles (title, author_id, content, image_url) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, article.getTitle());
            pstmt.setString(2, article.getAuthor().getUserId());
            pstmt.setString(3, article.getContent());
            pstmt.setString(4, article.getImageUrl());
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Article> findAll() {
        String sql = "SELECT a.id, a.title, a.content, a.image_url, u.user_id, u.name, u.password, u.email " +
                "FROM articles a JOIN users u ON a.author_id = u.user_id";

        List<Article> articles = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                User author = new User(
                        rs.getString("user_id"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("email")
                );
                Article article = new Article(
                        rs.getString("title"),
                        author,
                        rs.getString("content"),
                        rs.getString("image_url")
                );
                articles.add(article);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return articles;
    }
}
