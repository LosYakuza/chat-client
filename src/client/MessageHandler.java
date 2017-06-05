package client;

import server.Message;

public interface MessageHandler {
	public void messageReceived(Message m);
}
