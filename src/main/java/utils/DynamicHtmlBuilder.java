package utils;

import model.Article;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DynamicHtmlBuilder {
    private static final String Template_DIR = "src/main/resources/templates";
    private static final Logger log = LoggerFactory.getLogger(DynamicHtmlBuilder.class);

    public static byte[] buildIndexPage(User user, List<Article> articles) throws IOException {
        // 1. index.html 템플릿 읽기
        String template = readFile("index.html");

        // 2. 동적 헤더 생성
        String headerContent = buildHeader(user);
        String writeButtonContent = buildWriteButton(user);
        String articleListContent = buildArticleList(articles);

        // 3. 템플릿에 동적 컨텐츠 삽입
        String html = template
                .replace("<!-- HEADER_CONTENT -->", headerContent)
                .replace("<!-- WRITE_BUTTON -->", writeButtonContent)
                .replace("<!-- ARTICLE_LIST -->", articleListContent);

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

    private static String buildArticleList(List<Article> articles) {
        StringBuilder list = new StringBuilder();

        if (articles == null || articles.isEmpty()) {
            list.append("<p>등록된 게시글이 없습니다.</p>");
            return list.toString();
        }

        for (Article article : articles) {
            String authorName = article.getAuthor() != null ? article.getAuthor().getName() : "알 수 없음";

            list.append("<div class='post'>");

            // 계정 정보
            list.append("<div class='post__account'>")
                    .append("<img class='post__account__img' />") // 프로필 이미지 자리
                    .append("<p class='post__account__nickname'>")
                    .append(escapeHtml(authorName))
                    .append("</p>")
                    .append("</div>");

            // 게시글 이미지
            list.append("<img class='post__img' />");

            // 메뉴 (좋아요, 공유, 북마크)
            list.append("<div class='post__menu'>")
                    .append("<ul class='post__menu__personal'>")
                    .append("<li><button class='post__menu__btn'><img src='./img/like.svg' /></button></li>")
                    .append("<li><button class='post__menu__btn'><img src='./img/sendLink.svg' /></button></li>")
                    .append("</ul>")
                    .append("<button class='post__menu__btn'><img src='./img/bookMark.svg' /></button>")
                    .append("</div>");

            // 게시글 본문 (링크 포함)
            list.append("<a href='/article?id=").append(article.getId()).append("'>")
                    .append("<p class='post__article'>")
                    .append(escapeHtml(truncate(article.getContent(), 300)))
                    .append("</p>")
                    .append("</a>");

            list.append("</div>");
        }

        return list.toString();
    }

    // 본문 길이 제한
    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    private static String readFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(Template_DIR + File.separator + filename))) {
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

