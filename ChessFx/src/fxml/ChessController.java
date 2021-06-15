package fxml;



import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
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
	private int player1 = 1;
	private int player2 = 2;	

	private int nMoves = 1;
	
	/**
	 * wp white pawn	bp black pawn
	 * wk white king	bk black king
	 * wq white queen	bq black queen
	 * wb white bishop	bb black bishop
	 * wn white knight	bn black knight
	 * wr white rook	br black rook
	 */ 
	
	private static final String SERVER_IP = "127.0.0.1";
	private static final int SERVER_PORT = 7777;
	
	protected static final int WP = 1, WK = 2, WQ = 3, WB = 4, WN = 5, WR = 6, BP = 7, BK = 8, BQ = 9, BB = 10, BN = 11, BR =12;
	protected static final int[][] ADJACENTS = {{0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}};
	protected static final int[][] KNIGHT_MOVES = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
	
	private final String[] ROW_NAMES = {"a", "b", "c", "d", "e", "f", "g", "h"};
	
	private BoardModel boardModel = new BoardModel();
	private StackPane[][] tiles = new StackPane[8][8];
	private PlayerClient client;
	
	public void initialize() throws Throwable {
		System.out.println("Initializing controller");
		
		this.turnMessage.setText("White's turn to move");
		this.whiteClock.setText("2:00");
		this.blackClock.setText("2:00");
		this.startUp();
		this.gameBoard.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			String[] firstCoordinates = null;
			StackPane originalPane;
			Background originalBackground;
			@Override
			public void handle(MouseEvent ev) {
				Node target = (Node) ev.getTarget();
				while(!(target instanceof StackPane)) {
					if(target.getParent() == null) {
						ev.consume();
					}
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
						try {
							movePiece(r1, c1, r2, c2);
						} catch (Throwable e) {
							e.printStackTrace();
						}						
					}
					
					
					originalPane.setBackground(originalBackground);
					firstCoordinates = null;
					ev.consume();
				}
			}
			
		});
		
		this.initServerComms();
		
		
	}
	/**
	 * Creates a new player client and runs its on its own thread
	 * @throws Throwable
	 */
	private void initServerComms() throws Throwable{
		this.client = new PlayerClient(SERVER_IP, SERVER_PORT, this, boardModel);
		ExecutorService myExec = Executors.newSingleThreadExecutor();
		myExec.submit(this.client);
	}
	
	protected boolean isMyTurn() {
		return boardModel.currentPlayer == boardModel.myPlayer && boardModel.currentPlayer != 0 && boardModel.myPlayer != 0;
	}

	
	private void startUp() {
		this.loadTiles();
		this.updateBoardView();
	}
	
	/**
	 * 
	 * @param row1 row index of src piece
	 * @param col1 col index of src piece
	 * @param row2 row index of destination
	 * @param col2 col index of destination
	 * @return
	 * @throws Throwable
	 */
	private int movePiece(int row1, int col1, int row2, int col2) throws Throwable {
		int piece = boardModel.get(row1, col1);
		int capturedPieceNum = boardModel.get(row2, col2);
		
		
		if(!this.isLegal(row1,col1,row2,col2)) {
			System.out.println("illegal move");
			return 0;
		}
		else if(!isMyTurn()) {
			System.out.println("Is not my turn");
			return 0;
		}
		else {
			if(capturedPieceNum != 0) {
				System.out.println("capturedPiece:" + capturedPieceNum);
				if(capturedPieceNum > 6) {
					boardModel.deadBlack.add(capturedPieceNum);
				} else {
					boardModel.deadWhite.add(capturedPieceNum);
				}
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
			
			this.changePlayer();
			nMoves++;
			this.updateBoardView();
			this.client.sendUpdate();
			
			return 1;
		}
		
		
	}
	
	private void changePlayer() {
		if(boardModel.currentPlayer == player1) {
			boardModel.currentPlayer = player2;
		}
		else {
			boardModel.currentPlayer = player1;
		}
		System.out.println("Changed player to " + boardModel.currentPlayer);
		boardModel.message = (boardModel.currentPlayer + "'s turn to move");
	}
	
	private boolean isLegal(int r1, int c1, int r2, int c2) throws Throwable {
		BoardModel nextMove = (BoardModel) boardModel.clone();
		int src = nextMove.get(r1, c1);
		int dst = nextMove.get(r2, c2);
		ArrayList<int[]> legalMoves = new ArrayList<>();
		
		switch(src) {
			case 0:
				return false;
			case WP:
				int pieceLeft = nextMove.get(r1 + 1, c1 -1);
				int pieceRight = nextMove.get(r1 + 1, c1 + 1);
				
				if(nextMove.get(r1 + 1, c1) == 0) {
					int[] whitePawnForward = {0, 1};
					legalMoves.add(whitePawnForward);
					//if pawn is still in starting row
					if(r1 == 1){
						int[] whitePawnDoubleForward = {0, 2};
						legalMoves.add(whitePawnDoubleForward);
					}
				}
				
				//if piece diagonal right is black
				if((pieceRight == BP) || (pieceRight == BB) || (pieceRight == BR) || (pieceRight == BN) || (pieceRight == BQ) ||(pieceRight == BK)){
					int[] whitePawnAttackRight = {1, 1};
					legalMoves.add(whitePawnAttackRight);
				}
				//if piece diagonal left is black
				if((pieceLeft == BP) || (pieceLeft == BB) || (pieceLeft == BR) || (pieceLeft == BN) || (pieceLeft == BQ) ||(pieceLeft == BK)){
					int[] whitePawnAttackLeft = {-1, 1};
					legalMoves.add(whitePawnAttackLeft);
				}
				break;
			case BP:
				
				pieceRight = nextMove.get(r1 - 1, c1 + 1);
				pieceLeft = nextMove.get(r1 - 1, c1 - 1);
				
				
				if(nextMove.get(r1 - 1, c1) == 0) {
					int[] blackPawnForward = {0, -1};
					legalMoves.add(blackPawnForward);
					//if pawn is still in starting row 
					if(r1 == 6) {
						int[] blackPawnDoubleForward = {0, -2};
						legalMoves.add(blackPawnDoubleForward);
					}
				}
				
				//if piece diagonal right is white
				if((pieceRight == WP) || (pieceRight == WB) || (pieceRight == WR) || (pieceRight == WN) || (pieceRight == WQ) ||(pieceRight == WK)) {
					int[] blackPawnAttackRight = {1, -1};
					legalMoves.add(blackPawnAttackRight);
				}
				//if piece diagonal left is white
				if((pieceLeft == WP) || (pieceLeft == WB) || (pieceLeft == WR) || (pieceLeft == WN) || (pieceLeft == WQ) ||(pieceLeft == WK)) {
					int[] blackPawnAttackLeft = {-1, -1};
					legalMoves.add(blackPawnAttackLeft);
				}
				break;
			case WK: case BK:
				for(int i = 0; i < ADJACENTS.length; i++) {
					int occupyingPiece = nextMove.get(r1 + ADJACENTS[i][1], c1 + ADJACENTS[i][0]);
					if((occupyingPiece > 6 && src < 7) || occupyingPiece == 0 || (occupyingPiece < 7 && src > 6)){
						legalMoves.add(ADJACENTS[i]);
					}
				}
				break;
			case WB: case BB:
				boolean neLegal = true;
				boolean nwLegal = true;
				boolean seLegal = true;
				boolean swLegal = true;
				
				for(int i = 1; i < 8; i++) {
					if(neLegal) {				
						int occupyingPiece = nextMove.get(r1 + i, c1 + i);
						//ensure index not out of bounds
						if(((r1 + i) < 8) && ((c1 + i) < 8)) {
							int[] ne = {i, i};
							//if space is unoccupied
							if(occupyingPiece == 0) {
								legalMoves.add(ne);
							}
							//if space is occupied by other team
							else if((src < 7 && occupyingPiece > 6) || (src > 6 && occupyingPiece < 7)) {
								legalMoves.add(ne);
								neLegal = false;
							}
							//space is occupied by your team
							else {
								neLegal = false;
							}
						}
					}
					
					if(nwLegal) {
						int occupyingPiece = nextMove.get(r1 + i, c1 - i);
						//ensure index not out of bounds
						if(((r1 + i) < 8) && ((c1 - i) >= 0)) {
							int[] nw = {-i, i};
							
							//if space is unoccupied
							if(occupyingPiece == 0) {
								legalMoves.add(nw);
							}
							//if space is occupied by other team
							else if((src < 7 && occupyingPiece > 6) || (src > 6 && occupyingPiece < 7)) {
								legalMoves.add(nw);
								nwLegal = false;
							}
							//space is occupied by your team
							else {
								nwLegal = false;
							}
						}
					}
					
					if(seLegal) {
						int occupyingPiece = nextMove.get(r1 - i, c1 + i);
						//ensure index not out of bounds
						if(((r1 - i) >= 0) && ((c1 + i) < 8)) {
							int[] se = {i, -i};
							//if space is unoccupied
							if(occupyingPiece == 0) {
								legalMoves.add(se);
							}
							//if space is occupied by other team
							else if((src < 7 && occupyingPiece > 6) || (src > 6 && occupyingPiece < 7)) {
								legalMoves.add(se);
								seLegal = false;
							}
							//space is occupied by your team
							else {
								seLegal = false;
							}
						}
					}
					
					if(swLegal) {
						int occupyingPiece = nextMove.get(r1 - i, c1 - i);
						//ensure index not out of bounds
						if(((r1 - i) >= 0) && ((c1 - i) >= 0)) {
							int[] sw = {-i, -i};
							//if space is unoccupied
							if(occupyingPiece == 0) {
								legalMoves.add(sw);
							}
							//if space is occupied by other team
							else if((src < 7 && occupyingPiece > 6) || (src > 6 && occupyingPiece < 7)) {
								legalMoves.add(sw);
								swLegal = false;
							}
							//space is occupied by your team
							else {
								swLegal = false;
							}
						}
					}
				}
				break;
			case WN: case BN:
				for(int i =0; i < KNIGHT_MOVES.length; i++) {
					int occupyingPiece = nextMove.get(r1 + KNIGHT_MOVES[i][1], c1 + KNIGHT_MOVES[i][0]);
					if((occupyingPiece > 6 && src < 7) || occupyingPiece == 0){
						legalMoves.add(KNIGHT_MOVES[i]);
					}
				}
				break;
			case WR: case BR:
				boolean eLegal = true;
				boolean nLegal = true;
				boolean sLegal = true;
				boolean wLegal = true;
				for(int i = 1; i < 8; i++) {
					if(c1 + i < 8) {
						if(eLegal) {
							int occupyingPiece = nextMove.get(r1, c1 + i);
							int[] e = {i, 0};
							//space is unoccupied
							if(occupyingPiece == 0) {
								legalMoves.add(e);
							}
							//space is occupied by other team
							else if((src < 7 && occupyingPiece > 6) || (src > 6 && occupyingPiece < 7)) {
								legalMoves.add(e);
								eLegal = false;
							//space is occupied by own team
							}else {
								eLegal = false;
							}
						}
					}
					
					if(c1 - i >= 0) {
						if(wLegal) {
							int occupyingPiece = nextMove.get(r1, c1 - i);
							int[] w = {-i, 0};
							//space is unoccupied
							if(occupyingPiece == 0) {
								legalMoves.add(w);
							}
							//space is occupied by other team
							else if((src < 7 && occupyingPiece > 6) || (src > 6 && occupyingPiece < 7)) {
								legalMoves.add(w);
								wLegal = false;
							//space is occupied by own team
							}else {
								wLegal = false;
							}
						}
					}
					
					if(r1 + i < 8) {
						if(nLegal) {
							int occupyingPiece = nextMove.get(r1 + i, c1);
							int[] n = {0, i};
							//space is unoccupied
							if(occupyingPiece == 0) {
								legalMoves.add(n);
							}
							//space is occupied by other team
							else if((src < 7 && occupyingPiece > 6) || (src > 6 && occupyingPiece < 7)) {
								legalMoves.add(n);
								nLegal = false;
							//space is occupied by own team
							}else {
								nLegal = false;
							}
						}
					}
					
					if(r1 - i >= 0) {
						if(sLegal) {
							int occupyingPiece = nextMove.get(r1 - i, c1);
							int[] s = {0, -i};
							//space is unoccupied
							if(occupyingPiece == 0) {
								legalMoves.add(s);
							}
							//space is occupied by other team
							else if((src < 7 && occupyingPiece > 6) || (src > 6 && occupyingPiece < 7)) {
								legalMoves.add(s);
								sLegal = false;
							//space is occupied by own team
							}else {
								sLegal = false;
							}
						}
					}
					
				}
				break;
			case WQ: case BQ:
				eLegal = true;
				nLegal = true;
				sLegal = true;
				wLegal = true;
				for(int i = 1; i < 8; i++) {
					if(c1 + i < 8) {
						if(eLegal) {
							int occupyingPiece = nextMove.get(r1, c1 + i);
							int[] e = {i, 0};
							//space is unoccupied
							if(occupyingPiece == 0) {
								legalMoves.add(e);
							}
							//space is occupied by other team
							else if((src < 7 && occupyingPiece > 6) || (src > 6 && occupyingPiece < 7)) {
								legalMoves.add(e);
								eLegal = false;
							//space is occupied by own team
							}else {
								eLegal = false;
							}
						}
					}
					
					if(c1 - i >= 0) {
						if(wLegal) {
							int occupyingPiece = nextMove.get(r1, c1 - i);
							int[] w = {-i, 0};
							//space is unoccupied
							if(occupyingPiece == 0) {
								legalMoves.add(w);
							}
							//space is occupied by other team
							else if((src < 7 && occupyingPiece > 6) || (src > 6 && occupyingPiece < 7)) {
								legalMoves.add(w);
								wLegal = false;
							//space is occupied by own team
							}else {
								wLegal = false;
							}
						}
					}
					
					if(r1 + i < 8) {
						if(nLegal) {
							int occupyingPiece = nextMove.get(r1 + i, c1);
							int[] n = {0, i};
							//space is unoccupied
							if(occupyingPiece == 0) {
								legalMoves.add(n);
							}
							//space is occupied by other team
							else if((src < 7 && occupyingPiece > 6) || (src > 6 && occupyingPiece < 7)) {
								legalMoves.add(n);
								nLegal = false;
							//space is occupied by own team
							}else {
								nLegal = false;
							}
						}
					}
					
					if(r1 - i >= 0) {
						if(sLegal) {
							int occupyingPiece = nextMove.get(r1 - i, c1);
							int[] s = {0, -i};
							//space is unoccupied
							if(occupyingPiece == 0) {
								legalMoves.add(s);
							}
							//space is occupied by other team
							else if((src < 7 && occupyingPiece > 6) || (src > 6 && occupyingPiece < 7)) {
								legalMoves.add(s);
								sLegal = false;
							//space is occupied by own team
							}else {
								sLegal = false;
							}
						}
					}
					
				}
				
				neLegal = true;
				nwLegal = true;
				seLegal = true;
				swLegal = true;
				
				for(int i = 1; i < 8; i++) {
					if(neLegal) {				
						int occupyingPiece = nextMove.get(r1 + i, c1 + i);
						//ensure index not out of bounds
						if(((r1 + i) < 8) && ((c1 + i) < 8)) {
							int[] ne = {i, i};
							//if space is unoccupied
							if(occupyingPiece == 0) {
								legalMoves.add(ne);
							}
							//if space is occupied by other team
							else if((src < 7 && occupyingPiece > 6) || (src > 6 && occupyingPiece < 7)) {
								legalMoves.add(ne);
								neLegal = false;
							}
							//space is occupied by your team
							else {
								neLegal = false;
							}
						}
					}
					
					if(nwLegal) {
						int occupyingPiece = nextMove.get(r1 + i, c1 - i);
						//ensure index not out of bounds
						if(((r1 + i) < 8) && ((c1 - i) >= 0)) {
							int[] nw = {-i, i};
							
							//if space is unoccupied
							if(occupyingPiece == 0) {
								legalMoves.add(nw);
							}
							//if space is occupied by other team
							else if((src < 7 && occupyingPiece > 6) || (src > 6 && occupyingPiece < 7)) {
								legalMoves.add(nw);
								nwLegal = false;
							}
							//space is occupied by your team
							else {
								nwLegal = false;
							}
						}
					}
					
					if(seLegal) {
						int occupyingPiece = nextMove.get(r1 - i, c1 + i);
						//ensure index not out of bounds
						if(((r1 - i) >= 0) && ((c1 + i) < 8)) {
							int[] se = {i, -i};
							//if space is unoccupied
							if(occupyingPiece == 0) {
								legalMoves.add(se);
							}
							//if space is occupied by other team
							else if((src < 7 && occupyingPiece > 6) || (src > 6 && occupyingPiece < 7)) {
								legalMoves.add(se);
								seLegal = false;
							}
							//space is occupied by your team
							else {
								seLegal = false;
							}
						}
					}
					
					if(swLegal) {
						int occupyingPiece = nextMove.get(r1 - i, c1 - i);
						//ensure index not out of bounds
						if(((r1 - i) >= 0) && ((c1 - i) >= 0)) {
							int[] sw = {-i, -i};
							//if space is unoccupied
							if(occupyingPiece == 0) {
								legalMoves.add(sw);
							}
							//if space is occupied by other team
							else if((src < 7 && occupyingPiece > 6) || (src > 6 && occupyingPiece < 7)) {
								legalMoves.add(sw);
								swLegal = false;
							}
							//space is occupied by your team
							else {
								swLegal = false;
							}
						}
					}
				}
				break;
		}
		
		boolean isLegal = false;
		for(int i = 0; i < legalMoves.size(); i++){
			int[] move = legalMoves.get(i);
			if((c2 == (move[0] + c1)) && (r2 == (move[1] + r1))){
				isLegal = true;
				break;
			}
		}
		return isLegal;
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
	
	protected void updateBoardView() {
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
		
		this.whiteCaptured.getChildren().clear();
		for(int pieceNumber : boardModel.deadWhite) {
			Piece piece = numToPiece(pieceNumber);
			piece.setFitWidth(45);
			piece.setPreserveRatio(true);
			this.whiteCaptured.getChildren().add(piece);
		}
		
		this.blackCaptured.getChildren().clear();
		for(int pieceNumber : boardModel.deadBlack) {
			Piece piece = numToPiece(pieceNumber);
			piece.setFitWidth(45);
			piece.setPreserveRatio(true);
			this.blackCaptured.getChildren().add(piece);
		}
		
		this.turnMessage.setText(boardModel.message);
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
