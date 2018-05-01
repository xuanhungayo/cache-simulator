package cachesimulator.server;

import cachesimulator.cache.Cache;
import cachesimulator.cache.FSACache;
import cachesimulator.cache.LFUCache;
import cachesimulator.cache.LRUCache;
import cachesimulator.cache.SACache;
import cachesimulator.common.Config;
import cachesimulator.common.Helper;
import cachesimulator.model.CacheStat;

/**
 *
 * @author hungcx
 */
public class ServerSimulator {

	private CacheStat stat;
	private Cache cache;

	public ServerSimulator(String policy, long cacheSize) {
		if (policy.equals("LRU")) {
			stat = new CacheStat("LRU");
			cache = new LRUCache(cacheSize, stat);
		}
		if (policy.equals("LFU")) {
			stat = new CacheStat("LFU");
			cache = new LFUCache(cacheSize, stat);
		}
		if (policy.equals("SA")) {
			stat = new CacheStat("SA");
			cache = new SACache(cacheSize, stat);
		}
		if (policy.equals("FSA")) {
			stat = new CacheStat("FSA");
			cache = new FSACache(cacheSize, stat);
		}
		
	}

	public void handleRequest(String url, long time) {
		long size = cache.get(url, time);
		if (size >= 0) { // a cache hit
			return;
		} else {
			cache.put(url, Helper.getSize(url), time);
		}
	}

	public void printStat() {
		stat.printHitRate();
		stat.printByteRate();
		stat.printOtherStat();
	}

}
