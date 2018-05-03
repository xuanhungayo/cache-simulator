package cachesimulator.common;

import cachesimulator.model.ObjectGraph;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author hungcx
 */
public class Helper {

	private static Map<String, Long> objectSize = new HashMap<>();
	private static Map<String, Integer> numOfExplicitLink = new HashMap<>();
	private static long totalSize = 0;
	
	public static void loadData() {
		System.out.println("Loading data...");
		loadObjectSize();
		loadNumOfExplicitLink();
	}

	private static void loadObjectSize() {
		try {
			Scanner scanner = new Scanner(new File(Config.LOG_FILE));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] parts = line.split(" ");
				String key = parts[3];
				if (objectSize.containsKey(key) && objectSize.get(key) > 0) {
					continue;
				}
				if (!parts[4].equals("200") || parts[5].equals("-")) {
					continue;
				}
				Long size = Long.parseLong(parts[5]);
				objectSize.put(key, size);
			}
			scanner.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}

		for (long size : objectSize.values()) {
			totalSize += size;
		}
//		System.out.println(totalSize);
	//	System.out.println(objectMap.size());
	}
	
	private static void loadNumOfExplicitLink() {
		Reader reader = new Reader(Config.GRAPH_FILE);
		while (true) {
			String url = reader.nextLine();
			if (url == null) {
				break;
			}
			int numOfEdge = Integer.parseInt(reader.nextLine());
			int explicitTag = 0;
			for (int i = 0; i < numOfEdge; i++) {
				String[] parts = reader.nextLine().split(Config.DELIMITER);
				if (parts[1].equals("1")) {
					explicitTag++;
				}
			}

			numOfExplicitLink.put(url, explicitTag);
		}
		reader.close();
	}
	

	public static long getSize(String key) {
		if (objectSize.containsKey(key)) {
			return objectSize.get(key);
		} else {
			return 0;
		}
	}
	
	public static int getNumOfExplicitLink(String url) {
		if (numOfExplicitLink.containsKey(url)) {
			return numOfExplicitLink.get(url);
		} else {
			return 0;
		}
	}

	public static boolean isHTML(String url) {
		return url.endsWith(".htm") || url.endsWith(".html");
	}

	public static String trim(String url) {
		int lastIndex = url.indexOf(Config.SEARCH_KEY) + Config.SEARCH_KEY.length();
		return url.substring(lastIndex);
	}
}
