package main.java.com.eweware.service.base.mgr;

import main.java.com.eweware.service.base.cache.BlahCache;
import main.java.com.eweware.service.base.cache.BlahCacheConfiguration;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.log.LogFormatter;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

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
import java.util.logging.Level;
import java.util.logging.LogManager;
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
    private HttpClient client;

    private PoolingClientConnectionManager connectionPoolMgr;
    private Integer maxHttpConnections;
    private Integer maxHttpConnectionsPerRoute;
    private Integer httpConnectionTimeoutInMs;
    private Integer devBadgeAuthorityPort;

    /** for dev mode */


    public static SystemManager getInstance() throws SystemErrorException {
        if (SystemManager.singleton == null) {
            throw new SystemErrorException("SystemManager not initialized");
        }
        return SystemManager.singleton;
    }

    public SystemManager(
            String logLevel,
            boolean cryptoOn,
            String clientServiceEndpoint,
            String memcachedHostname,
            String memcachedPort,
            String devMemcachedHostname,
            String devRestPort
            ) {
        final String randomProvider = "SHA1PRNG";
        try {
            configureLogger(logLevel);
            this.cryptoOn = cryptoOn;
            maybeSetDevelopmentMode();
            if (isDevMode()) {
                if ((System.getenv("BLAHGUA_DEBUG_AWS") == null)) {
                    memcachedHostname = devMemcachedHostname; // same port 21191
                }
                logger.info("Memcached hostname '" + memcachedHostname + "' port '" + memcachedPort + "'");
                restEndpoint = "localhost:" + devRestPort;
                cryptoOn = true;
            }
            logger.info("*** Crypto is " + (cryptoOn ? "on" : "off") + " ***");
            this.clientServiceEndpoint = clientServiceEndpoint;
            final int expirationTime = 0; // TODO refine this?
            this.blahCacheConfiguration = new BlahCacheConfiguration(memcachedHostname, memcachedPort).setInboxBlahExpirationTime(expirationTime);
            this.randomizer = SecureRandom.getInstance(randomProvider);
            randomizer.generateSeed(20);
            this.sha1Digest = MessageDigest.getInstance("SHA-1"); // TODO try SHA-2
        } catch (NoSuchAlgorithmException e) {
            throw new WebServiceException("Failed to initialized SystemManager due to unavailable secure random provider '" + randomProvider + "'", e);
        } catch (Exception e) {
            throw new WebServiceException("Failed to initialize SystemManager", e);
        }
        SystemManager.singleton = this;
        this.state = ManagerState.INITIALIZED;
        System.out.println("*** SystemManager initialized ***");
    }

    public String getSecureRandomString() throws SystemErrorException {
        // TODO reseed this once in a while?
        final byte[] rand = new byte[20];
        try {
            return new String(rand, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new SystemErrorException("Unable to create secure random string", e, ErrorCodes.SERVER_CRYPT_ERROR);
        }
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
            throw new SystemErrorException("Unable to generate recovery codes", e, ErrorCodes.SERVER_CRYPT_ERROR);
        }
    }

    public ManagerState getState() {
        return state;
    }

    public void setMemcachedEnable(boolean on) throws SystemErrorException {
        getBlahCache().setMemcachedEnable(on);
    }

    public void start() {
        try {
            this.blahCache = new BlahCache(blahCacheConfiguration);
            startHttpClient();
            this.state = ManagerState.STARTED;
            System.out.println("*** SystemManager started ***");
        } catch (Exception e) {
            throw new WebServiceException("Problem starting SystemManager", e);
        }
    }

    private void configureLogger(String logLevel) {
//        final Logger logmgrlogger = LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME);
//        logmgrlogger.setLevel(Level.parse(logLevel));
//        final LogFormatter logFormatter = new LogFormatter();

    }

    public BlahCache getBlahCache() {
        return blahCache;
    }

    public void shutdown() {
        if (connectionPoolMgr != null) {
            connectionPoolMgr.shutdown();
        }
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

    public HttpClient getHttpClient() {
        return client;
    }

    private void startHttpClient() {
        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        if (isDevMode()) { // Debug BA port 8081
            schemeRegistry.register(new Scheme("http", getDevBadgeAuthorityPort(), PlainSocketFactory.getSocketFactory()));
        }
        connectionPoolMgr = new PoolingClientConnectionManager(schemeRegistry);
        connectionPoolMgr.setMaxTotal(getMaxHttpConnections()); // maximum total connections
        connectionPoolMgr.setDefaultMaxPerRoute(getMaxHttpConnectionsPerRoute()); // maximumconnections per route

        // Create a client that can be shared by multiple threads
        client = new DefaultHttpClient(connectionPoolMgr);

        // Set timeouts (if not set, thread may block forever)
        final HttpParams httpParams = client.getParams();
        httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, getHttpConnectionTimeoutInMs());
        httpParams.setLongParameter(ConnManagerPNames.TIMEOUT, getHttpConnectionTimeoutInMs());
        httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, getHttpConnectionTimeoutInMs());

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (connectionPoolMgr != null) {
                    connectionPoolMgr.shutdown();
                }
            }
        }));
    }

    public Integer getMaxHttpConnections() {
        return maxHttpConnections;
    }

    public void setMaxHttpConnections(Integer maxHttpConnections) {
        this.maxHttpConnections = maxHttpConnections;
    }

    public Integer getMaxHttpConnectionsPerRoute() {
        return maxHttpConnectionsPerRoute;
    }

    public void setMaxHttpConnectionsPerRoute(Integer maxHttpConnectionsPerRoute) {
        this.maxHttpConnectionsPerRoute = maxHttpConnectionsPerRoute;
    }

    public Integer getHttpConnectionTimeoutInMs() {
        return httpConnectionTimeoutInMs;
    }

    public void setHttpConnectionTimeoutInMs(Integer httpConnectionTimeoutInMs) {
        this.httpConnectionTimeoutInMs = httpConnectionTimeoutInMs;
    }

    public Integer getDevBadgeAuthorityPort() {
        return devBadgeAuthorityPort;
    }

    public void setDevBadgeAuthorityPort(Integer port) {
        this.devBadgeAuthorityPort = port;
    }


//    private void startHttpClientOld() {
//
//        client = new DefaultHttpClient();
//        connectionManager = client.getConnectionManager();
//        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//            @Override
//            public void run() {
//                if (connectionManager != null) {
//                    connectionManager.shutdown();
//                }
//            }
//        }));
//    }

//    public static DefaultHttpClient createHttpClient(String endpoint, Integer port) {
//        SchemeRegistry schemeRegistry = new SchemeRegistry();
//        schemeRegistry.register(
//                new Scheme("http", port, PlainSocketFactory.getSocketFactory()));
////        schemeRegistry.register(
////                new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
//
//        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
//        cm.setMaxTotal(400);
//        cm.setDefaultMaxPerRoute(100);
////        HttpHost host = new HttpHost(endpoint, port);
////        cm.setMaxPerRoute(new HttpRoute(host), 400);
//        return new DefaultHttpClient(cm);
//    }

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
