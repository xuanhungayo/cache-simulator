package cachesimulator.model;

/**
 *
 * @author hungcx
 */
public class CacheStat {

	private long totalRequest = 0;
	private long hit = 0;
	private long totalByte = 0;
	private long hitByte = 0;
	private String cacheName;

	public CacheStat(String name) {
		cacheName = name;
	}

	public void hit(long byteSize) {
		hit++;
		totalRequest++;
		hitByte += byteSize;
		totalByte += byteSize;
	}

	public void miss(long byteSize) {
		totalRequest++;
		totalByte += byteSize;
	}
	
	public void printHitRate() {
		double hitRate = (double) hit / totalRequest;
		System.out.println(String.format("%s: Hit rate = %.2f", cacheName, hitRate));
	}

	public void printByteRate() {
		double byteRate = (double) hitByte / totalByte;
		System.out.println(String.format("%s: Byte rate = %.2f", cacheName, byteRate));
	}
	
	public void printOtherStat() {
	}
}
