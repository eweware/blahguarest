package main.java.com.eweware.service.mgr;

import main.java.com.eweware.service.base.cache.BlahCache;
import main.java.com.eweware.service.base.cache.BlahCacheConfiguration;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.mgr.ManagerState;
import main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
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

/**
 * @author rk@post.harvard.edu
 */
public final class SystemManager implements ManagerInterface {

	private static SystemManager singleton;

    private ManagerState state = ManagerState.UNINITIALIZED;
    private final SecureRandom randomizer;
    private final MessageDigest sha1Digest;
    private BlahCache blahCache;
    private final BlahCacheConfiguration blahCacheConfiguration;
    

    public static SystemManager getInstance() throws SystemErrorException {
		if (SystemManager.singleton == null) {
			throw new SystemErrorException("SystemManager not initialized");
		}
		return SystemManager.singleton;
	}

	public SystemManager(String cacheHostname, String cachePort) {
        try {
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

    private static int code = 1;
    public String makeShortRandomCode() {
        try {
            String s = new String(Base64.encodeBase64(Long.toHexString(UUID.randomUUID().getLeastSignificantBits()).getBytes("UTF-8")), "UTF-8");
            return s.substring(2, Math.min(14, s.length() - 1)); // TODO does well for 10000+ trials: as a safety valve, the DB will drop a dup code
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ManagerState getState() {
		return state;
	}

	public void start() {
		try {
            this.blahCache = new BlahCache(blahCacheConfiguration);
            final MongoStoreManager storeMgr = (MongoStoreManager) MongoStoreManager.getInstance();
            checkManagers(storeMgr);
//            initializeDateFormats();
            this.state = ManagerState.STARTED;
            System.out.println("*** SystemManager started ***");
        } catch (Exception e) {
			e.printStackTrace();
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

	/** Ensures all managers are STARTED **/
	private void checkManagers(MongoStoreManager storeMgr)
			throws SystemErrorException {
		if (storeMgr.getState() != ManagerState.STARTED) {
			throw new SystemErrorException("MongoStoreManager failed to start");
		}
		if (BlahManager.getInstance().getState() != ManagerState.STARTED) {
			throw new SystemErrorException("BlahManager failed to start");
		}
		if (GroupManager.getInstance().getState() != ManagerState.STARTED) {
			throw new SystemErrorException("GroupManager failed to start");
		}
		if (UserManager.getInstance().getState() != ManagerState.STARTED) {
			throw new SystemErrorException("UserManager failed to start");
		}
        // TODO tracking mgr, media mgr.
	}

    public Map<String, OperationInfo> getStats(boolean reset) {
        Map<String, OperationInfo> stats = null;
        synchronized (infomapLock) {
            if (reset) {
                stats = operationToOpInfoMap;
                operationToOpInfoMap = new HashMap<String, OperationInfo>();
            } else {
                stats = new HashMap<String, OperationInfo>(operationToOpInfoMap);
            }
        }
        return stats;
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
