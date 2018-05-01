package cachesimulator.cache;

import cachesimulator.common.Config;
import cachesimulator.common.Helper;
import cachesimulator.model.CacheStat;
import cachesimulator.model.ObjectGraph;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author hungcx
 */
public class SACache extends Cache {

	private static class Entry {

		public long size;
		public long timestamp;
		public long frequency;

		public Entry(long sz, long ts, long freq) {
			size = sz;
			timestamp = ts;
			frequency = freq;
		}
	}

	private static class Pivot implements Comparable<Pivot> {

		public String key;
		public long timestamp;

		public Pivot(String k, long ts) {
			key = k;
			timestamp = ts;
		}

		@Override
		public int compareTo(Pivot p) {
			if (this.timestamp < p.timestamp) {
				return -1;
			}
			if (this.timestamp > p.timestamp) {
				return 1;
			}

			return key.compareTo(p.key);
		}
	}

	private static class Victim implements Comparable<Victim> {

		public String key;
		public long size;
		public long frequency;
		public int distance;

		public Victim() {
		}

		@Override
		public int compareTo(Victim v) {
			if (this.distance > v.distance) {
				return -1;
			}
			if (this.distance < v.distance) {
				return 1;
			}
			
			if (this.frequency < v.frequency) {
				return -1;
			}
			if (this.frequency > v.frequency) {
				return 1;
			}

			return key.compareTo(v.key);
		}
	}

	private ObjectGraph graph = new ObjectGraph();
	private Map<String, Entry> cache = new HashMap<>();
	private TreeSet<Pivot> pivots = new TreeSet<>();

	public SACache(long cap, CacheStat stat) {
		super(cap, stat);
		graph.load(Config.GRAPH_FILE);
	}

	@Override
	public long get(String key, long newTime) {
//		System.out.println(String.format("GET %s", key));
		
		if (cache.containsKey(key)) {
			Entry entry = cache.get(key);

			// Update entry and pivot
			if (Helper.isHTML(key)) {
				updatePivot(key, entry, newTime);
			}
			updateEntry(entry, newTime);

			stat.hit(entry.size);
			return entry.size;
		}
		stat.miss(Helper.getSize(key));
		return -1;
	}

	@Override
	public void put(String key, long size, long newTime) {
//		System.out.println(String.format("PUT %s %d", key, size));
		
		if (size > capacity) {
			return;
		}
		// Currently we never run into this case
//		if (cache.containsKey(key)) {
//			Entry entry = cache.get(key);
//			
//			// Update entry and pivot
//			if (Helper.isHTML(key)) {
//				long newTime = new Date().getTime();
//				updatePivot(key, entry, newTime);
//				updateEntry(entry, newTime);
//			}
//			
//			return;
//		}
		while (curSize + size > capacity) {
			evict(newTime);
		}

		Entry entry = new Entry(size, newTime, 1);
		cache.put(key, entry);
		curSize += size;
		if (Helper.isHTML(key)) {
			addPivot(key, entry);
		}

	}

	private void updateEntry(Entry entry, long time) {
		entry.frequency++;
		entry.timestamp = time;
	}

	private void evict(long time) {
		adjustPivots(time);

		List<String> randomSamples = pickRandomSamples(cache.keySet(),
				Config.SAMPLE_SIZE);
		
		List<String> pivotKeys = new LinkedList<>();
		for (Pivot pivot : pivots) {
			pivotKeys.add(pivot.key);
		}
		Map<String, Integer> distance = graph.getDistance(pivotKeys, randomSamples);

		TreeSet<Victim> victims = new TreeSet<>();
		for (Map.Entry<String, Integer> entry : distance.entrySet()) {
			Victim victim = new Victim();
			victim.key = entry.getKey();
			victim.distance = entry.getValue();
			victim.size = cache.get(victim.key).size;
			victim.frequency = cache.get(victim.key).frequency;
			victims.add(victim);
		}

		String key = victims.first().key;
		Entry entry = cache.get(key);
		removePivot(key, entry);
		cache.remove(key);
		curSize -= entry.size;
	}

	private List<String> pickRandomSamples(Set<String> keySet, int k) {
		List<String> list = new LinkedList<String>(keySet);
		Collections.shuffle(list);
		return list.subList(0, Math.min(k, list.size()));
	}
	
	// Pivot selection
	private void adjustPivots(long time) {
		while (!pivots.isEmpty()) {
			Pivot pivot = pivots.first();
			if (time - pivot.timestamp >= Config.PIVOT_ALPHA) {
				pivots.remove(pivot);
			} else {
				break;
			}
		}
		// System.out.println(pivots.size());
	}

	private void addPivot(String key, Entry entry) {
		Pivot pivot = new Pivot(key, entry.timestamp);
		pivots.add(pivot);
	}

	private void updatePivot(String key, Entry entry, long newTime) {
		Pivot pivot = new Pivot(key, entry.timestamp);
		boolean removed = pivots.remove(pivot);
		if (removed) {
			pivot.timestamp = newTime;
			pivots.add(pivot);
		}
	}

	private void removePivot(String key, Entry entry) {
		Pivot pivot = new Pivot(key, entry.timestamp);
		pivots.remove(pivot);
	}
}
