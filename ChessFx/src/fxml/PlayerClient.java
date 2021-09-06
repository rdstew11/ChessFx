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
			bm.blackInCheck = this.boardModel.blackInCheck;
			bm.whiteInCheck = this.boardModel.whiteInCheck;
			bm.blackInCheckmate = this.boardModel.blackInCheckmate;
			bm.whiteInCheckmate = this.boardModel.whiteInCheckmate;
			bm.previousMoves = this.boardModel.previousMoves;
			bm.nMoves = this.boardModel.nMoves;
			this.out.writeObject(bm);
			this.out.flush();
			this.out.reset();
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
					Platform.runLater(new Runnable() {
						public void run() {
							ctlr.loadTiles();
							ctlr.updateContext();
						}
					});
				}
				else if(msg instanceof BoardModel) {
					BoardModel bm = (BoardModel) msg;
					this.boardModel.model = bm.model;
					this.boardModel.currentPlayer = bm.currentPlayer;
					this.boardModel.message = bm.message;
					this.boardModel.deadBlack = bm.deadBlack;
					this.boardModel.deadWhite = bm.deadWhite;
					this.boardModel.blackInCheck = bm.blackInCheck;
					this.boardModel.whiteInCheck = bm.whiteInCheck;
					this.boardModel.blackInCheckmate = bm.blackInCheckmate;
					this.boardModel.whiteInCheckmate = bm.whiteInCheckmate;
					this.boardModel.previousMoves = bm.previousMoves;
					this.boardModel.nMoves = bm.nMoves;
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
