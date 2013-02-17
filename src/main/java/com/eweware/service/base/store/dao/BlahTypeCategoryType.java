package main.java.com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 2/16/13 Time: 10:59 AM
 */
public enum BlahTypeCategoryType {

    DEFAULT(0),
    POLL(1);
    private final Integer categoryId;

    BlahTypeCategoryType(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public BlahTypeCategoryType findByCategoryId(Integer category) {
        if (category == null) {
            return DEFAULT;
        }
        for (BlahTypeCategoryType type : BlahTypeCategoryType.values()) {
            if (type.categoryId != null && type.categoryId.equals(category)) {
                return type;
            }
        }
        return null;
    }
}
