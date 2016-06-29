
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ChatServer {

	public static Map<String, Socket> clientSocketMap = null;
	public static Map<String, List<String>> messageMap = new HashMap<String, List<String>>();
	public static Map<String, String> chatRoomUserMap = new HashMap<String, String>();
	ServerSocket socketServer = null;
	ClientThread clientThread = null;

	public static void main(String[] args) {

		ChatServer chatServer = new ChatServer();

	}

	public ChatServer() {
		try {
			Timer time = new Timer(); // Instantiate Timer Object
			ScheduledTask st = new ScheduledTask(); // Instantiate SheduledTask
													// class
			time.schedule(st, 0, 1000); // Cr
			socketServer = new ServerSocket(4343);
			clientSocketMap = new HashMap<String, Socket>();
			while (true) {
				Socket sockets = socketServer.accept();
				ClientThread clientThread = new ClientThread(sockets);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class ClientThread extends Thread {

		BufferedReader dis = null;
		BufferedWriter dos = null;

		public ClientThread(Socket client) {
			try {
				if (client != null && !client.isClosed()) {

					dis = new BufferedReader(new InputStreamReader(client.getInputStream()));
					dos = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
					char[] buf = new char[250];

					dis.read(buf, 0, 250);

					if (buf == null) {
						return;
					}
					int count = 0;
					String loginName = "";
					for (int i = 0; i < 250; i++) {
						if (buf[i] != '\u0000') {
							count++;
							loginName += buf[i];
						}
					}

					System.out.println("login name:" + loginName);
					clientSocketMap.put(loginName.trim(), client);

					start();
					sendUserNamesToClients();
				}
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

		public void run() {
			while (true) {
				try {
					char[] buf = new char[250];

					dis.read(buf, 0, 250);

					if (buf == null) {
						return;
					}
					int count = 0;
					String b = "";
					for (int i = 0; i < 250; i++) {
						if (buf[i] != '\u0000') {
							count++;
							b += buf[i];
						}
					}
					System.out.println(buf);
					System.out.println(count + " " + b + " " + b.length());

					if (b.startsWith("changeroom:")) {
						b = b.replace("changeroom:", "");
						String[] chatRoom = b.split("-");
						chatRoomUserMap.put(chatRoom[0], chatRoom[1]);
						System.out.println("change chat room:" + chatRoom[0] + " " + chatRoom[1]);
					} else {
						String[] messages = b.split("-");
						String loginUser = messages[0];
						String chatRoomNumber = messages[1];
						String textChat = messages[2];
						System.out.println("sendUser:" + chatRoomNumber);
						chatRoomNumber = chatRoomNumber.trim();
						List<String> messageList;
						if (messageMap.containsKey(chatRoomNumber)) {
							messageList = messageMap.get(chatRoomNumber);
							messageList.add("message:" + loginUser + " said:" + textChat);
						} else {
							messageList = new ArrayList<String>();
							messageList.add("message:" + loginUser + " said:" + textChat);
						}
						messageMap.put(chatRoomNumber, messageList);
					}
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}

			}
		}
	};

	public static boolean sendMessageToServer(String message, OutputStream dos) {
		try {
			byte[] toSendBytes = message.getBytes();
			int toSendLen = toSendBytes.length;
			byte[] toSendLenBytes = new byte[4];
			toSendLenBytes[0] = (byte) (toSendLen & 0xff);
			toSendLenBytes[1] = (byte) ((toSendLen >> 8) & 0xff);
			toSendLenBytes[2] = (byte) ((toSendLen >> 16) & 0xff);
			toSendLenBytes[3] = (byte) ((toSendLen >> 24) & 0xff);
			dos.write(toSendLenBytes);
			dos.write(toSendBytes);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	public class ScheduledTask extends TimerTask {
		Date now;

		public void run() {
			sendUserNamesToClients();
		}
	}

	public static void sendUserNamesToClients() {
		try {
			if (clientSocketMap != null && chatRoomUserMap != null && messageMap != null) {
				System.out.println("clientSocketMap:" + clientSocketMap.keySet());
				System.out.println("chatRoomUserMap:" + chatRoomUserMap.keySet() + " " + chatRoomUserMap.values());
				System.out.println("messageMap:" + messageMap.keySet() + " " + messageMap.values());
			}
			if (clientSocketMap != null) {
				for (String key : clientSocketMap.keySet()) {
					try {
						Socket clients = clientSocketMap.get(key);
						BufferedWriter bos = new BufferedWriter(new OutputStreamWriter(clients.getOutputStream()));

						if (chatRoomUserMap.containsKey(key)) {
							if (messageMap.containsKey(chatRoomUserMap.get(key))) {
								String messageSend = "";
								List<String> chatList = messageMap.get(chatRoomUserMap.get(key));
								for (int i = 0; i < chatList.size(); i++) {
									messageSend = messageSend + chatList.get(i) + ",";
								}
								System.out.println("key assigned message:" + key + "----------------" + messageSend);
								bos.write(messageSend, 0, messageSend.length());
								bos.flush();
							}
						}
					} catch (Exception e) {
						System.out.println("removing...");
						clientSocketMap.remove(key);
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if (child instanceof org.w3c.dom.CharacterData) {
			org.w3c.dom.CharacterData cd = (org.w3c.dom.CharacterData) child;
			return cd.getData();
		}
		return "";
	}
}
