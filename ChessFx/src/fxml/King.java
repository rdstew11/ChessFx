package fxml;

public class King extends Piece{

	/**Adjacent Moves	
	 * 
	 * (-1,1)	(0,1)	(1,1)
	 * 	(-1,0)	  X		(1,0)
	 * 	(-1,-1)	(0,-1)	(1,-1)
	 */
	public static int[][] legalMoves = {{0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1, 0}, {-1,1}};
	public King(String team) {
		super(team);
		loadImage("King");
	}
}
