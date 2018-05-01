package cachesimulator.cache;

import cachesimulator.model.CacheStat;


/**
 *
 * @author hungcx
 */
public abstract class Cache {
	protected long capacity; // Cache capacity in byte
	protected long curSize = 0;
	protected CacheStat stat;
	
	public Cache(long cap, CacheStat stat) {
		this.capacity = cap;
		this.stat = stat;
	}
	
	// Return the size of the according object
	public abstract long get(String key, long time);
	// Put objects with size to cache
	public abstract void put(String key, long size, long time);
}
