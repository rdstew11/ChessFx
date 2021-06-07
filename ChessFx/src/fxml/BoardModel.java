package fxml;

import java.util.ArrayList;

public class BoardModel {	
	private int[][] model = new int[8][8];
	private ArrayList<Integer> captured = new ArrayList<>();
	
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
		return model[col][row];
	}
	
	public void set(int val, int row, int col) {
		model[col][row] = val;
	}
	
	public void addCaptured(int piece) {
		captured.add(piece);
		
	}
	
	public ArrayList<Integer> getCaptured() {
		return captured;
	}

	
	public String toString() {
		String output = "\t 1 2 3 4 5 6 7 8 \n";
		
		for(int r = 0; r < 8; r++) {
			output += r + "\t";
			for(int c = 0; c < 8; c++) {
				output += model[c][r] + " ";
			}
			output += "\n";
		}
		
		return output;
	}
}
