package fxml;

import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class ChessController {
	@FXML
	private Label whiteClock;
	@FXML
	private Label blackClock;
	@FXML
	private Label turnMessage;
	@FXML
	private GridPane gameBoard;
	@FXML
	private FlowPane previousMoves;
	@FXML
	private FlowPane whiteCaptured;
	@FXML
	private FlowPane blackCaptured;
	
	private Background selectedBackground = new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY));
	private String player1 = "White";
	private String player2 = "Black";
	private String currentPlayer = player1;
	private int nMoves = 1;
	/**
	 * wp white pawn	bp black pawn
	 * wk white king	bk black king
	 * wq white queen	bq black queen
	 * wb white bishop	bb black bishop
	 * wn white knight	bn black knight
	 * wr white rook	br black rook
	 */ 
	
	public static final int WP = 1, WK = 2, WQ = 3, WB = 4, WN = 5, WR = 6, BP = 7, BK = 8, BQ = 9, BB = 10, BN = 11, BR =12;
	
	private final String[] ROW_NAMES = {"a", "b", "c", "d", "e", "f", "g", "h"};
	
	private BoardModel boardModel = new BoardModel();
	private StackPane[][] tiles = new StackPane[8][8];
	
	public void initialize() {
		System.out.println("Initializing controller");
		
		this.turnMessage.setText("White's turn to move");
		this.whiteClock.setText("2:00");
		this.blackClock.setText("2:00");
		this.loadTiles();
		this.updateBoardView();
		this.gameBoard.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			String[] firstCoordinates = null;
			StackPane originalPane;
			Background originalBackground;
			@Override
			public void handle(MouseEvent ev) {
				System.out.println("Someone clicked board");
				Node target = (Node) ev.getTarget();
				while(!(target instanceof StackPane)) {
					if(target.getParent() == null) {
						ev.consume();
					}
					System.out.println("here");
					target =  target.getParent();
				}
				StackPane selected = (StackPane) target;
				
				if(!selected.getChildren().isEmpty() && firstCoordinates == null){
					originalPane = selected;
					String id = selected.getId();
					firstCoordinates = id.split("_");
					originalBackground = selected.getBackground();
					selected.setBackground(selectedBackground);
				}
				
				else if(firstCoordinates != null){
					String[] secondCoordinates = selected.getId().split("_");
					
					int c1 = Integer.parseInt(firstCoordinates[0]) - 1;
					int r1 = Integer.parseInt(firstCoordinates[1]) - 1;
					int c2 = Integer.parseInt(secondCoordinates[0])- 1;
					int r2 = Integer.parseInt(secondCoordinates[1])- 1;
					
					if(r1 != r2 || c1 != c2) {
						movePiece(r1, c1, r2, c2);
						changePlayer();
						ev.consume();
					}
					
					
					originalPane.setBackground(originalBackground);
					firstCoordinates = null;
				}
			}
			
		});
		
		
	}

	
	private void movePiece(int row1, int col1, int row2, int col2) {
		int piece = boardModel.get(row1, col1);
		int capturedPiece = boardModel.get(row2, col2);
		
		if(capturedPiece != 0) {
			boardModel.addCaptured(capturedPiece);
		}
		
		boardModel.set(piece, row2, col2);
		boardModel.set(0, row1, col1);
		
		String message = nMoves + ".)";
		
		switch(piece) {
			case BK: case WK:
				message += "King";
				break;
			case WQ: case BQ:
				message += "Queen";
				break;
			case BB: case WB:
				message += "Bishop";
				break;
			case BR: case WR:
				message += "Rook";
				break;
			case BN: case WN:
				message += "Knight";
				break;
			case BP: case WP:
				message += "Pawn";
				break;
			default:
				break;
		}
		
		message += " to " + ROW_NAMES[row2] + col2;
		Label messageLabel = new Label(message);
		this.previousMoves.getChildren().add(messageLabel);
		
		nMoves++;
		this.updateBoardView();
	}
	
	private void changePlayer() {
		if(currentPlayer == player1) {
			currentPlayer = player2;
		}
		else {
			currentPlayer = player1;
		}
		this.turnMessage.setText(currentPlayer + "'s turn to move");
	}
	
	private void loadTiles() {
		ObservableList<Node> gameBoardChildren = gameBoard.getChildren();
		for(Node child : gameBoardChildren) {
			
			String id = child.getId();
			if(child.getId() != null) {
				String[] indices = id.split("_");
				int row = Integer.parseInt(indices[0]) - 1;
				int col = Integer.parseInt(indices[1]) - 1;
				tiles[row][col] = (StackPane) child;
			}
			
		}
	}
	
	private void updateBoardView() {
		System.out.println(boardModel);
	
		for(int r = 0; r < 8; r++) {
			for(int c = 0; c < 8; c++) {
				Piece piece = numToPiece(boardModel.get(r, c));
				tiles[c][r].getChildren().clear();
				if(piece != null) {
					tiles[c][r].getChildren().add(piece);
				}
			}
		}
		
		ArrayList<Integer> captured = boardModel.getCaptured();
		
		for(int i = 0; i < captured.size(); i++) {
			Piece piece = numToPiece(captured.get(i));
			System.out.println(piece);
			piece.setFitHeight(60);
			if(piece.getTeam().equals("black")) {
				this.blackCaptured.getChildren().add(piece);
			}
			else
			{
				this.whiteCaptured.getChildren().add(piece);
			}
		}
	}
	
	private Piece numToPiece(int num)
	{
		Piece piece;
		switch(num) {
			case WP:
				piece = new Pawn("white");
				break;
			case WK:
				piece = new King("white");
				break;
			case WQ:
				piece = new Queen("white");
				break;
			case WB:
				piece = new Bishop("white");
				break;
			case WN:
				piece = new Knight("white");
				break;
			case WR:
				piece = new Rook("white");
				break;
			case BP:
				piece = new Pawn("black");
				break;
			case BK:
				piece = new King("black");
				break;
			case BQ:
				piece = new Queen("black");
				break;
			case BB:
				piece = new Bishop("black");
				break;
			case BN:
				piece = new Knight("black");
				break;
			case BR:
				piece = new Rook("black");
				break;
			default:
				piece = null;
				break;
		}
		return piece;
	}
	

}
