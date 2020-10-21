package icn.icmyas.Models;

import java.io.Serializable;

public class NewsArticle implements Serializable {

    private String title, content, userId, createdAt;

    public NewsArticle(String title, String content, String userId, String createdAt) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getUserId() {
        return userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
