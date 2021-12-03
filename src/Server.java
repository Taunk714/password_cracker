import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {


	private ServerSocket s;
	private Socket client;
	private PrintWriter pr;
	private BufferedReader br;


	public Server(int port) throws IOException {
		bind(port);
	}

	public void bind(int port) throws IOException {
		s = new ServerSocket(port);
	}

	public void accept() throws IOException {
		client = s.accept();
		br = new BufferedReader(new InputStreamReader(client.getInputStream()));
		pr = new PrintWriter(client.getOutputStream());
	}

	// response and print the message
	public void response(String s){
		responseWithoutPrint(s);
		System.out.println("Response: " + s);
	}

	// response
	public void responseWithoutPrint(String s){
		pr.println(s);
		pr.flush();
	}

	public String receive() throws IOException {
		try{
			return br.readLine();
		} catch (IOException e) {
			return null;
		}
	}

	// close connection, socket still work.
	public void closeConnection() throws IOException {
		client.close();
		pr.close();
		br.close();
	}

	// close socket.
	public void close() throws IOException {
		client.close();
		pr.close();
		br.close();
		s.close();
	}

//	/**
//	 * measurement test. when one connection closed, it still listen the port, and can connect with new client.
//	 * @throws IOException
//	 */
//	public static void measure() throws IOException {
//		Server server = new Server();
//
//		try{
//			server.bind(_PORT);
//			while (true) {
//				server.accept();
//				System.out.println("A client joined");
//				String request;
//				MeasureInfo measureInfo = new MeasureInfo(server);
//				while ((request = server.receive())!= null) {
//					measureInfo.parseRequest(request);
//				}
//				System.out.println("The client disconnected");
//			}
//		} catch (IOException | InterruptedException e){
//			e.printStackTrace();
//		} finally {
//			server.close();
//		}
//	}
//
//	public static void echo() throws IOException {
//		Server server = new Server();
//
//		try{
//			server.bind(_PORT);
//			while (true) {
//				server.accept();
//				System.out.println("A client joined");
//				String request;
////				MeasureInfo measureInfo = new MeasureInfo(server);
//				while ((request = server.receive())!= null) {
//					server.response(request);
//				}
//				System.out.println("The client disconnected");
//			}
//		} catch (IOException e){
//			e.printStackTrace();
//		} finally {
//			server.close();
//		}
//	}

//	public static void main(String [] args) throws IOException{
//		measure();
//	}
}
