package cachesimulator.cache;

import cachesimulator.common.Config;
import cachesimulator.common.Helper;
import cachesimulator.model.CacheStat;
import cachesimulator.model.DoubleLinkedList;
import cachesimulator.model.ObjectGraph;
import java.util.Arrays;
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
public class FSACache extends Cache {

	public static class Entry {

		public long size;
		public long timestamp;
		public long frequency;
		public DoubleLinkedList.Node node;

		public Entry(long sz, long ts, long freq) {
			size = sz;
			timestamp = ts;
			frequency = freq;
		}
	}

	public static class Pivot implements Comparable<Pivot> {

		public String key;
		public long timestamp;
		public int explicitLink;
		public long frequency;

		private double pivotValue() {
			return explicitLink * frequency;
		}

		public Pivot() {
		}

		@Override
		public int compareTo(Pivot p) {
			if (this.pivotValue() < p.pivotValue()) {
				return -1;
			}
			if (this.pivotValue() > p.pivotValue()) {
				return 1;
			}

			return key.compareTo(p.key);
		}
	}

	public static class Victim implements Comparable<Victim> {

		public String key;
		public long size;
		public long frequency;
		public int distance;

		public Victim() {
		}

		private double cacheValue() {
//			if (size == 0) {
//				return -1;
//			}
			return (double) frequency / (100 + size) ;
		}

		@Override
		public int compareTo(Victim v) {
			if (this.distance > v.distance) {
				return -1;
			}
			if (this.distance < v.distance) {
				return 1;
			}

			if (this.cacheValue() < v.cacheValue()) {
				return -1;
			}
			if (this.cacheValue() > v.cacheValue()) {
				return 1;
			}

			return key.compareTo(v.key);
		}
	}

	private ObjectGraph graph = new ObjectGraph();
	private Map<String, Entry> cache = new HashMap<>();
	private DoubleLinkedList lruList = new DoubleLinkedList();
	private TreeSet<Pivot> pivots = new TreeSet<>();
	private List<String> fixedPivots = Arrays.asList(Config.FIXED_PIVOTS);

	public FSACache(long cap, CacheStat stat) {
		super(cap, stat);
		graph.load(Config.GRAPH_FILE);
	}

	@Override
	public long get(String key, long newTime) {
		if (cache.containsKey(key)) {
			refresh(key);
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
		if (size > capacity) {
			return;
		}

		while (curSize + size > capacity) {
			evict(newTime);
		}

		lruList.pushFront(key);
		Entry entry = new Entry(size, newTime, 1);
		entry.node = lruList.begin();
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
		adjustPivots(time); // Pivot selection here
		// System.out.println(pivots.size());

		// Pick samples and measure distance
//		List<String> samples = pickLRUSamples(Config.SAMPLE_SIZE);
		List<String> samples = pickRandomSamples(cache.keySet(), 
				Config.SAMPLE_SIZE);
		List<String> pivotKeys = new LinkedList<>(fixedPivots);
		for (Pivot pivot : pivots) {
			pivotKeys.add(pivot.key);
		}
		Map<String, Integer> distance = graph.getDistance(pivotKeys, samples);

		TreeSet<Victim> victims = new TreeSet<>();
		for (Map.Entry<String, Integer> entry : distance.entrySet()) {
			Victim victim = new Victim();
			victim.key = entry.getKey();
			victim.distance = entry.getValue();
			Entry e = cache.get(victim.key);
			victim.size = e.size;
			victim.frequency = e.frequency;
			victims.add(victim);
		}

		// Choose the victim and evict
		String victim = victims.first().key;
		Entry victimEntry = cache.get(victim);
		cache.remove(victim);
		curSize -= victimEntry.size;

		removePivot(victim, victimEntry);
		/* We add back the not-victim samles 
		since we remove them from the lru list */
		Collections.reverse(samples);
		for (String key : samples) {
			if (!key.equals(victim)) {
				lruList.pushBack(key);
				Entry entry = cache.get(key);
				entry.node = lruList.end();
			}
		}
	}

	private List<String> pickLRUSamples(int k) {
		List<String> list = new LinkedList<String>();
		for (int i = 0; i < k; i++) {
			if (lruList.empty()) {
				break;
			}
			list.add(lruList.back());
			lruList.popBack();
		}
		return list;
	}
	
	private List<String> pickRandomSamples(Set<String> keySet, int k) {
		List<String> list = new LinkedList<String>(keySet);
		Collections.shuffle(list);
		return list.subList(0, Math.min(k, list.size()));
	}

	// Pivot selection
	private void adjustPivots(long time) {
		TreeSet<Pivot> newPivots = new TreeSet();
		while (pivots.size() > 0 && newPivots.size() < Config.LIMIT_PIVOT) {
			Pivot pivot = pivots.last();
			pivots.remove(pivot);
			if (time - pivot.timestamp < Config.PIVOT_ALPHA) {
				newPivots.add(pivot);
			}
		}
		pivots = newPivots;
		// System.out.println(pivots.size());
	}

	private void addPivot(String key, Entry entry) {
		Pivot pivot = new Pivot();
		pivot.key = key;
		pivot.timestamp = entry.timestamp;
		pivot.frequency = entry.frequency;
		pivot.explicitLink = Helper.getNumOfExplicitLink(key);

		pivots.add(pivot);
	}

	private void updatePivot(String key, Entry entry, long newTime) {
		Pivot pivot = new Pivot();
		pivot.key = key;
		pivot.timestamp = entry.timestamp;
		pivot.frequency = entry.frequency;
		pivot.explicitLink = Helper.getNumOfExplicitLink(key);

		boolean removed = pivots.remove(pivot);
		if (removed) {
			pivot.timestamp = newTime;
			pivots.add(pivot);
		}
	}

	private void removePivot(String key, Entry entry) {
		Pivot pivot = new Pivot();
		pivot.key = key;
		pivot.timestamp = entry.timestamp;
		pivot.frequency = entry.frequency;
		pivot.explicitLink = Helper.getNumOfExplicitLink(key);

		pivots.remove(pivot);
	}

	private void refresh(String key) {
		FSACache.Entry entry = cache.get(key);
		lruList.remove(entry.node);
		lruList.pushFront(key);
		entry.node = lruList.begin();
	}
}
