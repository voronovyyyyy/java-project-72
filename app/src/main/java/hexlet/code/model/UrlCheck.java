package hexlet.code.model;

import java.sql.Timestamp;

public final class UrlCheck {
    private Long id;
    private Long urlId;
    private int statusCode;
    private String title;
    private String h1;
    private String description;
    private Timestamp createdAt;

    public UrlCheck(Long urlId, int statusCode, String title, String h1, String description) {
        this.urlId = urlId;
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        id = value;
    }

    public Long getUrlId() {
        return urlId;
    }

    public void setUrlId(Long value) {
        urlId = value;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int value) {
        statusCode = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        title = value;
    }

    public String getH1() {
        return h1;
    }

    public void setH1(String value) {
        h1 = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        description = value;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp value) {
        createdAt = value;
    }
}
