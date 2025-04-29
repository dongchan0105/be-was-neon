package utils.parser;

import dto.HttpResponse;
import frontHandler.ModelView;
import model.Article;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.ArticleRepository;
import utils.DynamicHtmlBuilder;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static session.SessionManager.SESSION_COOKIE_NAME;

public class HttpResponseParser {

    private static final Logger log = LoggerFactory.getLogger(HttpResponseParser.class);
    private static final ArticleRepository articleRepository = new ArticleRepository();

    public static HttpResponse makeHttpResponse(ModelView mv) throws IOException {
        String viewPath = mv.getViewName();
        Map<String, Object> model = mv.getModel();
        HttpResponse response = new HttpResponse();

        // 리다이렉트 처리
        if (viewPath.startsWith("redirect:")) {
            String redirectPath = viewPath.substring("redirect:".length());
            response.setStatusCode(302);
            response.setStatusText("Found");
            response.getHeaders().put("Location", redirectPath);
            response.setBody(new byte[0]);
            response.setContentType("text/plain");

            if (model.containsKey(SESSION_COOKIE_NAME)) {
                response.getCookies().put(SESSION_COOKIE_NAME, model.get(SESSION_COOKIE_NAME).toString());
            }
            return response;
        }

        // 동적 페이지 처리
        if ("dynamic/index".equals(viewPath)) {
            log.debug("Generating dynamic index page");

            User user = (User) model.get("user");
            List<Article> articles = articleRepository.findAll();

            byte[] htmlBody = DynamicHtmlBuilder.buildIndexPage(user, articles);

            response.setStatusCode(200);
            response.setStatusText("OK");
            response.setContentType("text/html;charset=utf-8");
            response.setBody(htmlBody);

            if (model.containsKey(SESSION_COOKIE_NAME)) {
                response.getCookies().put(SESSION_COOKIE_NAME, model.get(SESSION_COOKIE_NAME).toString());
            }

            return response;
        }

        // 그 외 경로는 404 처리
        response.setStatusCode(404);
        response.setStatusText("Not Found");
        response.setContentType("text/html;charset=utf-8");
        response.setBody("<h1>404 Not Found</h1>".getBytes(StandardCharsets.UTF_8));
        return response;
    }
}
