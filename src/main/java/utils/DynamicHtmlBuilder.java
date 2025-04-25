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
    private static final String TEMPLATE_DIR = "src/main/resources/templates";
    private static final Logger log = LoggerFactory.getLogger(DynamicHtmlBuilder.class);

    /**
     * 인덱스 페이지를 동적으로 생성
     */
    public static byte[] buildIndexPage(User user, List<Article> articles) throws IOException {
        // 1. 템플릿 읽기
        String template = readFile("index.html");

        // 2. 동적 콘텐츠 생성
        String headerContent = buildHeader(user);
        String writeButtonContent = buildWriteButton(user);
        String articleListContent = buildArticleList(articles);

        // 3. 템플릿에 삽입
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
            header.append("<div class='user-info'>")
                    .append("<span>")
                    .append(escapeHtml(user.getName()))
                    .append(" 님 환영합니다!</span>")
                    .append("<a href='/logout' class='btn btn_contained btn_size_s'>로그아웃</a>")
                    .append("</div>");
        } else {
            header.append("<ul class='header__menu'>")
                    .append("<li class='header__menu__item'><a class='btn btn_contained btn_size_s' href='/login'>로그인</a></li>")
                    .append("<li class='header__menu__item'><a class='btn btn_ghost btn_size_s' href='/registration'>회원 가입</a></li>")
                    .append("</ul>");
        }
        return header.toString();
    }

    private static String buildWriteButton(User user) {
        StringBuilder btn = new StringBuilder();
        btn.append("<div class='write-btn-area' style='margin-top:24px;'>");
        if (user != null) {
            btn.append("<a class='btn btn_contained btn_size_m' href='/write'>글쓰기</a>");
        } else {
            btn.append("<a class='btn btn_contained btn_size_m' href='/login'>글쓰기</a>");
        }
        btn.append("</div>");
        return btn.toString();
    }

    private static String buildArticleList(List<Article> articles) {
        StringBuilder list = new StringBuilder();
        if (articles == null || articles.isEmpty()) {
            list.append("<p>등록된 게시글이 없습니다.</p>");
            return list.toString();
        }

        for (Article article : articles) {
            String authorName = article.getAuthor() != null
                    ? article.getAuthor().getName()
                    : "알 수 없음";

            list.append("<div class='post'>");

            // 계정 정보
            list.append("<div class='post__account'>")
                    .append("<img class='post__account__img' />")
                    .append("<p class='post__account__nickname'>")
                    .append(escapeHtml(authorName))
                    .append("</p>")
                    .append("</div>");

            // 게시글 이미지 (있을 때만)
            if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                list.append("<div class='post__image-container'>")
                        .append("<img class='post__img' src='")
                        .append(escapeHtml(article.getImageUrl()))
                        .append("' alt='article image'/>")
                        .append("</div>");
            }

            // 메뉴 아이콘
            list.append("<div class='post__menu'>")
                    .append("<ul class='post__menu__personal'>")
                    .append("<li><button class='post__menu__btn'><img src='./img/like.svg'/></button></li>")
                    .append("<li><button class='post__menu__btn'><img src='./img/sendLink.svg'/></button></li>")
                    .append("</ul>")
                    .append("<button class='post__menu__btn'><img src='./img/bookMark.svg'/></button>")
                    .append("</div>");

            // 본문 링크
            list.append("<a href='/article?id=")
                    .append(article.getId())
                    .append("'>")
                    .append("<p class='post__article'>")
                    .append(escapeHtml(truncate(article.getContent(), 300)))
                    .append("</p>")
                    .append("</a>");

            list.append("</div>");
        }
        return list.toString();
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    private static String readFile(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new FileReader(TEMPLATE_DIR + File.separator + filename))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private static String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
