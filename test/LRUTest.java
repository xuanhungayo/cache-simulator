
import cachesimulator.cache.Cache;
import cachesimulator.cache.LRUCache;
import cachesimulator.model.CacheStat;


/**
 *
 * @author hungcx
 */
public class LRUTest {
	public static void main(String[] args) {
		CacheStat stat = new CacheStat("LRUCache");
		Cache cache = new LRUCache(10, stat);
		cache.put("1", 1, 0);
		cache.put("4", 4, 0);
		cache.put("3", 3, 0);
		assert cache.get("4", 0) == 4;
		cache.put("2", 2, 0);
		cache.put("5", 5, 0);
		assert cache.get("3", 0) == -1 ;
		assert cache.get("4", 0) == -1;
		assert cache.get("2", 0) == 2;
		System.out.println("Done!!");
	}
}
