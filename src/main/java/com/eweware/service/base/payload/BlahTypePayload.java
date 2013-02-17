package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.store.dao.BlahTypeDAOConstants;

import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/23/12 Time: 2:58 PM
 */
public class BlahTypePayload extends BasePayload implements BlahTypeDAOConstants {

    public BlahTypePayload() {
        super();
    }

    public BlahTypePayload(String id) {
        super(id);
    }

    public BlahTypePayload(Map<String, Object> map) {
        super(map);
    }

    public String getName() {
        return (String) get(NAME);
    }

    public void setName(String name) {
        put(NAME, name);
    }

    public Integer getCategoryId() {
        return (Integer) get(CATEGORY_ID);
    }

    public void setCategoryId(Integer categoryId) {
        put(CATEGORY_ID, categoryId);
    }
}
