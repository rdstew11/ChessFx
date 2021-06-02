package fxml;





import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
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
	
	private Color player1 = Color.WHITE;
	private Color player2 = Color.BLACK;
	private Color currentPlayer = player1;
	
	private final String[] ROW_NAMES = {"a", "b", "c", "d", "e", "f", "g", "h"};
	
	private String[][] boardModel = new String[8][8];
	private StackPane[][] tiles = new StackPane[8][8];
	
	public void initialize() {
		System.out.println("Initializing controller");
		
		this.turnMessage.setText("White's turn to move");
		this.whiteClock.setText("2:00");
		this.blackClock.setText("2:00");
		this.loadTiles();
		//this.freshBoard();
		
		this.gameBoard.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent ev) {
				System.out.println("Someone clicked board");
				Node target = (Node) ev.getTarget();
				while(!(target instanceof StackPane)) {
					target = target.getParent();
				}
				System.out.println(target.getId());
			}
			
		});
	}
	
	private void changePlayer() {
		if(currentPlayer == player1) {
			currentPlayer = player2;
		}
		else {
			currentPlayer = player1;
		}
	}
	
	private void loadTiles() {
		ObservableList<Node> gameBoardChildren = gameBoard.getChildren();
		for(Node child : gameBoardChildren) {
			
			String id = child.getId();
			if(child.getId() != null) {
				
			}
		}
	}
	
	private void updateBoardView() {
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				switch(boardModel[i][j]) {
					case "wp":
						Pawn whitePawn = new Pawn("white");
						tiles[i][j].getChildren().add(whitePawn);
						break;
					default:
						
				}
			}
		}
	}
	
	private void freshBoard() {
		boardModel[0][0] = "wp";
		
		this.updateBoardView();
	}

}
