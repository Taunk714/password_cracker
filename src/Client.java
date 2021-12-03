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
		s.close();
		pr.close();
		br.close();
	}

//	// simple echo client. Call this method in
//	public static void simpleEchoClient() throws IOException {
//		Client c = new Client();
//		try {
//			BufferedReader userBw = new BufferedReader( new InputStreamReader(System.in));
//			String host = userBw.readLine();
//			int port = Integer.parseInt(userBw.readLine());
//			c.connect(host, port);
//			String userInput;
//			while((userInput = userBw.readLine())!= null){
//				c.send(userInput);
//				c.receive();
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			c.close();
//		}
//	}
//
//	/**
//	 * Core function.
//	 * @param type measurement type, rtt or tput
//	 * @param num number of probes
//	 * @param size measurement size
//	 * @param delay server delay
//	 * @return array that stores the result of each size.
//	 * @throws IOException
//	 */
//	public static double[] measure(String type, int num, int[] size, int delay) throws IOException {
//		Client c = new Client();
//		double[] ret = new double[size.length];
//		for (int j = 0; j < size.length; j++) {
//			try {
//				// connect
//				c.connect(host, port);
//				String[] messages = generateMessage(num, size[j]);
//
//				// CSP
//				c.setUp(type, num, size[j], delay);
//
//				double time = 0;
//				for (int i = 0; i < messages.length; i++) {
//					// measure
//					long rtt = c.measure(i+1, messages[i]);
//					if (type.equals("rtt")){
//						time += rtt;
//					}else if(type.equals("tput")){
//						time += 1.0 * size[j]/(rtt/1000.0);
//					}
//				}
//
//				// CTP
//				c.endMeasure();
//
//				// Calculate average
//				time = time / messages.length;
//				if (type.equals("rtt")){
//					ret[j] = time;
//				}else if(type.equals("tput")){
//					ret[j] = time;
//				}
//
//				// show the time
//				System.out.println(time);
//			} catch (ConnectException e){
//				System.out.println("Connection refused! Check your host and port and test again!");
//				return null;
//			} catch (IOException e) {
//				System.out.println(e.getMessage());
//				return null;
//			}finally {
//				c.close();
//			}
//		}
//		return ret;
//	}
//
//	public static void main(String [] args) throws IOException {
//		// User can enter host and port
//		Scanner sc = new Scanner(System.in);
//		System.out.print("host:");
//		host = sc.nextLine();
//
//		// User must enter a correct format port
//		while(true){
//			try{
//				System.out.print("port:");
//				port = sc.nextInt();
//				break;
//			} catch (Exception e) {
//				System.out.println("Please enter an integer");
//			}
//		}
//
//		// if the error occurs, the program will use the default delay.
//		int delay = 0;
//		try{
//			System.out.print("server delay:");
//			delay = sc.nextInt();
//		} catch (Exception e) {
//			System.out.println("Input Error! Use default server delay: 0");
//		}
//
//		// you can choose parameter here. The four parameters are measurement type, number of probes, size of each experiment group, server delay
////        System.out.println(Arrays.toString(measure("rtt", 100, new int[]{1, 100, 200, 400, 800, 1000}, delay)));
////        System.out.println(Arrays.toString(measure("tput", 10, new int[]{1000000, 2000000, 4000000, 8000000, 16000000, 32000000},delay)));
//	}
//
//	public static String[] generateMessage(int number, int size){
//		String[] arr = new String[number];
//		Arrays.fill(arr, "a".repeat(size));
//		return arr;
//	}
}
