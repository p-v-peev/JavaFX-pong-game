package networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import application.Main;

public class UDPReader extends Thread implements Networking {

	private String host = null;
	private int port;
	private Main application = null;

	private DatagramSocket connection = null;
	private InetAddress IPAddress = null;
	private DatagramPacket receivePacket = null;
	private byte[] recivePacket = null;

	public UDPReader(String host, int port, Main application) {
		this.host = host;
		this.port = port;
		this.application = application;
		this.recivePacket = new byte[11];

	}

	@Override
	public int connect() {
		int i = 0;
		while (i < 5) {
			try {
				this.connection = new DatagramSocket();
				this.IPAddress = InetAddress.getByName(this.host);
				this.receivePacket = new DatagramPacket(recivePacket, recivePacket.length);
				byte[] sendData = new byte[1];
				sendData[0] = 1;
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, this.IPAddress, this.port);
				this.connection.send(sendPacket);
				break;
			} catch (UnknownHostException e) {
				i++;
				e.printStackTrace();
			} catch (SocketException e) {
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
		this.connection.close();
	}

	public void run() {
		System.out.println("UDP Reader started.");
		String command = null;
		while ((command = readFromInput()) != null) {
			if (command.startsWith(":41")) {
				int i = Integer.parseInt(command.substring(3, 7)) - 1000;
				System.out.println(i);
				this.application.movePaddle(this.application.bottomPaddle, i);
			} else if (command.startsWith(":42")) {
				int i = Integer.parseInt(command.substring(3, 7)) - 1000;
				System.out.println(i);
				this.application.movePaddle(this.application.upperPaddle, i);
			}else if (command.startsWith(":43")) {
				int x = Integer.parseInt(command.substring(3, 7)) - 1000;
				System.out.println(x);
				int y = Integer.parseInt(command.substring(7, 11)) - 1000;
				System.out.println(y);
				this.application.moveBall(x, y);			
			}
			try {
				sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String readFromInput(){
		try {
			this.connection.receive(receivePacket);
			String command = new String(receivePacket.getData());
			System.out.println("UDP " + command);
			return command;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
