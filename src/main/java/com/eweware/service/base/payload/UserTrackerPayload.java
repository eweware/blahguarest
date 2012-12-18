package main.java.com.eweware.service.base.payload;

import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/6/12 Time: 4:25 PM
 */
public class UserTrackerPayload extends BasePayload {

    public UserTrackerPayload() {
        super();
    }

    public UserTrackerPayload(String id) {
        super(id);
    }

    public UserTrackerPayload(Map<String, Object> map) {
        super(map);
    }
}
