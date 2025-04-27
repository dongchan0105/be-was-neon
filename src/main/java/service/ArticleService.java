package service;

import dto.WriteArticleRequest;
import model.Article;
import repository.ArticleRepository;

import java.util.List;

public class ArticleService {

    private static final ArticleRepository repository = new ArticleRepository();

    public static Article writeArticle(WriteArticleRequest request) {
        Article article;
        if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
            article = new Article(
                    request.title(),
                    request.author(),
                    request.content(),
                    request.imageUrl()
            );
        } else {
            article = new Article(
                    request.title(),
                    request.author(),
                    request.content()
            );
        }
        repository.save(article);
        return article;
    }


    public static Article findArticleById(int id){
        return repository.findById(id);
    }

    public static List<Article> findAllArticle(){
        return repository.findAll();
    }
}
