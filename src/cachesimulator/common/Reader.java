package cachesimulator.common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author hungcx
 */
public class Reader {

	InputStream is;
	InputStreamReader isreader;
	private BufferedReader reader;
	private String line;

	public Reader(String filePath) {
		try {
			is = new FileInputStream(filePath);
			isreader = new InputStreamReader(is);
			reader = new BufferedReader(isreader);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	public String nextLine() {
		try {
			return reader.readLine();
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void close() {
		try {
			reader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
