package cachesimulator.app;

import cachesimulator.common.Config;
import cachesimulator.common.Helper;
import cachesimulator.common.Reader;
import cachesimulator.server.ServerSimulator;

/**
 *
 * @author hungcx
 */
public class MainApp {

	public static void main(String[] args) {
		System.out.println("Starting app...");
		Helper.loadData();

		for (String policy: Config.POLICIES) {
			for (long portion : Config.CACHE_PORTIONS) {
				System.out.println("---------------------");
				long cacheSize = Config.DATA_SIZE / 100 * portion;
				ServerSimulator simulator = new ServerSimulator(policy,
						cacheSize);
				Reader reader = new Reader(Config.LOG_FILE);
				while (true) {
					String line = reader.nextLine();
					if (line == null) {
						break;
					}
					String[] parts = line.split(Config.DELIMITER);
					String url = parts[Config.URL_POS];
					long time = Long.parseLong(parts[Config.TIME_POS]);
					simulator.handleRequest(url, time);
				}
				System.out.println(String.format("Portion = %d", portion));
				System.out.println(String.format("Log file = %s", Config.LOG_FILE));
				simulator.printStat();
				reader.close();
			}
		}
	}
}
