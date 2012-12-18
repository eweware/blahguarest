package main.java.com.eweware.service.base.cache;

import main.java.com.eweware.service.base.payload.InboxBlahPayload;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rk@post.harvard.edu
 *         Date: 10/8/12 Time: 3:42 PM
 */
public class Inbox implements Serializable {

    private static final ArrayList<InboxBlahPayload> EMPTY = new ArrayList<InboxBlahPayload>(0);

    /**
     * The highest inbox number for an implied group
     */
    private final Integer topInbox;

    /**
     * Inbox items.
     */
    private final List<InboxBlahPayload> items;

    public Inbox(Integer topInbox, List<InboxBlahPayload> items) {
        this.topInbox = topInbox;
        this.items = (items == null) ? EMPTY : items;
    }

    public List<InboxBlahPayload> getItems() {
        return items;
    }

    public Integer getTopInbox() {
        return topInbox;
    }

    /**
     * @return boolean  Returns true if this inbox is empty.
     */
    public boolean isEmpty() {
        return (items == null || items.isEmpty());
    }
}
