package handler;

import db.Database;
import dto.WriteArticleRequest;
import model.Article;
import model.User;
import service.ArticleService;
import session.SessionManager;

import java.util.Map;

import static session.SessionManager.SESSION_COOKIE_NAME;

public class WriteHandler implements ReturnViewPathHandler{


    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        WriteArticleRequest writeArticleRequest = parseQueryString(paramMap);
        Article article = ArticleService.writeArticle(writeArticleRequest);
        Database.addArticle(article);
        model.put("article", article);
        return "redirect:/index.html";
    }

    private WriteArticleRequest parseQueryString(Map<String, String> paramMap) {

        String sessionId = paramMap.getOrDefault(SESSION_COOKIE_NAME,"");
        User user = SessionManager.getUser(sessionId);

        return new WriteArticleRequest(
                paramMap.getOrDefault("title",""),
                user,
                paramMap.getOrDefault("content","")
        );
    }
}
