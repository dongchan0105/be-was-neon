package dto;

import model.User;

public record WriteArticleRequest(String title, User author, String content) {
}
