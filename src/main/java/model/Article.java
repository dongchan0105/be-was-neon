package model;

/**
 * Article 모델: 이미지 URL 지원 추가
 */
public class Article {

    private static int idSequence = 1;
    private final int id;
    private String title;
    private User author;
    private String content;
    private String imageUrl; // 업로드한 이미지 경로 (public URL)

    /**
     * 기본 생성자: 이미지 없는 경우
     */
    public Article(String title, User author, String content) {
        this.id = idSequence++;
        this.title = title;
        this.author = author;
        this.content = content;
        this.imageUrl = null;
    }

    /**
     * 이미지 포함 생성자
     */
    public Article(String title, User author, String content, String imageUrl) {
        this.id = idSequence++;
        this.title = title;
        this.author = author;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 업로드된 이미지 URL 반환
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * 이미지 URL 설정
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "Article [id=" + id + ", title=" + title +
                ", author=" + author + ", content=" + content +
                ", imageUrl=" + imageUrl + "]";
    }
}
