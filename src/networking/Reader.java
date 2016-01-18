package networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import application.Main;

public class Reader extends Thread implements Networking {

	private String host = null;
	private int port;
	private Main application = null;

	private Socket connection = null;
	private BufferedReader input = null;

	public Reader(String host, int port, Main application) {
		this.host = host;
		this.port = port;
		this.application = application;
	}

	@Override
	public int connect() {
		int i = 0;
		while (i < 5) {
			try {
				this.connection = new Socket(this.host, this.port);
				this.input = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
				break;
			} catch (UnknownHostException e) {
				i++;
			} catch (IOException e) {
				i++;
			}
		}
		return i;
	}

	@Override
	public void disconnect() {
		try {
			if (this.input != null)
				this.input.close();
			if (this.connection != null)
				this.connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		String command = null;
		while ((command = this.readFromInput()) != null) {
			switch (command) {
			case ":22":
				// Server ...............
				break;
			case ":21":
				int freeRoomsNumber = Integer.parseInt(this.readFromInput());
				String[] names = new String[freeRoomsNumber];
				for (int i = 0; i < freeRoomsNumber; i++) {
					names[i] = readFromInput();
				}
				this.application.createRoomsListScene(names);
				break;
			case ":50":
				this.application.createGameScene();
				break;
			case ":51":
				this.application.decrementCounter();
				break;
			case ":60":
				this.application.setScore(this.application.myScore, this.readFromInput());
				break;
			case ":61":
				this.application.setScore(this.application.oponentScore, this.readFromInput());
				break;
			default:
				break;
			}
		}
	}

	public String readFromInput() {
		try {
			String line = this.input.readLine();
			System.out.println("Reader " + line);
			return line;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
