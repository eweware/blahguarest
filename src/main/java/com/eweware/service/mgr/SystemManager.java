package main.java.com.eweware.service.mgr;

import main.java.com.eweware.service.base.cache.BlahCache;
import main.java.com.eweware.service.base.cache.BlahCacheConfiguration;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.mgr.ManagerState;
import main.java.com.eweware.service.rest.session.BlahguaSession;
import org.apache.commons.codec.binary.Base64;

import javax.xml.ws.WebServiceException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 */
public final class SystemManager implements ManagerInterface {

    private static final Logger logger = Logger.getLogger("SystemManager");

    private static SystemManager singleton;

    private ManagerState state = ManagerState.UNINITIALIZED;
    private boolean devMode;
    private final SecureRandom randomizer;
    private final MessageDigest sha1Digest;
    private BlahCache blahCache;
    private final BlahCacheConfiguration blahCacheConfiguration;
    private String restEndpoint;
    private final String clientServiceEndpoint;
    private final boolean cryptoOn;

    /** for dev mode */


    public static SystemManager getInstance() throws SystemErrorException {
        if (SystemManager.singleton == null) {
            throw new SystemErrorException("SystemManager not initialized");
        }
        return SystemManager.singleton;
    }

    public SystemManager(
            boolean cryptoOn,
            String clientServiceEndpoint,
            String cacheHostname,
            String cachePort,
            String devMemcachedHostname,
            String devRestPort
            ) {
        try {
            this.cryptoOn = cryptoOn;
            maybeSetDevelopmentMode();
            if (isDevMode()) {
                cacheHostname = devMemcachedHostname; // same port 21191
                restEndpoint = "localhost:" + devRestPort;
                cryptoOn = true;
            }
            this.clientServiceEndpoint = clientServiceEndpoint;
            final int expirationTime = 0; // TODO refine this?
            this.blahCacheConfiguration = new BlahCacheConfiguration(cacheHostname, cachePort).setInboxBlahExpirationTime(expirationTime);
            this.randomizer = SecureRandom.getInstance("SHA1PRNG");
            this.sha1Digest = MessageDigest.getInstance("SHA-1"); // TODO try SHA-2
        } catch (NoSuchAlgorithmException e) {
            throw new WebServiceException("failed to initialized SystemManager", e);
        }
        SystemManager.singleton = this;
        this.state = ManagerState.INITIALIZED;
        System.out.println("*** SystemManager initialized ***");
    }

    public boolean isCryptoOn() {
        return cryptoOn;
    }

    private void maybeSetDevelopmentMode() {
        final String dev = System.getenv("BLAHGUA_DEV_MODE");
        devMode = (dev != null && dev.equals("true"));
    }

    /**
     * <p>Get the local REST service endpoint (hostname+port) only if in dev mode.</p>
     * @return Local hostname and port for Catalina service
     * @see #isDevMode()
     */
    public String getDevRestEndpoint() {
        return restEndpoint;
    }

    /**
     * <p>Returns true if we're in development mode.</p>
     * @return true if we're in development mode
     * @see #getDevRestEndpoint()
     */
    public boolean isDevMode() {
        return devMode;
    }

    public String getClientServiceEndpoint() {
        return clientServiceEndpoint;
    }


    public String makeShortRandomCode() throws SystemErrorException {
        try {
            String s = new String(Base64.encodeBase64(Long.toHexString(UUID.randomUUID().getLeastSignificantBits()).getBytes("UTF-8")), "UTF-8");
            return s.substring(2, Math.min(14, s.length() - 1)); // TODO does well for 10000+ trials: as a safety valve, the DB will drop a dup code
        } catch (UnsupportedEncodingException e) {
            throw new SystemErrorException("Unable to generate recovery codes", e, ErrorCodes.SERVER_SEVERE_ERROR);
        }
    }

    public ManagerState getState() {
        return state;
    }

    public void start() {
        try {
            this.blahCache = new BlahCache(blahCacheConfiguration);
            this.state = ManagerState.STARTED;
            System.out.println("*** SystemManager started ***");
        } catch (Exception e) {
            throw new WebServiceException("Problem starting SystemManager", e);
        }
    }

    public BlahCache getBlahCache() {
        return blahCache;
    }

    public void shutdown() {
        blahCache.shutdown();
        this.state = ManagerState.SHUTDOWN;
        System.out.println("*** System shut down ***");
    }

    public Map<String, OperationInfo> processMetrics(boolean reset) {
        synchronized (infomapLock) {
            if (reset) {
                operationToOpInfoMap = new HashMap<String, OperationInfo>();
                return null;
            } else {
                return new HashMap<String, OperationInfo>(operationToOpInfoMap);
            }
        }
    }

    public class OperationInfo implements Serializable {

        public long getMax() {
            return max;
        }

        public long getMin() {
            return min;
        }

        public double getAve() {
            return ave;
        }

        public long getCount() {
            return count;
        }

        public Date getStarted() {
            return started;
        }

        public long max = 0; // max response time
        public long min = 0; // min response time
        public double ave = 0; // average response time since last poll
        public long count = 0; // number of posts
        public Date started = new Date(); // time since start of info collection (for this item) in UTC

        public OperationInfo(long min) {
            this.min = min;
        }
    }

    java.util.Map<String, OperationInfo> operationToOpInfoMap = new HashMap<String, OperationInfo>();
    final Object infomapLock = new Object();

    public void setResponseTime(String operation, long responseTimeInMs) {
        synchronized (infomapLock) {
            OperationInfo info = operationToOpInfoMap.get(operation);
            if (info == null) {
                info = new OperationInfo(responseTimeInMs);
                operationToOpInfoMap.put(operation, info);
            }
            // 1 3 ave 2  , now add 4  ave = 2 + (4 - 2)/3 =
            if (info.max < responseTimeInMs) {
                info.max = responseTimeInMs;
            }
            if (info.min > responseTimeInMs) {
                info.min = responseTimeInMs;
            }
            info.count++;
            info.ave += (responseTimeInMs - info.ave) / info.count;
        }
    }
}
