import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class GameBoard extends GridPane{
	private Tile[][] tiles;
	
	public GameBoard(int nRows, int nCols) {
		tiles = new Tile[nRows][nCols];
		String[] rowNames = {"a","b","c","d","e","f","g","h"};
		for(int col = 0; col < nCols; col++) {
			for(int row = 0; row < nRows; row++) {
				Tile tile = new Tile(); 
				this.add(tile, col, row);
				
				if((col + row) % 2 == 0) {
					tile.setFill(Color.WHITE);
				}
				else {
					tile.setFill(Color.web("#7393B3"));
				}
				tile.setId(rowNames[row] + col);
				tiles[row][col] = tile;
			}
		}
		this.setBorder(ChessApp.SOLID_BLACK_BORDER);
	}
	
	public Tile getTile(int row, int col) {
		return tiles[row][col];
	}
	
}
