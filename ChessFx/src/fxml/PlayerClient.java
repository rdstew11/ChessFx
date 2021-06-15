package fxml;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javafx.application.Platform;

public class PlayerClient implements Runnable {
	ObjectOutputStream out;
	ObjectInputStream in;
	ChessController ctlr;
	String serverIp;
	int port;
	BoardModel boardModel;
	
	public PlayerClient(String serverIp, int port, ChessController chessController, BoardModel boardModel) throws Throwable {
		this.serverIp = serverIp;
		this.port = port;
		this.ctlr = chessController;
		this.boardModel = boardModel;
		

	}
	/**
	 * Sends version of boardModel to game server
	 */
	public void sendUpdate() {
		try {
			BoardModel bm = new BoardModel();
			bm.currentPlayer = this.boardModel.currentPlayer;
			bm.model = this.boardModel.model;
			bm.message = this.boardModel.message;
			bm.deadWhite = this.boardModel.deadWhite;
			bm.deadBlack = this.boardModel.deadBlack;
			bm.myPlayer = this.boardModel.myPlayer;
			this.out.writeObject(bm);
			this.out.flush();
			this.out.reset();
			
			System.out.println("Sent data model to server:\n" + bm);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			System.out.format("Connecting to server at %s on port %d%n", serverIp, port);
			Socket socket = new Socket(serverIp, port);
			System.out.println("Connected to server");
			
			this.out = new ObjectOutputStream(socket.getOutputStream());
			this.in = new ObjectInputStream(socket.getInputStream());
			
			while(true) {
				Object msg = in.readObject();
				if(msg instanceof PlayerRegistration) {
					int playerId = ((PlayerRegistration) msg).playerId;
					System.out.println("Register as player " + playerId);
					boardModel.myPlayer = playerId;
				}
				else if(msg instanceof BoardModel) {
					System.out.println("Received boardModel from server");
					BoardModel bm = (BoardModel) msg;
					boardModel.model = bm.model;
					boardModel.currentPlayer = bm.currentPlayer;
					boardModel.deadWhite = bm.deadWhite;
					boardModel.deadBlack = bm.deadBlack;
					//boardModel.myPlayer = bm.myPlayer;
					boardModel.message = bm.message;
					Platform.runLater(new Runnable() {
						public void run() {
							ctlr.updateBoardView();
						}
					});
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
