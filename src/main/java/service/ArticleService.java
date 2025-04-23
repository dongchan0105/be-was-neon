package service;

import db.Database;
import dto.WriteArticleRequest;
import model.Article;

import java.util.List;

public class ArticleService {

    public static Article writeArticle(WriteArticleRequest request){
        Article article = new Article(request.title(), request.author(), request.content());
        Database.addArticle(article);
        return article;
    }

    public static Article findArticleById(int id){
       return  Database.findArticleById(id);
    }

    public static List<Article> findAllArticle(){
        return Database.findAllArticle();
    }
}
