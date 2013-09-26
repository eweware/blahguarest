package com.eweware.service.base.payload;

import com.eweware.service.base.store.dao.PollOptionDAOConstants;

/**
 * @author rk@post.harvard.edu
 *         Date: 2/15/13 Time: 6:08 PM
 */
public class PollOptionsTextPayload extends BasePayload implements PollOptionDAOConstants {

    public PollOptionsTextPayload() {
    }

    public PollOptionsTextPayload(String tagLine, String text) {
        setTagLine(tagLine);
        setText(text);
    }

    public String getTagLine() {
        return (String) get(TAGLINE);
    }

    public void setTagLine(String tagLine) {
        put(TAGLINE, tagLine);
    }

    public String getText() {
        return (String) get(TEXT);
    }

    public void setText(String text) {
        put(TEXT, text);
    }
}
