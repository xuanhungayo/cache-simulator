package cachesimulator.common;

/**
 *
 * @author hungcx
 */
public class Config {
	// Cache tuning
	public static String[] POLICIES = {"LRU", "LFU", "SA", "FSA"};
	public static int SAMPLE_SIZE = 20;
	public static long[] CACHE_PORTIONS = {2, 4, 6, 8, 10, 12}; 
	public static int LIMIT_BFS_DEPTH = 3;
		// SA
	public static long PIVOT_ALPHA = 2; 
		// FSA
	public static long LIMIT_PIVOT = 5;
	public static String[] FIXED_PIVOTS = {
		"/",
		"/english/index.html",
		"/french/index.html",
		"/english/teams/",
		"/french/teams/"
	};

	
	public static long DATA_SIZE = 110000000; // 110 MB
	public static String[] IMPLICIT_TAGS = {"img", "frame", "link"};
	public static String[] EXPLICIT_TAGS = {"a"};
	
	public static String LOG_FILE = "resource/day6_large";
	public static String GRAPH_FILE = "resource/graph.txt";
	
	// Web page crawler
	public static String DELIMITER = " ";
	public static int URL_POS = 3;
	public static int TIME_POS = 1;
	public static String PREFIX = "https://web.archive.org/web/20001203005500/http://www.france98.com:80";
	public static String SEARCH_KEY = "http://www.france98.com:80";
	public static int LIMIT_WAITTIME = 64;
}
