package main.java.com.eweware.service.base.mgr;

import main.java.com.eweware.service.base.cache.BlahCache;
import main.java.com.eweware.service.base.cache.BlahCacheConfiguration;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.type.RunMode;
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
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 */
public final class SystemManager implements ManagerInterface {

    private static final Logger logger = Logger.getLogger(SystemManager.class.getName());

    private static SystemManager singleton;
    private ManagerState state = ManagerState.UNKNOWN;


    private RunMode runMode;
    private boolean qaMode;
    private boolean devMode;
    private boolean prodMode;

    private final SecureRandom randomizer;
    private final MessageDigest sha1Digest;
    private BlahCache blahCache;
    private final BlahCacheConfiguration blahCacheConfiguration;
    private final boolean cryptoOn;
    private HttpClient client;

    private PoolingClientConnectionManager connectionPoolMgr;
    private Integer maxHttpConnections;
    private Integer maxHttpConnectionsPerRoute;
    private Integer httpConnectionTimeoutInMs;

    private String prodBaseUrl;
    private final String prodBaseUrlWithVersion;
    private String restServiceBaseUrl;  // e.g., https://beta.blahgua.com
    private String restServiceBaseUrlWithVersion;  // e.g., https://beta.blahgua.com/v2
    private String prodS3WebsiteProdBucket;
    private String s3WebsiteBucket;
    private String s3BaseUrl; // The base URL with protocol for all S3 buckets (in PROD, QA, and DEV)  private String prodImagesBucketUrl;
    private String prodImagesBucketUrl;
    private String imagesBucketUrl;

    private String qaBadgeAuthorityEndpoint; // contains protocol, hostname, port, and REST version
    private Integer qaBadgeAuthorityPort; // for http client
    private String devBadgeAuthorityEndpoint; // contains protocol, hostname, port, and REST version
    private Integer devBadgeAuthorityPort; // for http client

    public static SystemManager getInstance() throws SystemErrorException {
        if (SystemManager.singleton == null) {
            throw new SystemErrorException("SystemManager not initialized");
        }
        return SystemManager.singleton;
    }

