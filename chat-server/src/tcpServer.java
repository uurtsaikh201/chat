// tcpServer.java by fpont 3/2000

// usage : java tcpServer <port number>.
// default port is 1500.
// connection to be closed by client.
// this server handles only 1 connection.

import java.net.*;
import java.io.*;
import java.lang.*;

public class tcpServer {

	public static void main(String args[]) {

		int port;
		ServerSocket server_socket;
		BufferedReader input;
		BufferedWriter output;
		try {
			port = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("port = 1500 (default)");
			port = 1500;
		}

		try {

			server_socket = new ServerSocket(port);
			System.out.println("Server waiting for client on port " + server_socket.getLocalPort());

			// server infinite loop
			while (true) {
				Socket socket = server_socket.accept();
				System.out.println("New connection accepted " + socket.getInetAddress() + ":" + socket.getPort());

				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				// print received data
				try {

					while (true) {
						
						char[] buf = new char[250];

						input.read(buf, 0, 250);
						
						if (buf == null) {
							return;
						}
						int count=0;
						String b = "";
						for(int i=0;i<250;i++){
							if(buf[i]!='\u0000'){
								count++;
								b+=buf[i];
							}
						}
						System.out.println(buf);
						
						System.out.println(count+ " "+b + " " +b.length());
						// input.close();

						// output.flush();
						// for(int i=0;i<15;i++)
						{
							// Flushing the output after the write will
							// immediatly intimate the client
							output.write(buf, 0, count);
							output.flush();
						}
						// output.write(buf);
						// output.flush();

					}
				} catch (Exception e) {
					System.out.println(e);
				}

				// connection closed by client
				

			}

		}

		catch (IOException e) {
			System.out.println(e);
		}
	}
}
