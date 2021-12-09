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
import java.nio.charset.StandardCharsets;

/**
 * This is the interface of the server, we don't want our master and worker nodes to be exposed.
 *
 */
public class ServerInterface {

	private static String masterIp = "10.10.1.1";
	private static int masterPort = 1203;

	private static final int _PORT = 1202;

	public ServerInterface() {

	}

	public static void main(String [] args) throws IOException{
		final Server server = new Server(_PORT);
		Client client = new Client(masterIp, masterPort);
		while(true){
			server.accept();
			System.out.println("connect to front end");
			String request = null;

			while ((request = server.receive())!= null) {
				final String finalRequest = request;
				final Client finalClient = client;
				if (finalRequest.startsWith("GET")) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								String[] s = finalRequest.split(" ");
								String type = s[1].substring(1);
//								String[] split = type2.split("|");
//								String type = split[1];

								System.out.println("forward " + type);
//
//								if (type.equals("remove")) {
//									finalClient.send("remove");
//									String receive = finalClient.receive();
//									server.response("HTTP/1.1 200 OK\r\n\r\n" + receive);
//									return;
//								}

								long start = System.currentTimeMillis();
								finalClient.send(type);
								String receive = finalClient.receive();
//								finalClient.close();

								long end = System.currentTimeMillis();
								long time = end - start;
								System.out.println(time);
								System.out.println("Receive: " + receive + time);

								String response_str = receive + " " + time + " ms";
								server.response("HTTP/1.1 200 OK\r\n"
										+ "Access-Control-Allow-Origin:*\r\n"
										+ "Access-Control-Allow-Headers:*\r\n"
										+ "Content-Type: text/html; charset=utf-8\r\n"
										+ "Content-Length:" + response_str.getBytes().length + "\r\n\r\n"
										+ response_str);
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
