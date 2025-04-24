package utils.parser;

import db.Database;
import dto.HttpResponse;
import frontHandler.ModelView;
import handler.StaticRequestHandler;
import model.Article;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.DynamicHtmlBuilder;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static session.SessionManager.SESSION_COOKIE_NAME;


public class HttpResponseParser {

    private static final String STATIC_DIRECTORY = "src/main/resources/static";
    private static final Logger log = LoggerFactory.getLogger(HttpResponseParser.class);

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

            // 모델과 article 정보확인
            User user = (User) model.get("user");
            List<Article> articles = Database.findAllArticle();


            byte[] htmlBody = DynamicHtmlBuilder.buildIndexPage(user,articles);
            response.setStatusCode(200);
            response.setStatusText("OK");
            response.setContentType("text/html;charset=utf-8");
            response.setBody(htmlBody);

            if (model.containsKey(SESSION_COOKIE_NAME)) {
                response.getCookies().put(SESSION_COOKIE_NAME, model.get(SESSION_COOKIE_NAME).toString());
            }

            return response;
        }

        // 논리 뷰 이름 처리: 확장자 없으면 .html 붙이기
        if (!viewPath.contains(".")) {
            viewPath += ".html";
        }

        // 경로 보정
        viewPath = URLDecoder.decode(viewPath, StandardCharsets.UTF_8.name());
        File file = new File(STATIC_DIRECTORY + File.separator + viewPath.replace("/", File.separator));
        log.debug("File path: {}", file.getAbsolutePath());

        // 파일이 없거나 디렉터리면 404 처리
        if (!file.exists() || file.isDirectory()) {
            response.setStatusCode(404);
            response.setStatusText("Not Found");
            response.setContentType("text/html;charset=utf-8");
            response.setBody("<h1>404 Not Found</h1>".getBytes(StandardCharsets.UTF_8));
            return response;
        }

        // 파일 읽기 (java.io 방식)
        StringBuilder htmlBuilder = new StringBuilder();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                htmlBuilder.append(line).append("\n");
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        // 모델 데이터로 ${key} 치환
        String html = htmlBuilder.toString();
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            String key = "${" + entry.getKey() + "}";
            html = html.replace(key, entry.getValue().toString());
        }

        response.setStatusCode(200);
        response.setStatusText("OK");
        response.setContentType(StaticRequestHandler.determineContentType(file));
        response.setBody(html.getBytes(StandardCharsets.UTF_8));

        if (model.containsKey(SESSION_COOKIE_NAME)) {
            response.getCookies().put(SESSION_COOKIE_NAME, model.get(SESSION_COOKIE_NAME).toString());
        }

        return response;
    }
}

