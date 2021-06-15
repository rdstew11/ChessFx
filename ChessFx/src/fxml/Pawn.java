package fxml;

public class Pawn extends Piece {
	
	/**
	 * 			(0,2)
	 * 	(-1,1)	(0,1)	(1,1)
	 * 			  X	
	 */
	
	
	public static int[][] blackLegalMoves = {{0,1}, {0,2}, {1,1}, {-1,1}};
	public static int[][] whiteLegalMoves = {{0,-1}, {0,-2}, {1,-1}, {-1,-1}};
	public Pawn(String team) {
		super(team);
		loadImage("Pawn");
	}
}
