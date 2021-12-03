package intf;

import com.sun.net.httpserver.HttpServer;
import utils.Client;
import utils.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
		while(true){
			server.accept();
			System.out.println("connect to front end");
			String request = null;
			Client client = null;
			client = new Client(masterIp, masterPort);
			while ((request = server.receive())!= null) {
				String finalRequest = request;
				Client finalClient = client;
				if (finalRequest.startsWith("GET")) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								String[] s = finalRequest.split(" ");
								String type = s[1].substring(1);
								System.out.println("md5:" + type);

								if (type.equals("remove")) {
									finalClient.send("remove");
									String receive = finalClient.receive();
									server.response("HTTP/1.1 200 OK\r\n\r\n" + receive);
									return;
								}

								long start = System.currentTimeMillis();
								finalClient.send("md5 " + type);
								String receive = finalClient.receive();

								long end = System.currentTimeMillis();
								long time = end - start;
								System.out.println(time);
								System.out.println("receive:" + receive);

								server.response("HTTP/1.1 200 OK\r\n\r\n" + receive);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}).start();
				}

			}
		}

	}
}