import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
	/**
	 * How to run this file:
	 * 1. set the host and port parameters.
	 * 2. run the main function. In this case, you can enter server delayvia command line.
	 * If you want to change the size or type or number of probes, change the corresponding parameters in main function.
	 *
	 * The final output is an array the stores corresponding data(rtt or tput)
	 */


	private Socket s;
	private PrintWriter pr;
	private BufferedReader br;


	public Client(String host, int port) throws IOException {
		connect(host, port);
	}

	public void connect(String host, int port) throws IOException {
		s = new Socket(host, port);
		pr = new PrintWriter(s.getOutputStream());
		br = new BufferedReader(new InputStreamReader(s.getInputStream()));
	}
//
//	// CSP. Send set up message and get response, delay must be 0
//	public void setUp(String type, long numOfProbe, long measureSize) throws IOException {
//		setUp(type, numOfProbe, measureSize, 0);
//	}
//
//	// CSP. Send set up message and get response
//	public void setUp(String type, long numOfProbe, long measureSize, int delay) throws IOException {
//		System.out.println("=========CSP start===========");
//		String s = MeasureInfo.generateCSP(type, numOfProbe, measureSize, delay);
//		send(s);
//		receive();
//		System.out.println("=========CSP end===========");
//	}
//
//	// MP. Send measurement message
//	public long measure(long id, String s) throws IOException {
//		System.out.println("=========Measure group "+id+"===========");
//		long start = System.currentTimeMillis();
//		send(MeasureInfo.generateMP(id, s));
//		receive();
//		long end = System.currentTimeMillis();
//		return end - start;
//	}
//
//	// CTP. send end message and get response.
//	public void endMeasure() throws IOException {
//		System.out.println("=========CTP start===========");
//		send(MeasureInfo.generateCTP());
//		receive();
//		System.out.println("=========CTP end===========");
//	}

	// Send message
	public void send(String message) throws IOException {
		pr.println(message);
		pr.flush();
	}

	// Get response and print out
	public String receive() throws IOException {
		String s = receiveWithoutPrint();
		System.out.println(s);
		return s;
	}

	// Get response
	public String receiveWithoutPrint() throws IOException {
		return br.readLine();
	}

	// close connection
	public void close() throws IOException {
		System.out.println("client close");
		s.close();
		pr.close();
		br.close();
	}

}
