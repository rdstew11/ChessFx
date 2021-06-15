package fxml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;

public class BoardModel implements Cloneable, Serializable {	
	protected int[][] model = new int[8][8];
	protected ArrayList<Integer> deadWhite = new ArrayList<>();
	protected ArrayList<Integer> deadBlack = new ArrayList<>();
	protected int myPlayer = 0;
	protected int currentPlayer = 0;
	protected String message;
	protected Stack<String> previousMoves = new Stack<>();
	
	protected int nMoves = 1;
	
	protected boolean whiteInCheck;
	protected boolean blackInCheck;
	
	protected boolean whiteInCheckmate;
	protected boolean blackInCheckmate;
	
	
	public BoardModel () {
		for(int i = 0; i < 8; i++) {
			model[i][6] = ChessController.BP;
			model[i][1] = ChessController.WP;
			
			model[i][2] = 0;
			model[i][3] = 0;
			model[i][4] = 0;
			model[i][5] = 0;
		}
		
		model[0][7] = ChessController.BR;
		model[1][7] = ChessController.BN;
		model[2][7] = ChessController.BB;
		model[3][7] = ChessController.BQ;
		model[4][7] = ChessController.BK;
		model[5][7] = ChessController.BB;
		model[6][7] = ChessController.BN;
		model[7][7] = ChessController.BR;
		
		model[0][0] = ChessController.WR;
		model[1][0] = ChessController.WN;
		model[2][0] = ChessController.WB;
		model[3][0] = ChessController.WQ;
		model[4][0] = ChessController.WK;
		model[5][0] = ChessController.WB;
		model[6][0] = ChessController.WN;
		model[7][0] = ChessController.WR;
		

	}
	

	
	public int get(int row, int col) {
		if(row >= 8 || row  < 0 || col >= 8 || col < 0) {
			return 0;
		}
		return model[col][row];
	}
	
	public void set(int val, int row, int col) {
		model[col][row] = val;
	}
	
	public void move(int r1, int c1, int r2, int c2) {
		if(r2 > 7 || c2 > 7  || r2 < 0 || c2 < 0) {
			return;
		} else {
			model[c2][r2] = model[c1][r1];
			model[c1][r1] = 0;
		}
	}
	
	public String toString() {
		String output = "";
		
		for(int r = 0; r < 8; r++) {
			for(int c = 0; c < 8; c++) {
				output += model[c][r] + " ";
			}
			output += "\n";
		}
		output += "Current player: " + currentPlayer + "\nMy Player: " + myPlayer + "\nDead Black:";
		for(int piece : deadBlack) {
			output += " " + piece;
		}
		output += "\nDead White:";
		for(int piece : deadWhite) {
			output += " " + piece;
		}
		output += "\nwhite check(mate): " + whiteInCheck + " (" + whiteInCheckmate + ")" ;
		output += "\nblack check(mate): " + blackInCheck + " (" + blackInCheckmate + ")" ;
		return output;
	}
	
	@Override
	protected BoardModel clone() throws CloneNotSupportedException {
		BoardModel cloned = (BoardModel) super.clone();
		cloned.model = new int[this.model.length][];
		for(int i = 0; i < cloned.model.length; i++) {
			cloned.model[i] = this.model[i].clone();
		}
		
		return cloned;
	}
}
