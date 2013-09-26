package com.eweware.service.base.i18n;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/13/12 Time: 7:41 PM
 *
 *         Encapsulates our locale identifier
 */
public class LocaleId {

    public static final LocaleId en_us = new LocaleId("en_us");

    private String bcp47;

    public LocaleId(String bcp47) {
        this.bcp47 = bcp47;
    }
}
