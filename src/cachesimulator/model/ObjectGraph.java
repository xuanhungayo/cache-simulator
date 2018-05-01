package cachesimulator.model;

import cachesimulator.cache.SACache;
import cachesimulator.common.Config;
import cachesimulator.common.Helper;
import cachesimulator.common.Reader;
import cachesimulator.common.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author hungcx
 */
public class ObjectGraph {

	public static class Neighbor {

		public String url;
		public int w;

		public Neighbor(String url, int w) {
			this.url = url;
			this.w = w;
		}
	}

	private Map<String, List<Neighbor>> edges = new HashMap<>();

	public ObjectGraph() {
	}

	public void load(String graphFile) {
		System.out.println("Loading graph file...");
		Reader reader = new Reader(graphFile);
		while (true) {
			String url = reader.nextLine();
			if (url == null) {
				break;
			}
			int numOfEdge = Integer.parseInt(reader.nextLine());

			// Create list of neighbors
			List<Neighbor> neighbors = new ArrayList<>();
			for (int i = 0; i < numOfEdge; i++) {
				String[] parts = reader.nextLine().split(Config.DELIMITER);
				Neighbor neighbor = new Neighbor(parts[0], Integer.parseInt(parts[1]));
				neighbors.add(neighbor);
			}

			// Add edges
			edges.put(url, neighbors);
		}
		reader.close();
	}

	public void build(String logFile) {
		System.out.println("Build graph of objects from web archive..");
		int counter = 0;

		Set<String> urlSet = new HashSet<>();
		Reader reader = new Reader(logFile);
		while (true) {
			String line = reader.nextLine();
			if (line == null) {
				break;
			}
			String[] parts = line.split(" ");
			String url = parts[3];
			counter++;
			System.out.println(String.format("Current counter = %d", counter));
			if (!Helper.isHTML(url) || urlSet.contains(url)) {
				continue;
			}
			urlSet.add(url);
			System.out.println(url);
			List<Neighbor> edgeList = parse(url);
			edges.put(url, edgeList);
		}
		reader.close();
	}

	public void saveToText(String edgesFile) {
		Writer writer = new Writer(edgesFile);
		for (Map.Entry<String, List<Neighbor>> entry : edges.entrySet()) {
			writer.writeln(entry.getKey());
			writer.writeln(Integer.toString(entry.getValue().size()));
			for (Neighbor next : entry.getValue()) {
				writer.writeln(String.format("%s %d", next.url, next.w));
			}
		}
		writer.close();
	}

	public List<Neighbor> parse(String url) {
		Set<String> linkSet = new HashSet<>();
		List<Neighbor> result = new ArrayList<>();
		int waitTime = 0;
		while (true) {
			try {
				if (waitTime > Config.LIMIT_WAITTIME) {
					break;
				}
				Thread.sleep(1000 * waitTime);
				Document doc = Jsoup.connect(Config.PREFIX + url).get();

				addLink(doc, result, linkSet, Config.IMPLICIT_TAGS, 0);
				addLink(doc, result, linkSet, Config.EXPLICIT_TAGS, 1);

				break;
			} catch (HttpStatusException ex) {
				System.err.println(String.format("HTTP status = %d", ex.getStatusCode()));
				break;
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
				if (waitTime == 0) {
					waitTime = 16;
				} else {
					waitTime *= 2;
				}
			}
		}

		return result;
	}

	private void addLink(Document doc, List<Neighbor> result,
			Set<String> linkSet, String[] tags, int w) {
		for (String tag : tags) {
			Elements elements = doc.getElementsByTag(tag);
			for (Element ele : elements) {
				String link = ele.attr("href");
				if (link.isEmpty()) {
					link = ele.attr("src");
				}
				if (link.isEmpty()) {
					continue;
				}
				if (!link.contains(Config.SEARCH_KEY)) {
					continue;
				}
				link = Helper.trim(link);
				if (linkSet.contains(link)) {
					continue;
				}
				linkSet.add(link);
				Neighbor neighbor = new Neighbor(link, w);
				result.add(neighbor);
			}
		}
	}

	public Map<String, Integer> getDistance(List<String> pivots,
			List<String> samples) {
		Map<String, Integer> result = new HashMap<>();
		for (String sample : samples) {
			result.put(sample, Integer.MAX_VALUE);
		}

		Queue<String> queue = new LinkedList<>();
		Map<String, Integer> dist = new HashMap<>();
		for (String pivot : pivots) {
			queue.add(pivot);
			dist.put(pivot, 0);
		}
		
		while (!queue.isEmpty()) {
			String url = queue.poll();
			int curDist = dist.get(url);
			if (curDist > Config.LIMIT_BFS_DEPTH)
				break;
			if (result.containsKey(url)) {
				// System.out.println(curDist);
				result.put(url, curDist);
			}
			if (!edges.containsKey(url)) {
				continue;
			}
			for (Neighbor neighbor : edges.get(url)) {
				String next = neighbor.url;
				int w = neighbor.w;
				if (dist.containsKey(next)) {
					continue;
				}
				if (w == 0) { // Implicit tag
					dist.put(next, curDist);
				} else {
					dist.put(next, curDist + 1);
					queue.add(next);
				}
			}
		}
		
		return result;
	}
}
