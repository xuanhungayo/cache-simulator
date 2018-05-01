package cachesimulator.cache;

import cachesimulator.common.Helper;
import cachesimulator.model.CacheStat;
import cachesimulator.model.DoubleLinkedList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hungcx
 */
public class LRUCache extends Cache {

	private static class Entry {

		public long size;
		public DoubleLinkedList.Node node;

		Entry(long size, DoubleLinkedList.Node node) {
			this.size = size;
			this.node = node;
		}
	}

	private DoubleLinkedList lruList = new DoubleLinkedList();
	private Map<String, Entry> cache = new HashMap<>();

	public LRUCache(long cap, CacheStat stat) {
		super(cap, stat);
	}

	@Override
	public long get(String key, long time) {
		if (cache.containsKey(key)) {
			refresh(key);
			Entry data = cache.get(key);
			stat.hit(data.size);
			return data.size;
		}
		stat.miss(Helper.getSize(key));
		return -1;
	}

	@Override
	public void put(String key, long size, long time) {
		if (size > capacity) {
			return;
		}
		// Currently we never run into this case
//		if (cache.containsKey(key)) {
//			refresh(key);
//			return;
//		}

		while (curSize + size > capacity) {
			evict();
		}
		lruList.pushFront(key);
		Entry entry = new Entry(size, lruList.begin());
		cache.put(key, entry);
		curSize += size;
	}

	// Bring key to the most recently used position
	private void refresh(String key) {
		Entry entry = cache.get(key);
		lruList.remove(entry.node);
		lruList.pushFront(key);
		entry.node = lruList.begin();
	}

	// Evict the least recently used object
	private void evict() {
		String key = lruList.back();
		long size = cache.get(key).size;

		lruList.popBack();
		cache.remove(key);
		curSize -= size;
	}
}
