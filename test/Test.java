
import cachesimulator.common.Config;
import cachesimulator.common.Helper;
import cachesimulator.common.Reader;
import cachesimulator.model.ObjectGraph;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author hungcx
 */
public class Test {

	public static void main(String[] args) {
		ObjectGraph graph = new ObjectGraph();
		graph.build(Config.LOG_FILE);
		graph.saveToText(Config.GRAPH_FILE);
	}
}
