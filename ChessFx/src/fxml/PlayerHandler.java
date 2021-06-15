package fxml;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerHandler implements Runnable {
	
	private Socket socket;
	private int playerId;
	private GameServer server;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private BoardModel boardModel;
	
	public PlayerHandler(Socket socket, int playerId, GameServer server) throws Throwable {
		this.socket = socket;
		this.playerId = playerId;
		this.server = server;
		this.boardModel = new BoardModel();
		this.boardModel.myPlayer = playerId;
		System.out.println("Starting handler for id: " + this.playerId);
		in = new ObjectInputStream(this.socket.getInputStream());
		out = new ObjectOutputStream(this.socket.getOutputStream());
		PlayerRegistration regData = new PlayerRegistration(this.playerId);
		out.writeObject(regData);
		out.flush();
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				Object msg = this.in.readObject();
				this.server.notifyPlayers((BoardModel) msg);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void update(BoardModel bm) {
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
		try {
			this.out.writeObject(this.boardModel);
			this.out.flush();
			this.out.reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void startGame() throws Throwable {
		boardModel.currentPlayer = 1;
		if(boardModel.currentPlayer == boardModel.myPlayer) {
			boardModel.message = "Your turn, white";
		} else {
			boardModel.message = "Waiting for black to move";
		}
		this.out.writeObject(boardModel);
		this.out.flush();
		this.out.reset();
		ExecutorService myExec = Executors.newSingleThreadExecutor();
		myExec.submit(this);
	}
}
