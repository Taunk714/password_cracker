import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

/**
 * This is the interface of the server, we don't want our master and worker nodes to be exposed.
 *
 */
public class ServerInterface {

	private static String masterIp = "127.0.0.1";
	private static int masterPort = 1203;

	private static final int _PORT = 1202;

	public ServerInterface() {

	}

	public static void main(String [] args) throws IOException{
		Server server = new Server(_PORT);
		server.accept();
		System.out.println("connect to front end");
		String request = null;
		while ((request = server.receive())!= null) {
			if (request.startsWith("GET")){
				String[] s = request.split(" ");
				String md5 = s[1].substring(1);
				System.out.println("md5:"+md5);
				Client client = new Client(masterIp, masterPort);
				long start = System.currentTimeMillis();
				client.send("md5 "+ md5);
				String receive = client.receive();
				long end = System.currentTimeMillis();
				long time = end - start;
				System.out.println(time);
				System.out.println("receive:"+receive);
				server.response(receive);
			}
		}
	}
}
