package handler;

import dto.WriteArticleRequest;
import org.apache.commons.fileupload.FileItem;
import model.Article;
import model.User;
import service.ArticleService;
import session.SessionManager;
import utils.ImageStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import static session.SessionManager.SESSION_COOKIE_NAME;

public class ArticleWriteHandler implements ReturnViewPathHandler {
    private static final Logger log = LoggerFactory.getLogger(ArticleWriteHandler.class);

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        // 기본 파라미터
        String title = paramMap.getOrDefault("title", "");
        String content = paramMap.getOrDefault("content", "");
        String sessionId = paramMap.getOrDefault(SESSION_COOKIE_NAME, "");
        User user = SessionManager.getUser(sessionId);

        // 이미지 업로드 처리
        String imageUrl = "";
        @SuppressWarnings("unchecked")//케스팅 할때 경고 무시하기
        Map<String, FileItem> fileItems = (Map<String, FileItem>) model.get("fileItems");
        if (fileItems != null && fileItems.containsKey("image")) {
            FileItem imageItem = fileItems.get("image");
            if (imageItem.getSize() > 0) {
                try {
                    imageUrl = ImageStorage.save(imageItem);
                } catch (IOException e) {
                    log.error("Image saving failed", e);
                }
            }
        }

        // DTO 생성 (이미지 URL 포함)
        WriteArticleRequest req = new WriteArticleRequest(title, user, content, imageUrl);

        // 비즈니스 로직 및 저장
        Article article = ArticleService.writeArticle(req);

        // 모델에 결과 담기
        model.put("article", article);
        return "redirect:/index";
    }
}