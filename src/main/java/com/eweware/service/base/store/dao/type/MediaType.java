package main.java.com.eweware.service.base.store.dao.type;

/**
 * <p>A type of media</p>
 * @author rk@post.harvard.edu
 *         Date: 5/20/13 Time: 5:47 PM
 */
public enum MediaType {

    I("image");

    private final String description;

    MediaType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
