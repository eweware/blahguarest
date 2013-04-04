package main.java.com.eweware.service.base.store.dao.type;

/**
 * @author rk@post.harvard.edu
 *         Date: 2/16/13 Time: 10:59 AM
 */
public enum BlahTypeCategoryType {

    /**
     * <p> The default blah. Just text with comments.</p>
     */
    DEFAULT(0),

    /**
     * <p> A blah that follows the pattern of a poll.</p>
     */
    POLL(1),

    /**
     * <p>A blah that follows the pattern of a prediction.</p>
     */
    PREDICTION(2),

    /**
     * <p>An advert blah.</p>
     */
    ADVERT(3);

    private final Integer categoryId;

    BlahTypeCategoryType(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public static BlahTypeCategoryType findByCategoryId(Integer category) {
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
