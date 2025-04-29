package utils.parser;

import dto.HttpResponse;
import frontHandler.ModelView;
import handler.StaticRequestHandler;
import model.Article;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.ArticleRepository;
import utils.DynamicHtmlBuilder;

import java.io.*;
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

        // 로그인 상태에 따른 동적 HTML 생성
        if ("dynamic/index".equals(viewPath)) {
            log.debug("viewPath = {}", viewPath);

            User user = (User) model.get("user");
            List<Article> articles = articleRepository.findAll();

            log.debug("user = {}", user);

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

        // 경로 보정
        viewPath = URLDecoder.decode(viewPath, StandardCharsets.UTF_8);

        // JAR 안에서도 동작하는 방식으로 수정
        InputStream inputStream = HttpResponseParser.class.getClassLoader().getResourceAsStream("static/" + viewPath);

        if (inputStream == null) {
            log.warn("Requested file not found: {}", viewPath);
            response.setStatusCode(404);
            response.setStatusText("Not Found");
            response.setContentType("text/html;charset=utf-8");
            response.setBody("<h1>404 Not Found</h1>".getBytes(StandardCharsets.UTF_8));
            return response;
        }

        // 파일 읽기
        StringBuilder htmlBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                htmlBuilder.append(line).append("\n");
            }
        }

        response.setStatusCode(200);
        response.setStatusText("OK");
        response.setContentType(StaticRequestHandler.determineContentType(viewPath));
        response.setBody(htmlBuilder.toString().getBytes(StandardCharsets.UTF_8));

        if (model.containsKey(SESSION_COOKIE_NAME)) {
            response.getCookies().put(SESSION_COOKIE_NAME, model.get(SESSION_COOKIE_NAME).toString());
        }

        return response;
    }
}
