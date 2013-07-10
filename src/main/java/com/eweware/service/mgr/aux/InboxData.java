package main.java.com.eweware.service.mgr.aux;

import java.util.List;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/7/13 Time: 11:56 PM
 */
public class InboxData {

    private final Integer inboxNumber;
    private final List<Map<String, Object>> inboxItems;

    public InboxData(List<Map<String, Object>> inboxItems) {
        this(-1, inboxItems);
    }

    public InboxData(Integer inboxNumber, List<Map<String, Object>> inboxItems) {
        this.inboxNumber = inboxNumber;
        this.inboxItems = inboxItems;
    }

    public Integer getInboxNumber() {
        return inboxNumber;
    }

    public List<Map<String, Object>> getInboxItems() {
        return inboxItems;
    }
}
