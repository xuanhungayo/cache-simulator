package cachesimulator.common;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 *
 * @author hungcx
 */
public class Writer {
	private OutputStream os;
	private OutputStreamWriter oswriter;
	
	public Writer(String filePath) {
		try {
			os = new FileOutputStream(filePath);
			oswriter = new OutputStreamWriter(os);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
	}
	
	public void write(String s) {
		try {
			oswriter.write(s);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void writeln(String s) {
		try {
			oswriter.write(s);
			oswriter.write("\n");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void close() {
		try {
			oswriter.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
