package networking;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import application.Main;

public class Writer implements Networking {

	private String host = null;
	private int port;
	private Main application = null;

	private Socket connection = null;
	private BufferedWriter output = null;

	public Writer(String host, int port, Main application) {
		this.host = host;
		this.port = port;
		this.application = application;
	}

	@Override
	public int connect() {
		int i = 0;
		while (i < 5) {
			try {
				this.connection = new Socket(host, port);
				this.output = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
				break;
			} catch (UnknownHostException e) {
				i++;
				e.printStackTrace();
			} catch (IOException e) {
				i++;
				e.printStackTrace();
			}
		}
		return i;
	}

	@Override
	public void disconnect() {
		try {
			if (this.output != null)
				this.output.close();
			if (this.connection != null)
				this.connection.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(String line) {
		try {
			System.out.println(line);
			this.output.write(line);
			this.output.flush();
		} catch (IOException e) {
			e.printStackTrace();
			this.application.createErrorConnectionScene();
		}
	}

	public void write(char ch) {
		try {
			this.output.write(ch);
			this.output.flush();
		} catch (IOException e) {
			e.printStackTrace();
			this.application.createErrorConnectionScene();
			this.application.fireHandler();
		}
	}

}
