package utils;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DynamicHtmlBuilder {
    private static final String STATIC_DIR = "src/main/resources/static";
    private static final Logger log = LoggerFactory.getLogger(DynamicHtmlBuilder.class);

    public static byte[] buildIndexPage(User user) throws IOException {
        // 1. index.html 템플릿 읽기
        String template = readFile("index.html");

        // 2. 동적 헤더 생성
        String headerContent = buildHeader(user);
        String writeButtonContent = buildWriteButton(user);

        // 3. 템플릿에 동적 컨텐츠 삽입
        String html = template
                .replace("<!-- HEADER_CONTENT -->", headerContent)
                .replace("<!-- WRITE_BUTTON -->", writeButtonContent);

        return html.getBytes(StandardCharsets.UTF_8);
    }

    private static String buildHeader(User user) {
        StringBuilder header = new StringBuilder();
        log.debug("user = {}", user);
        if (user != null) {
            // 로그인 상태: 사용자 이름 + 로그아웃 버튼
            header.append("<div class='user-info'>");
            header.append("<span>").append(escapeHtml(user.getName())).append(" 님 환영합니다!    </span>");
            header.append("<a href='/logout' class='btn btn_contained btn_size_s'>로그아웃</a>");
            header.append("</div>");
        } else {
            // 비로그인 상태: 로그인/회원가입 버튼
            header.append("<ul class='header__menu'>");
            header.append("<li class='header__menu__item'>");
            header.append("<a class='btn btn_contained btn_size_s' href='/login'>로그인</a>");
            header.append("</li>");
            header.append("<li class='header__menu__item'>");
            header.append("<a class='btn btn_ghost btn_size_s' href='/registration'>회원 가입</a>");
            header.append("</li>");
            header.append("</ul>");
        }
        return header.toString();
    }

    private static String buildWriteButton(User user) {
        StringBuilder button = new StringBuilder();
        button.append("<div class='write-btn-area' style='margin-top: 24px;'>");

        if (user != null) {
            // 로그인 사용자: 글쓰기 가능
            button.append("<a class='btn btn_contained btn_size_m' href='/write'>글쓰기</a>");
        } else {
            // 비로그인 사용자: 로그인 페이지로 이동
            button.append("<a class='btn btn_contained btn_size_m' href='/login'>글쓰기</a>");
        }

        button.append("</div>");
        return button.toString();
    }

    private static String readFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(STATIC_DIR + File.separator + filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

