package fxml;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class GameServer {
	protected static final int PORT = 7777;
	private static final int MAX_CLIENTS = 2;
	ArrayList<PlayerHandler> clients = new ArrayList<PlayerHandler>();
	ObjectInputStream in;
	
	public GameServer() throws Throwable{
		ServerSocket server;
		Socket client;
		server = new ServerSocket(PORT);
		while(clients.size() < MAX_CLIENTS) {
			System.out.println("Server listening on port: "  + PORT);
			client = server.accept();
			System.out.println("Client detected");
			if (clients.size() == MAX_CLIENTS) {
				System.out.println("Reached max clients - unable to connect");
				client.close();
			}
			else {
				clients.add(new PlayerHandler(client, clients.size() + 1, this));
			}
		}
		
		for(PlayerHandler player : clients) {
			player.startGame();
		}
		
	}
	
	public static void main(String[] args) throws Throwable {
		new GameServer();
	}

	
	/**
	 * Goes through list of PlayerHandlers ands calls update() which sends each the same version of the board model
	 * @param bm
	 */
	public void notifyPlayers(BoardModel bm) {
		for(PlayerHandler player: clients) {
			player.update(bm);
		}
	}
}
