package com.eweware.service.base.payload;

import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/24/12 Time: 6:59 PM
 */
public class CommentTrackerPayload extends BasePayload {

    public CommentTrackerPayload() {
        super();
    }

    public CommentTrackerPayload(String id) {
        super(id);
    }

    public CommentTrackerPayload(Map<String, Object> map) {
        super(map);
    }

}
