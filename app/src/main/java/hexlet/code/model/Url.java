package hexlet.code.model;

import java.sql.Timestamp;

public final class Url {
    private Long id;
    private String name;
    private Timestamp createdAt;

    public Url(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        id = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp value) {
        createdAt = value;
    }
}
