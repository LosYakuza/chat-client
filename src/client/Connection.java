package client;

import server.Message;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 * Conecta y escucha mensaje del server
 *
 */
public class Connection extends Thread {

	private Socket s;
	private DataInputStream i;
	private DataOutputStream o;
	private String user;
	private MessageHandler mh;
	private int stop = 0;

	public Connection(String host, int port, String user, MessageHandler mh) throws Exception {
		this.s = new Socket(host, port);
		this.user = user;
		this.mh = mh;
		this.i = new DataInputStream(new BufferedInputStream(this.s.getInputStream()));
		this.o = new DataOutputStream(new BufferedOutputStream(this.s.getOutputStream()));
	}

	public void stopRequest(){
		stop=1;
	}
	
	@Override
	public void run() {
		boolean c = true;
		while (c && stop==0) {
			try {
				Message msg = new Message(i.readUTF());
				System.out.println("IN client: " + msg);
				process(msg);
			}catch(EOFException e){
				
			}catch (IOException e) {
				e.printStackTrace();
				
				c=false;
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}
		kissoff();
	}

	private void kissoff() {
		try {
			this.s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void process(Message msg) throws IOException {
		/**
		 * Mensajes de protocolo
		 */
		if (msg.getType() == Message.SERVER_ASK) {
			if (msg.getText().equals("login")) {
				login();
				return;
			}
		}
		
		/**
		 * Envia para ser mostrado 
		 */
		this.mh.messageReceived(msg);

	}

	private void login() throws IOException {
		Message m = new Message();
		m.setDestination("user");
		m.setText(this.user);
		m.setType(Message.CLIENT_DATA);
		this.o.writeUTF(m.toString());
		this.o.flush();
	}
	
	public void sendChat(String usr,String text) throws IOException{
		Message m = new Message();
		m.setType(Message.USR_MSJ);
		m.setDestination(usr);
		m.setText(text);
		this.o.writeUTF(m.toString());
		this.o.flush();
	}

}
