package cachesimulator.cache;

import cachesimulator.common.Helper;
import cachesimulator.model.CacheStat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author hungcx
 */
public class LFUCache extends Cache {
	private static class Entry {
		long frequency;
		long size;
		public Entry() {
		}
	}
	
	private static class SetEntry implements Comparable<SetEntry>{
		long frequency;
		String key;
		public SetEntry() {
		}
		
		@Override
		public int compareTo(SetEntry e) {
			if (frequency < e.frequency)
				return -1;
			if (frequency > e.frequency)
				return 1;
			int compareKey = key.compareTo(e.key);
			return compareKey;
		}
	}
	
	
	private TreeSet<SetEntry> frequencySet = new TreeSet();
	private Map<String, Entry> cache = new HashMap();
	
	public LFUCache(long capacity, CacheStat stat) {
		super(capacity, stat);
	}
	
	@Override 
	public long get(String key, long time) {
		if (cache.containsKey(key)) {
			update(key);
			Entry entry = cache.get(key);
			stat.hit(entry.size);
			return entry.size;
		}
		stat.miss(Helper.getSize(key));
		return -1;
	}
	
	@Override
	public void put(String key, long size, long time) {
		if (size > capacity)
			return;
		while (curSize + size > capacity)
			evict();
		
		SetEntry setEntry = new SetEntry();
		setEntry.frequency = 1;
		setEntry.key = key;
		frequencySet.add(setEntry);
		
		Entry entry = new Entry();
		entry.frequency = 1;
		entry.size = size;
		cache.put(key, entry);
		curSize += size;
		
	}
	
	private void update(String key) {
		Entry entry = cache.get(key);
		SetEntry setEntry = new SetEntry();
		setEntry.frequency = entry.frequency;
		setEntry.key = key;
		frequencySet.remove(setEntry);
		
		entry.frequency++;
		setEntry.frequency++;
		frequencySet.add(setEntry);
	}
	
	private void evict() {
		String victim = frequencySet.first().key;
		Entry entry = cache.get(victim);
		cache.remove(victim);
		frequencySet.remove(frequencySet.first());
		curSize -= entry.size;
	}
}
