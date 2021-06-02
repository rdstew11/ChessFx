import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class GameController {

	private Color currentPlayer;
	private Label whiteClock;
	private Label blackClock;
	private FlowPane messageBox;
	private HBox game;
	
	public GameController() {
		initialize();
		game = ChessApp.game;
	}
	
	public void initialize() {
		System.out.println("Initializing controller");
		
		
	}

}
