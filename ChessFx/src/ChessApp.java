
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * ~~Color Codes~~
 * bfcba8
 * 5b8a72
 * 56776c
 * 464f41
 * ~~~~~~~~~~~~~~~
 */

public class ChessApp extends Application {
	
	public static HBox game = new HBox();
	public static final double TILE_SIZE = 70;
	public static final BorderStrokeStyle SOLID = BorderStrokeStyle.SOLID;
	public static final double CAPTURED_SIZE = 35;
	public static final Border SOLID_BLACK_BORDER = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.MEDIUM));
	public static final Border THIN_SOLID_BLACK_BORDER = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.THIN));

	public static final Font CLOCK_FONT = Font.font("Verdana", FontWeight.NORMAL, FontPosture.REGULAR, 48);
	
	public static void main(String[] args) {
		Application.launch(args);		
	}
	
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		int width = 905;
		int height = 640;
		
		Label title = new Label();
		title.setText("Chess");
		title.setFont(Font.font("Verdana",FontWeight.BOLD, FontPosture.REGULAR, 36));
		title.setTextFill(Color.WHITE);

		
		StackPane header = new StackPane();
		header.setBackground(new Background(new BackgroundFill(Color.web("#56776c"), CornerRadii.EMPTY, Insets.EMPTY)));
		header.setMaxWidth(Double.MAX_VALUE);
		header.setPrefHeight(75);
		header.getChildren().addAll(title);
		
		GameBoard board = freshBoard();
		
		Label capturedTitle = new Label();
		capturedTitle.setText("Captured Pieces");
		capturedTitle.setFont(Font.font("Verdana", FontWeight.NORMAL, FontPosture.REGULAR, 18));
		StackPane capturedHeader = new StackPane();
		capturedHeader.getChildren().add(capturedTitle);
		capturedHeader.setPrefWidth(width / 2);
		capturedHeader.setPrefHeight(30);
		capturedHeader.setBorder(SOLID_BLACK_BORDER);
		
		
		Insets clockPadding = new Insets(10);
		double panelWidth = 170;
		
		Label blackTime = new Label();
		blackTime.setId("blackClock");
		blackTime.setText("2:00");
		blackTime.setFont(CLOCK_FONT);
		blackTime.setTextFill(Color.WHITE);
		
		StackPane blackClock = new StackPane();
		blackClock.setPrefWidth(Double.MAX_VALUE);
		blackClock.getChildren().add(blackTime);
		blackClock.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
		blackClock.setPadding(clockPadding);
		blackClock.setPrefWidth(panelWidth);
		
		Label whiteTime = new Label();
		whiteTime.setId("whiteClock");
		whiteTime.setText("2:00");
		whiteTime.setFont(CLOCK_FONT);
		
		
		StackPane whiteClock = new StackPane();
		whiteClock.setMaxWidth(Double.MAX_VALUE);
		whiteClock.getChildren().add(whiteTime);
		whiteClock.setPadding(clockPadding);
		whiteClock.setPrefWidth(panelWidth);
		whiteClock.setBorder(new Border(new BorderStroke(Color.BLACK, null, Color.BLACK, Color.BLACK, SOLID, null, SOLID, SOLID, null, BorderStroke.MEDIUM, null)));
		
		FlowPane blackCapturedPieces = new FlowPane();
		blackCapturedPieces.setBorder(THIN_SOLID_BLACK_BORDER);
		blackCapturedPieces.prefHeight(300);
		blackCapturedPieces.setBackground(new Background(new BackgroundFill(Color.web("#bfcba8"), CornerRadii.EMPTY, Insets.EMPTY)));
		blackCapturedPieces.getChildren().add(new Label("Black Captured Pieces"));
		
		FlowPane whiteCapturedPieces = new FlowPane();
		whiteCapturedPieces.setBorder(THIN_SOLID_BLACK_BORDER);
		whiteCapturedPieces.setBackground(new Background(new BackgroundFill(Color.web("#bfcba8"), CornerRadii.EMPTY, Insets.EMPTY)));
		whiteCapturedPieces.prefHeight(300);
		whiteCapturedPieces.getChildren().add(new Label("White Captured Pieces"));

		
		GridPane sidePanel = new GridPane();
		ColumnConstraints sideCol1 = new ColumnConstraints();
		ColumnConstraints sideCol2 = new ColumnConstraints();
		sideCol1.setPercentWidth(50);
		sideCol2.setPercentWidth(50);
		sidePanel.getColumnConstraints().addAll(sideCol1, sideCol2);

		RowConstraints sideRow2 = new RowConstraints(300);
		sidePanel.getRowConstraints().addAll(new RowConstraints(), sideRow2, new RowConstraints());
		sidePanel.setPrefWidth(350);
		sidePanel.add(whiteClock, 0, 0);
		sidePanel.add(blackClock, 1, 0);
		sidePanel.add(whiteCapturedPieces, 0, 1);
		sidePanel.add(blackCapturedPieces, 1, 1);
		
		
		Label message = new Label();
		message.setText("This ain't checkers this is chess");
		FlowPane messageBox = new FlowPane(Orientation.VERTICAL);
		messageBox.setId("messageBox");
		messageBox.setBorder(SOLID_BLACK_BORDER);
		messageBox.setBackground(new Background(new BackgroundFill(Color.web("#bfcba8"), CornerRadii.EMPTY, Insets.EMPTY)));
		messageBox.getChildren().addAll(new Label("Console Messages"), message);
		messageBox.setPrefHeight(177);
		
		VBox side = new VBox();
		side.getChildren().addAll(sidePanel, messageBox);
		
		game.getChildren().addAll(board, side);
		GameController controller = new GameController();
		
		
		VBox root = new VBox();
		root.getChildren().addAll(header, game);
		
		Scene scene = new Scene(root, width, height);
		
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("Chess"); 
		primaryStage.show(); 
	}

	
	public GameBoard freshBoard() {
		GameBoard board = new GameBoard(8, 8);
	
		for(int i = 0; i < 8; i++) {
			Pawn whitePawn = new Pawn("white");
			Pawn blackPawn = new Pawn("Black");
			board.getTile(1, i).setPiece(blackPawn);
			board.getTile(6, i).setPiece(whitePawn);
		}
		
		Queen whiteQueen = new Queen("white");
		Queen blackQueen = new Queen("Black");
		board.getTile(7,3).setPiece(whiteQueen);
		board.getTile(0, 3).setPiece(blackQueen);
		
		King whiteKing = new King("white");
		King blackKing = new King("black");
		board.getTile(7, 4).setPiece(whiteKing);
		board.getTile(0, 4).setPiece(blackKing);
		
		Bishop whiteBishop1 = new Bishop("white");
		Bishop whiteBishop2 = new Bishop("white");
		Bishop blackBishop1 = new Bishop("black");
		Bishop blackBishop2 = new Bishop("black");
		board.getTile(7, 2).setPiece(whiteBishop1);
		board.getTile(7, 5).setPiece(whiteBishop2);
		board.getTile(0, 2).setPiece(blackBishop1);
		board.getTile(0, 5).setPiece(blackBishop2);
		
		Rook whiteRook1 = new Rook("white");
		Rook whiteRook2 = new Rook("white");
		Rook blackRook1 = new Rook("black");
		Rook blackRook2 = new Rook("black");
		board.getTile(7, 0).setPiece(whiteRook1);
		board.getTile(7, 7).setPiece(whiteRook2);
		board.getTile(0, 0).setPiece(blackRook1);
		board.getTile(0, 7).setPiece(blackRook2);
		
		Knight whiteKnight1 = new Knight("white");
		Knight whiteKnight2 = new Knight("white");
		Knight blackKnight1 = new Knight("black");
		Knight blackKnight2 = new Knight("black");
		board.getTile(7, 1).setPiece(whiteKnight1);
		board.getTile(7, 6).setPiece(whiteKnight2);
		board.getTile(0, 1).setPiece(blackKnight1);
		board.getTile(0, 6).setPiece(blackKnight2);

		return board;
	}
	

}
