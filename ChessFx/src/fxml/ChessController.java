package fxml;



import java.util.ArrayList;
import java.util.Stack;
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
	private int white = 1;
	private int black = 2;	

	
	
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
	protected static final int[] whitePieces = {WP, WK, WQ, WB, WN, WR};
	protected static final int[] blackPieces = {BP, BK, BQ, BB, BN, BR};
	
	private final String[] COL_NAMES = {"a", "b", "c", "d", "e", "f", "g", "h"};
	
	private BoardModel boardModel = new BoardModel();
	private StackPane[][] tiles = new StackPane[8][8];
	private PlayerClient client;

	
	
	public void initialize() throws Throwable {
		System.out.println("Initializing controller");
		
		this.turnMessage.setText("White's turn to move");
	
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
				
				if(!selected.getChildren().isEmpty() && firstCoordinates == null && isMyTurn()){
					originalPane = selected;
					String id = selected.getId();
					System.out.println(id);
					firstCoordinates = id.split("_");
					int piece = boardModel.get(Integer.parseInt(firstCoordinates[1]) -  1, Integer.parseInt(firstCoordinates[0]) - 1);
					if((boardModel.myPlayer == 1 && piece > 6)|| (boardModel.myPlayer == 2 && piece < 7)) {
						System.out.println("This is not your piece");
						firstCoordinates = null;
						return;
					}
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
		this.startUp();
		
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
	
	protected void updateContext() {
		if(boardModel.myPlayer == black) {
			ObservableList<Node> gameBoardChildren = gameBoard.getChildren();
			for(Node child: gameBoardChildren) {
				String id = child.getId();
				String newText = "";
				if(id != null && id.length() >= 4) {
					if(id.substring(0, 4).equals("side")) {
						String reverseRows[] = {"8", "7", "6", "5", "4", "3", "2", "1"};
						int number = Integer.parseInt(id.substring(4,5));
						newText = reverseRows[number];
					}
					else if(id.substring(0,3).equals("bot")) {
						String letter = id.substring(3,4);
						String newLetter = "";
						switch(letter) {
						case "A":
							newText = "H";
							break;
						case "B":
							newText = "G";
							break;
						case "C":
							newText = "F";
							break;
						case "D":
							newText = "E";
							break;
						case "E":
							newText = "D";
							break;
						case "F":
							newText = "C";
							break;
						case "G":
							newText = "B";
							break;
						case "H":
							newText = "A";
							break;
						}
					}
				}
				
				Label childLabel = (Label) child;
				childLabel.setText(newText);
			}
		}
		loadTiles();
	}
	
	//creates empty stackPanes nested inside gameboard (gridPane)
	private void loadTiles() {
		ObservableList<Node> gameBoardChildren = gameBoard.getChildren();
		for(Node child : gameBoardChildren) {
			
			String id = child.getId();
			System.out.println("First ID: " + id);
			if(child.getId() != null) {
				String[] indices = id.split("_");
				int row = Integer.parseInt(indices[0]) - 1;
				int col = Integer.parseInt(indices[1]) - 1;
				
				//need to flip board if player is black
				if(boardModel.myPlayer == black) {
					row = 7 - row;
					col = 7 - col;
					int idC = col + 1;
					int idR = row + 1;
					String newId = idR + "_" + idC;
					System.out.println("newId :" + newId);
					child.setId(newId);
				}
				
				tiles[row][col] = (StackPane) child;
				
			}
			
		}
	}
	
	
	//Updates FXML of player clients
	protected void updateBoardView() {
	
		//reads numbers from boardModel, converts to Piece and places inside
		//stackPanes
		if(true){
			for(int r = 0; r < 8; r++) {
				for(int c = 0; c < 8; c++) {
					Piece piece = numToPiece(boardModel.get(r, c));
					tiles[c][r].getChildren().clear();
					if(piece != null) {
						tiles[c][r].getChildren().add(piece);
					}
				}
			}
		}
		/**
		else {	//if player is black, need to flip the board
			for(int r = 0; r < 8; r++) {
				for(int c = 0; c < 8; c++) {
					//need to swap H8 to the bottom left
					int tileC = 7 - c;
					int tileR = 7 - r;
					Piece piece = numToPiece(boardModel.get(r, c));
					tiles[tileC][tileR].getChildren().clear();
					if(piece != null) {
						tiles[tileC][tileR].getChildren().add(piece);
					}
				}
			}
		}*/

		
		//Updating white captured pieces display
		this.whiteCaptured.getChildren().clear();
		for(int pieceNumber : boardModel.deadWhite) {
			Piece piece = numToPiece(pieceNumber);
			piece.setFitWidth(45);
			piece.setPreserveRatio(true);
			this.whiteCaptured.getChildren().add(piece);
		}
		
		//Updating black captured pieces display
		this.blackCaptured.getChildren().clear();
		for(int pieceNumber : boardModel.deadBlack) {
			Piece piece = numToPiece(pieceNumber);
			piece.setFitWidth(45);
			piece.setPreserveRatio(true);
			this.blackCaptured.getChildren().add(piece);
		}
		
		
		//Creating list of previous moves for the bottom right of client
		@SuppressWarnings("unchecked")
		Stack<String> clonedPrevMoves = (Stack<String>) boardModel.previousMoves.clone();
		this.previousMoves.getChildren().clear();
		while(!clonedPrevMoves.isEmpty()) {
			Label temp = new Label(clonedPrevMoves.pop());
			this.previousMoves.getChildren().add(temp);
		}
		
		//Updating message below previous moves
		if(boardModel.blackInCheckmate) {
			this.turnMessage.setText("White wins!");
		} else if(boardModel.whiteInCheckmate) {
			this.turnMessage.setText("Black wins!");
		} else if(boardModel.blackInCheck) {
			this.turnMessage.setText("Black in check - Black's move");
		} else if(boardModel.whiteInCheck) {
			this.turnMessage.setText("White in check - White's move");
		} else {
			this.turnMessage.setText(boardModel.message);
		}
	}
	
	protected boolean isMyTurn() {
		return boardModel.currentPlayer == boardModel.myPlayer && boardModel.currentPlayer != 0 && boardModel.myPlayer != 0;
	}

	
	private void startUp() {
		this.loadTiles();
		this.updateBoardView();
	}
	
	/**
	 * Checks to make sure move is legal and that it is your turn before
	 * moving soiurce piece to destination and capturing any piece in destination
	 * @param row1 row index of src piece
	 * @param col1 col index of src piece
	 * @param row2 row index of destination
	 * @param col2 col index of destination
	 * @return 1 if move was successful, 0 if move was aborted 
	 * @throws Throwable
	 */
	private int movePiece(int row1, int col1, int row2, int col2) throws Throwable {
		int piece = boardModel.get(row1, col1);
		int capturedPieceNum = boardModel.get(row2, col2);
		
		if(!this.isLegal(row1,col1,row2,col2)) {
			return 0;
		}
		else if(!isMyTurn()) {
			return 0;
		}
		else {
			if(capturedPieceNum != 0) {
				if(capturedPieceNum > 6) {
					boardModel.deadBlack.add(capturedPieceNum);
				} else {
					boardModel.deadWhite.add(capturedPieceNum);
				}
			}
			
			boardModel.set(piece, row2, col2);
			boardModel.set(0, row1, col1);
			
			boardModel.whiteInCheck = isInCheck(boardModel, white);
			boardModel.blackInCheck = isInCheck(boardModel, black);
			if(boardModel.whiteInCheck) {
				boardModel.whiteInCheckmate = isInCheckmate(boardModel, white);
			}
			else {
				boardModel.whiteInCheckmate = false;
			}
			
			if(boardModel.blackInCheck) {
				boardModel.blackInCheckmate = isInCheckmate(boardModel, black);
			}
			else {
				boardModel.blackInCheckmate = false;
			}
			

			String message = boardModel.nMoves + ".)";
			
			if(boardModel.myPlayer == white) {
				message += "White";
			}
			else{
				message +="Black";
			}
			
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
			
			message += " to " + COL_NAMES[col2] + (row2 + 1);
			boardModel.previousMoves.push(message);
			
			boardModel.nMoves++;
			
			this.changePlayer();
			this.updateBoardView();
			this.client.sendUpdate();
			
			return 1;
		}
	}
	
	/**
	 * Finds king of given team 
	 * @param bm boardModel to search king for
	 * @param team team who's king you are searching for
	 * @return int array of row & col indices
	 */
	private int[] findKing(BoardModel bm, int team) {
		int row = -1, col = -1;
		for(int c = 0; c < 8 ; c++) {
			for(int r = 0; r < 8; r++) {
				int piece = bm.get(r, c);
				if((piece == WK && team == white || (piece == BK && team == black))) {
					row = r;
					col = c;
				}
			}
		}
		int[] kingIndex = {row, col};
		return kingIndex;
	}
	
	private boolean isInCheck(BoardModel bm, int team) {
		//find king
		
		int[] kingIndices = findKing(bm, team);
		int row = kingIndices[0], col = kingIndices[1];
		
		//king not found
		if(row == -1 || col == -1) {
			return false;
		}
		
		//check pawn attacks
		if((bm.get(row + 1, col + 1) == BP || bm.get(row + 1, col - 1) == BP) && team == white) {
			return true;
		}
		
		if((bm.get(row - 1,  col + 1) == WP || bm.get(row - 1, col - 1) == WP) && team == black) {
			return true;
		}
		
		//check diagonals
		int piece;
		
		//northeast (+i, +i)
		piece = getPieceNorthEast(bm, row, col)[0];
		//king may be attack
		if(((piece == BB || piece == BQ) && team == white) || ((piece == WB || piece == WQ) && team == black)) {
			return true;
		}
		
		//northwest (-i, +i)
		piece = getPieceNorthWest(bm, row, col)[0];
		//king may be attacked
		if(((piece == BB || piece == BQ) && team == white) || ((piece == WB || piece == WQ) && team == black)) {
			return true;
		}
		
		
		//southwest (-i, -i)
		piece = getPieceSouthWest(bm, row, col)[0];
		if(((piece == BB || piece == BQ) && team == white) || ((piece == WB || piece == WQ) && team == black)) {
			return true;
		}
		
		//southeast (+i, -i)
		piece = getPieceSouthEast(bm, row, col)[0];
		if(((piece == BB || piece == BQ) && team == white) || ((piece == WB || piece == WQ) && team == black)) {
			return true;
		}
		
		
		//check row & column
		
		//north (0, +i)
		piece = getPieceNorth(bm, row, col)[0];
		if(((piece == BR || piece == BQ) && team == white) || ((piece == WR || piece == WQ) && team == black)) {
			return true;
		}
		
		//east (+i, 0)
		piece = getPieceEast(bm, row, col)[0];
		if(((piece == BR || piece == BQ) && team == white) || ((piece == WR || piece == WQ) && team == black)) {
			return true;
		}
		
		//south (0, -i)
		piece = getPieceSouth(bm, row, col)[0];
		if(((piece == BR || piece == BQ) && team == white) || ((piece == WR || piece == WQ) && team == black)) {
			return true;
		}
		
		//west (-i, 0)
		piece = getPieceWest(bm, row, col)[0];
		if(((piece == BR || piece == BQ) && team == white) || ((piece == WR || piece == WQ) && team == black)) {
			return true;
		} 
		
		
		//check knight move
		for(int i = 0; i < KNIGHT_MOVES.length; i++) {
			piece = bm.get(row + KNIGHT_MOVES[i][0], col + KNIGHT_MOVES[i][1]);
			if((piece == BN && team == white) || (piece == WN && team == black)) {
				return true;
			}
		}
		return false;
	}
	
	private void changePlayer() {
		String teamColor;
		if(boardModel.currentPlayer == white) {
			boardModel.currentPlayer = black;
			teamColor = "Black";
		}
		else {
			boardModel.currentPlayer = white;
			teamColor = "White";
		}
		
		boardModel.message = (teamColor + "'s turn to move");
	}
	
	/**
	 * Checks to see if a team is in checkmate in the given BoardModel
	 * @param bm BoardModel to scan for checkmate in
	 * @param team team to scan checkmate for
	 * @return true if in checkmate, false if not in checkmate
	 * @throws Throwable
	 */
	private boolean isInCheckmate(BoardModel bm, int team) throws Throwable {
		int[] kingIndices = findKing(bm, team);
		int row = kingIndices[0], col = kingIndices[1];
		
		//can't be in checkmate if not in check
		if(!isInCheck(bm, team)) {
			return false;
		}
		
		//see if king can just move and escape check
		for(int i = 0; i < ADJACENTS.length; i++) {
			BoardModel simulator = bm.clone();
			
			int dst = simulator.get(row + ADJACENTS[i][0], col + ADJACENTS[i][1]);
			//if destination is on the same team, skip move
			if(dst != 0 && ((team == white && dst < 7) || (team == black && dst > 6))) {
				continue;
			} else {
				simulator.move(row, col, row + ADJACENTS[i][0], col + ADJACENTS[i][1]);
				if(!isInCheck(simulator, team)) {
					return false;
				}
			}
		}
		//find source(s) of check, then see if there's any way to disrupt
		ArrayList<int[]> sourcesOfCheck = getThreats(bm, row, col);
		
		for(int i = 0; i < sourcesOfCheck.size(); i++) {
			int[] src = sourcesOfCheck.get(i);
			int srcRow = src[1];
			int srcCol = src[2];
			int rowDiff = srcRow - row;
			int colDiff = srcCol - col;
			
			//checks to see if threat can be eliminated and remove check
			ArrayList<int[]> threatsOnSource = getThreats(bm,srcRow, srcCol);
			for(int j = 0; j < threatsOnSource.size(); j++) {
				int[] defender = threatsOnSource.get(j);
				BoardModel simulator = bm.clone();
				simulator.move(defender[1], defender[2], srcRow, srcCol);
				if(!isInCheck(simulator, team)) {
					return false;
				}
			}
			
			//finds tiles in path then checks to see if threat can be blocked
			
			//use step to make loop in correct direction
			int rowStep = 0, colStep = 0;
			if(rowDiff > 0) {
				rowStep = 1;
			} else if(rowDiff < 0) {
				rowStep = -1;
			}
			
			if(colDiff > 0) {
				colStep = 1;
			} else if(colDiff < 0) {
				colStep = -1;
			}
			
			int currentRow = row + rowStep;
			int currentCol = col + colStep;
			
			while(currentRow != srcRow && currentCol != srcCol) {
				ArrayList<int[]> potentialBlockers = getThreats(bm, currentRow, currentCol);
				for(int j = 0; j < potentialBlockers.size(); j++) {
					int[] blocker = potentialBlockers.get(j);
					
					//if potential blocking piece is not on your team, skip
					if((team == white && blocker[0] > 6) || (team == black && blocker[0] < 7)) {
						continue;
					} else {
						BoardModel simulator = bm.clone();
						simulator.move(blocker[1], blocker[2], currentRow, currentCol);
						if(!isInCheck(simulator, team)) {
							return false;
						}
					}
				}
				currentRow += rowStep;
				currentCol += colStep;
			}
			
			
		}
		
		
		//if nothing worked, then its check mate
		return true;
	}
	
	
	/**
	 * Finds all pieces capable of attacking given tile
	 * @param bm BoardModel to search through
	 * @param row row index of tile to find threats for
	 * @param col column index of tile to find threats for
	 * @return ArrayList containing int arrays of {piece, row index, col index} of pieces able to attack tile
	 */
	private ArrayList<int[]> getThreats(BoardModel bm, int row, int col){
		//find source(s) of check, then see if there's any way to disrupt
		ArrayList<int[]> sourcesOfThreat = new ArrayList<>();
		
		int team;
		
		if(bm.get(row, col) == 0) {
			team = 0;
		} else if(bm.get(row, col) > 6) {
			team = black;
		} else {
			team = white;
		}
		
		//check to see if pawns can move forward into empty tile (for blocking purposes)
		if(team == 0) {
			if(bm.get(row - 1, col) == WP) {
				int[] wp = {WP, row - 1, col};
				sourcesOfThreat.add(wp);
			}
			if(bm.get(row - 2, col) == WP && bm.get(row -1, col) == 0 && row - 2 == 1) {
				int[] wp = {WP, row - 2, col};
				sourcesOfThreat.add(wp);
			}
			if(bm.get(row + 1, col) == BP) {
				int[] bp = {BP, row + 1, col};
				sourcesOfThreat.add(bp);
			}
			if(bm.get(row + 2, col) == BP && bm.get(row + 1, col) == 0 && row + 2 == 6) {
				int[] bp = {BP, row + 2, col};
				sourcesOfThreat.add(bp);
			}
		}
		
		//check ne pawn attack for white
		if(team == white && boardModel.get(row + 1, col + 1) == BP){
			int[] bp = {BP, row + 1, col + 1};
			sourcesOfThreat.add(bp);
		}
		//check nw pawn attack for white
		if(team == white && boardModel.get(row + 1,  col - 1) == BP) {
			int[] bp = {BP, row + 1, col - 1};
			sourcesOfThreat.add(bp);
		}
		
		//check se pawn attack for black
		if(team == black && boardModel.get(row - 1, col + 1) == WP){
			int[] wp = {WP, row - 1, col + 1};
			sourcesOfThreat.add(wp);
		}
		//check sw pawn attack for black
		if(team == black && boardModel.get(row - 1,  col - 1) == WP) {
			int[] wp = {WP, row - 1, col - 1};
			sourcesOfThreat.add(wp);
		}
		
		//checking diagonals
		
		int[] dir = getPieceNorthEast(bm, row, col);
		if((team != black && (dir[0] == BQ || dir[0] == BB)) || (team != white && (dir[0] == WQ || dir[0] == WB))) {
			sourcesOfThreat.add(dir);
		}
		
		dir = getPieceNorthWest(bm, row, col);
		if((team != black && (dir[0] == BQ || dir[0] == BB)) || (team != white && (dir[0] == WQ || dir[0] == WB))) {
			sourcesOfThreat.add(dir);
		}
		
		dir = getPieceSouthEast(bm, row, col);
		if((team != black && (dir[0] == BQ || dir[0] == BB)) || (team != white && (dir[0] == WQ || dir[0] == WB))) {
			sourcesOfThreat.add(dir);
		}
		
		dir = getPieceSouthWest(bm, row, col);
		if((team != black && (dir[0] == BQ || dir[0] == BB)) || (team != white && (dir[0] == WQ || dir[0] == WB))) {
			sourcesOfThreat.add(dir);
		}
		
		//checking rows and columns
		
		dir = getPieceNorth(bm, row, col);
		if((team != black && (dir[0] == BQ || dir[0] == BR)) || (team != white && (dir[0] == WQ || dir[0] == WR))) {
			sourcesOfThreat.add(dir);
		}
		
		dir = getPieceEast(bm, row, col);
		if((team != black && (dir[0] == BQ || dir[0] == BR)) || (team != white && (dir[0] == WQ || dir[0] == WR))) {
			sourcesOfThreat.add(dir);
		}
		
		dir = getPieceSouth(bm, row, col);
		if((team != black && (dir[0] == BQ || dir[0] == BR)) || (team != white && (dir[0] == WQ || dir[0] == WR))) {
			sourcesOfThreat.add(dir);
		}
		
		dir = getPieceWest(bm, row, col);
		if((team != black && (dir[0] == BQ || dir[0] == BR)) || (team != white && (dir[0] == WQ || dir[0] == WR))) {
			sourcesOfThreat.add(dir);
		}
		
		return sourcesOfThreat;
	}
	
	/**
	 * Gets first piece north of src tile
	 * @param bm BoardModel to search in
	 * @param row row index of src tile
	 * @param col col index of src tile
	 * @return int array containing {piece, row index, col index}
	 */
	private int[] getPieceNorth(BoardModel bm, int row, int col) {
		//north (0, +i)
		//i starts @ 1 because there's no need to check src tile
		for(int i = 1; i < 8;  i++) {
			int piece = bm.get(row, col + i);
			if(piece != 0) {
				int[] solution = {piece, row, col + i};
				return solution;
			}
		}
		int[] noSolution = {-1, -1, -1};
		return noSolution;
	}
	
	/**
	 * Gets first piece south of src tile
	 * @param bm BoardModel to search in
	 * @param row row index of src tile
	 * @param col col index of src tile
	 * @return int array containing {piece, row index, col index}
	 */
	private int[] getPieceSouth(BoardModel bm, int row, int col) {
		//South (0, -i)
		//i starts @ 1 because there's no need to check src tile
		for(int i = 1; i < 8;  i++) {
			int piece = bm.get(row, col - i);
			if(piece != 0) {
				int[] solution = {piece, row, col - i};
				return solution;
			}
		}
		int[] noSolution = {-1, -1, -1};
		return noSolution;	
	}

	/**
	 * Gets first piece east of src tile
	 * @param bm BoardModel to search in
	 * @param row row index of src tile
	 * @param col col index of src tile
	 * @return integer representing first piece found
	 */
	private int[] getPieceEast(BoardModel bm, int row, int col) {
		//east (+i, 0)
		//i starts @ 1 because there's no need to check src tile
		for(int i = 1; i < 8;  i++) {
			int piece = bm.get(row + i, col);
			if(piece != 0) {
				int[] solution = {piece, row + i, col};
				return solution;			
			}
		}
		int[] noSolution = {-1, -1, -1};
		return noSolution;	
	}
	
	/**
	 * Gets first piece west of src tile
	 * @param bm BoardModel to search in
	 * @param row row index of src tile
	 * @param col col index of src tile
	 * @return integer representing first piece found
	 */
	private int[] getPieceWest(BoardModel bm, int row, int col) {
		//west (-i, 0)
		//i starts @ 1 because there's no need to check src tile
		for(int i = 1; i < 8;  i++) {
			int piece = bm.get(row - i, col);
			if(piece != 0) {
				int[] solution = {piece, row - i, col};
				return solution;
			}
		}
		int[] noSolution = {-1, -1, -1};
		return noSolution;	
	}
	
	/**
	 * Gets first piece northeast of src tile
	 * @param bm BoardModel to search in
	 * @param row row index of src tile
	 * @param col col index of src tile
	 * @return integer representing first piece found
	 */
	private int[] getPieceNorthEast(BoardModel bm, int row, int col) {
		//northeast (+i, +i)
		//i starts @ 1 because there's no need to check src tile
		for(int i = 1; i < 8;  i++) {
			int piece = bm.get(row + i, col + i);
			if(piece != 0) {
				int[] solution = {piece, row + i, col + i};
				return solution;
			}
		}
		int[] noSolution = {-1, -1, -1};
		return noSolution;	
	}
	
	/**
	 * Gets first piece northwest of src tile
	 * @param bm BoardModel to search in
	 * @param row row index of src tile
	 * @param col col index of src tile
	 * @return integer representing first piece found
	 */
	private int[] getPieceNorthWest(BoardModel bm, int row, int col) {
		//northwest (-i, +i)
		//i starts @ 1 because there's no need to check src tile
		for(int i = 1; i < 8;  i++) {
			int piece = bm.get(row + i, col - i);
			if(piece != 0) {
				int[] solution = {piece, row + i, col - i};
				return solution;
			}
		}
		int[] noSolution = {-1, -1, -1};
		return noSolution;	
	}
	
	/**
	 * Gets first piece southeast of src tile
	 * @param bm BoardModel to search in
	 * @param row row index of src tile
	 * @param col col index of src tile
	 * @return integer representing first piece found
	 */
	private int[] getPieceSouthEast(BoardModel bm, int row, int col) {
		//southeast (-i, -i)
		//i starts @ 1 because there's no need to check src tile
		for(int i = 1; i < 8;  i++) {
			int piece = bm.get(row - i, col - i);
			if(piece != 0) {
				int[] solution = {piece, row - i, col - i};
				return solution;
			}
		}
		int[] noSolution = {-1, -1, -1};
		return noSolution;	
	}
	
	/**
	 * Gets first piece southwest of src tile
	 * @param bm BoardModel to search in
	 * @param row row index of src tile
	 * @param col col index of src tile
	 * @return integer representing first piece found
	 */
	private int[] getPieceSouthWest(BoardModel bm, int row, int col) {
		//southeast (-i, +i)
		//i starts @ 1 because there's no need to check src tile
		for(int i = 1; i < 8;  i++) {
			int piece = bm.get(row - i, col + i);
			if(piece != 0) {
				int[] solution = {piece, row - i, col + i};
				return solution;
			}
		}
		int[] noSolution = {-1, -1, -1};
		return noSolution;
	}
	
	private boolean isLegal(int r1, int c1, int r2, int c2) throws Throwable {
		BoardModel nextMove = (BoardModel) boardModel.clone();
		int src = nextMove.get(r1, c1);
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
					if((occupyingPiece > 6 && src < 7) || (occupyingPiece < 7 && src > 6) ||  occupyingPiece == 0){
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
		
		//checks if move will place you in check
		if(isLegal) {
			BoardModel simulator = (BoardModel) boardModel.clone();
			simulator.move(r1, c1, r2, c2);
			if(simulator.myPlayer == white && isInCheck(simulator, white)) {
				isLegal = false;
			}
			else if(simulator.myPlayer == black && isInCheck(simulator, black)) {
				isLegal = false;
			}
			
		}
		return isLegal;
	}
	
	
	
	//takes in a predefined number and returns piece that corresponds to it
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
