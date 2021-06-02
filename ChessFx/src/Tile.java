
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class Tile extends StackPane{
	private Rectangle rectangle;
	private Piece piece;
	
	public Tile() {
		rectangle = new Rectangle();
		rectangle.setHeight(ChessApp.TILE_SIZE);
		rectangle.setWidth(ChessApp.TILE_SIZE);
		this.getChildren().add(rectangle);
	}
	
	public void setFill(Paint paint) {
		rectangle.setFill(paint);
	}
	
	public void setPiece(Piece piece) {
		this.piece = piece;
		this.getChildren().add(this.piece);

	}
}