    public SystemManager(
            String runMode,
            String prodRestVersion,
            String qaRestVersion,
            String devRestVersion,
            String logLevel,
            boolean cryptoOn,
            String prodRestProtocol,
            String prodRestHostname,
            String qaRestProtocol,
            String qaRestHostname,
            String qaRestPort,
            String devRestProtocol,
            String devRestHostname,
            String devRestPort,
            String s3BaseUrl, // we assume that it is the same for QA, DEV and PROD
            String prodImagesBucket,
            String qaImagesBucket,
            String devImagesBucket,
            String s3WebsiteProdBucket,
            String s3WebsiteQABucket,
            String s3WebsiteDevBucket
    ) {
        final String randomProvider = "SHA1PRNG";
        try {
            configureLogger(logLevel);

            setRunMode(runMode);

            if (qaMode) {
                restServiceBaseUrl = qaRestProtocol + "://" + qaRestHostname + ":" + qaRestPort;
                restServiceBaseUrlWithVersion = restServiceBaseUrl + "/" + qaRestVersion;
                imagesBucketUrl = s3BaseUrl + "/" + qaImagesBucket;
                s3WebsiteBucket = s3WebsiteQABucket;
            } else if (devMode) {
                restServiceBaseUrl = devRestProtocol + "://" + devRestHostname + ":" + devRestPort;
                restServiceBaseUrlWithVersion = restServiceBaseUrl + "/" + devRestVersion;
                imagesBucketUrl = s3BaseUrl + "/" + devImagesBucket;
                s3WebsiteBucket = s3WebsiteDevBucket;
            } else { // RunMode.PROD
                restServiceBaseUrl = prodRestProtocol + "://" + prodRestHostname; // port 80
                restServiceBaseUrlWithVersion = restServiceBaseUrl + "/" + prodRestVersion;
                imagesBucketUrl = s3BaseUrl + "/" + prodImagesBucket;
                s3WebsiteBucket = s3WebsiteProdBucket;
            }
            this.s3BaseUrl = s3BaseUrl;
            this.prodBaseUrl = prodRestProtocol + "://" + prodRestHostname;
            this.prodBaseUrlWithVersion = prodRestProtocol + "://" + prodRestHostname + "/" + prodRestVersion;
            this.prodS3WebsiteProdBucket = s3WebsiteProdBucket;
            this.prodImagesBucketUrl = s3BaseUrl + "/" + prodImagesBucket;

            this.cryptoOn = cryptoOn;

            logger.info("s3BaseUrl=" + s3BaseUrl);
            logger.info("restServiceBaseUrl=" + restServiceBaseUrl);
            logger.info("restServiceBaseUrlWithVersion=" + restServiceBaseUrlWithVersion);
            logger.info("s3WebsiteBucket=" + s3WebsiteBucket);
            logger.info("imagesBucketUrl=" + imagesBucketUrl);
            logger.info("crypto " + (cryptoOn ? "on" : "off"));

            final int expirationTime = 0; // TODO refine this?
            this.blahCacheConfiguration = new BlahCacheConfiguration(null, null).setInboxBlahExpirationTime(expirationTime);
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

    public String getProdImagesBucketUrl() {
        return prodImagesBucketUrl;
    }

    public String getImagesBucketUrl() {
        return imagesBucketUrl;
    }

    public String getProdBaseUrlWithVersion() {
        return prodBaseUrlWithVersion;
    }

    public String getProdBaseUrl() {
        return prodBaseUrl;
    }

    public String getRestServiceBaseUrlWithVersion() {
        return restServiceBaseUrlWithVersion;
    }

    public String getS3BaseUrl() {
        return s3BaseUrl;
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

    private void setRunMode(String mode) {
        final String modeProp = System.getProperty("blagua.run.mode");
        if (modeProp != null) {
            System.out.println("Run mode from System property=" + modeProp);
        }
        try {
            this.runMode = RunMode.valueOf((modeProp == null) ? mode.toUpperCase() : modeProp.toUpperCase());
        } catch (Exception e) {
            throw new WebServiceException("Invalid run mode '" + mode + "'");
        }
        qaMode = (runMode == RunMode.QA);
        if (qaMode) {
            System.out.println(">>> STARTING IN QA MODE <<<");
        } else {
            devMode = (runMode == RunMode.DEV);
            if (devMode) {
                System.out.println(">>> STARTING IN DEVELOPMENT MODE <<<");
            } else {
                prodMode = true;
                System.out.println(">>> STARTING IN PRODUCTION MODE <<<");
            }
        }
    }
    public RunMode getRunMode() {
        return runMode;
    }
    public boolean isQaMode() {
        return qaMode;
    }
    public boolean isDevMode() {
        return devMode;
    }
    public boolean isProdMode() {
        return prodMode;
    }

    /**
     * <p>Get the  REST service endpoint base Url. E.g., "http://qa.rest.blahgua.com:8080".</p>
     * <p>Provides protocol, hostname, and port.</p>
     *
     * @return Base URL for REST service
     */
    public String getRestServiceBaseUrl() {
        return restServiceBaseUrl;
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

//    public void setMemcachedEnable(boolean on) throws SystemErrorException {
//        getBlahCache().setMemcachedEnable(on);
//    }

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

//    public BlahCache getBlahCache() {
//        return blahCache;
//    }

    public void shutdown() {
        if (connectionPoolMgr != null) {
            connectionPoolMgr.shutdown();
        }
//        blahCache.shutdown();
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

    /**
     * This client talks to the badge authority.
     */
    private void startHttpClient() {
        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        if (qaMode) {
//            schemeRegistry.register(new Scheme("http", 8080, PlainSocketFactory.getSocketFactory()));
            schemeRegistry.register(new Scheme("http", getQaBadgeAuthorityPort(), PlainSocketFactory.getSocketFactory()));
        } else if (devMode) {
//            schemeRegistry.register(new Scheme("http", 8080, PlainSocketFactory.getSocketFactory()));
            schemeRegistry.register(new Scheme("http", getDevBadgeAuthorityPort(), PlainSocketFactory.getSocketFactory()));
        } else { // PROD
            schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
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

    public String getQaBadgeAuthorityEndpoint() {
        return qaBadgeAuthorityEndpoint;
    }

    public void setQaBadgeAuthorityEndpoint(String endpoint) {
        this.qaBadgeAuthorityEndpoint = endpoint;
    }

    public Integer getQaBadgeAuthorityPort() {
        return qaBadgeAuthorityPort;
    }

    public void setQaBadgeAuthorityPort(Integer port) {
        qaBadgeAuthorityPort = port;
    }

    public String getDevBadgeAuthorityEndpoint() {
        return devBadgeAuthorityEndpoint;
    }

    public void setDevBadgeAuthorityEndpoint(String endpoint) {
        devBadgeAuthorityEndpoint = endpoint;
    }

    public Integer getDevBadgeAuthorityPort() {
        return devBadgeAuthorityPort;
    }

    public void setDevBadgeAuthorityPort(Integer port) {
        devBadgeAuthorityPort = port;
    }

    public String getS3WebsiteProdBucket() {
        return prodS3WebsiteProdBucket;
    }

    public void setS3WebsiteProdBucket(String bucket) {
        prodS3WebsiteProdBucket = bucket;
    }

    public String getWebsiteBucket() {
        return s3WebsiteBucket;
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
