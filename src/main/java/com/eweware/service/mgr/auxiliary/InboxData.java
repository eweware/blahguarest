package com.eweware.service.mgr.auxiliary;

import java.util.List;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/7/13 Time: 11:56 PM
 */
public class InboxData {

    private final Integer _inboxNumber;
    private final List<Map<String, Object>> _inboxItems;

    public InboxData(List<Map<String, Object>> inboxItems) {
        this(-1, inboxItems);
    }

    public InboxData(Integer inboxNumber, List<Map<String, Object>> inboxItems) {
        _inboxNumber = inboxNumber;
        _inboxItems = inboxItems;
    }

    public Integer getInboxNumber() {
        return _inboxNumber;
    }

    public List<Map<String, Object>> getInboxItems() {
        return _inboxItems;
    }
}
