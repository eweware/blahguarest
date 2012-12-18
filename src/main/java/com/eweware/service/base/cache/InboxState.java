package main.java.com.eweware.service.base.cache;

import java.io.Serializable;
import java.util.List;

/**
 * @author rk@post.harvard.edu
 *         Date: 10/8/12 Time: 3:06 PM
 */
public class InboxState implements Serializable {

    private final Integer topInbox;
    private final List<String> itemIds;

    public InboxState(Integer topInbox, List<String> itemIds) {

        this.topInbox = topInbox;
        this.itemIds = itemIds;
    }

    public Integer getTopInbox() {
        return topInbox;
    }

    public List<String> getItemIds() {
        return itemIds;
    }

    public String toString() {
        final StringBuilder b = new StringBuilder("[inboxstate top=");
        b.append(topInbox);
        b.append(" idCount=");
        b.append(itemIds.size());
        b.append(" ids=");
        b.append(itemIds);
        b.append("]");
        return b.toString();
    }
}
