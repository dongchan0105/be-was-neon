package db;

import model.Article;
import model.User;

import java.util.*;

public class Database {

    private static final Map<String, User> users = new HashMap<>();

    private static final Map<Integer, Article> articles = new HashMap<>();

    public static void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public static User findUserById(String userId) {
        return users.get(userId);
    }

    public static Collection<User> findAllUser() {
        return users.values();
    }

    public static void addArticle(Article article) {
        articles.put(article.getId(), article);
    }

    public static Article findArticleById(Integer articleId) {
        return articles.get(articleId);
    }

    public static List<Article> findAllArticle() {
        return new ArrayList<>(articles.values());
    }
}
