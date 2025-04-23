package model;

public class Article {

    private static int idSequence = 1;
    private final int id;
    private String title;
    private User author;
    private String content;

    public Article(String title, User author, String content) {
        this.id = idSequence++;
        this.title = title;
        this.author = author;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    // setId 제거 (직접 설정 금지하는 게 안전함)
    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public User getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Article [id=" + id + ", title=" + title + ", author=" + author + ", content=" + content + "]";
    }
}
