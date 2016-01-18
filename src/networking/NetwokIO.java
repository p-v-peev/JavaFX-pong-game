package networking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import application.Main;

public class NetwokIO extends Thread implements Networking {
	private String host = null;
	private Main application = null;
	private int port;

	private Socket connection = null;
	private BufferedWriter output = null;
	private BufferedReader input = null;

	public NetwokIO(String host, int port, Main application) {
		this.host = host;
		this.application = application;
		this.port = port;
	}

	@Override
	public int connect() {
		int i = 0;
		while (i < 5) {
			try {
				this.connection = new Socket(host, this.port);
				this.input = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
				this.output = new BufferedWriter(new OutputStreamWriter(this.connection.getOutputStream()));
				break;
			} catch (UnknownHostException e) {
				i++;
				e.printStackTrace();
			} catch (IOException e) {
				i++;
				e.printStackTrace();
			}
		}
		if (i < 5) {
			boolean connected = this.connectOtherPorts();
			if (connected) {
				this.application.createNamesScene();
				this.disconnect();

			} else {
				this.application.createErrorConnectionScene();
				this.disconnect();
			}
		} else {
			this.application.createErrorConnectionScene();
			this.disconnect();
		}
		return i;
	}

	@Override
	public void disconnect() {
		try {
			if (this.output != null)
				this.output.close();
			if (this.input != null)
				this.input.close();
			if (this.connection != null)
				this.connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		this.connect();
	}

	private String read() {
		try {
			return this.input.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void writeToOutput(String line) {
		try {
			this.output.write(line);
			this.output.flush();
		} catch (IOException e) {
			e.printStackTrace();
			this.application.createErrorConnectionScene();
		}
	}

	private boolean connectOtherPorts() {
		int outputConnect = 0;
		int inputConnect = 0;
		int udpConnected = 0;
		this.writeToOutput(":10");
		String command = this.read();
		if (command != null && command.equals(":20")) {
			command = this.read();
			if (command != null && command.equals(":31")) {
				command = this.read();
				if (command != null) {
					this.application.output = new Writer(this.host, Integer.parseInt(command), this.application);
					outputConnect = this.application.output.connect();
				}
			}
			command = this.read();
			if (command != null && command.equals(":32")) {
				command = this.read();
				if (command != null) {
					this.application.input = new Reader(this.host, Integer.parseInt(command), this.application);
					inputConnect = this.application.input.connect();
				}
			}
			command = this.read();
			if (command != null && command.equals(":33")) {
				command = this.read();
				if (command != null) {
					System.out.println(command);
					this.application.udpReader = new UDPReader(this.host, Integer.parseInt(command), this.application);
					udpConnected = this.application.udpReader.connect();
				}
			}
		} else {
			this.application.createErrorConnectionScene();
			return false;
		}

		if (outputConnect < 5 && inputConnect < 5 && udpConnected < 5) {
			this.application.udpReader.start();
			this.application.input.start();
			return true;
		} else {
			this.application.output.disconnect();
			this.application.input.disconnect();
			return false;
		}
	}

}
