package com.eweware.service.base.cache;

/**
 * @author rk@post.harvard.edu
 *         Date: 10/6/12 Time: 9:08 PM
 */
public class BlahCacheConfiguration {

    public String getHostname() {
        return hostname;
    }

    public String getPort() {
        return port;
    }

    public Integer getInboxBlahExpiration() {
        return inboxBlahExpiration;
    }

    private final String hostname;
    private final String port;
    private Integer inboxBlahExpiration;

    public BlahCacheConfiguration(String hostname, String port) {
        this.hostname = hostname;
        this.port = port;
    }

    public BlahCacheConfiguration setInboxBlahExpirationTime(Integer expirationTime) {
        this.inboxBlahExpiration = expirationTime;
        return this;
    }

    public String toString() {
        return "[memcached hostname=" + hostname + " port=" + port + "]";
    }


}
