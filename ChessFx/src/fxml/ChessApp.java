package fxml;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChessApp extends Application {

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		Parent root = FXMLLoader.load(getClass().getResource("ChessApp.fxml"));
		
		int height = 900, width = 650;
		Scene scene = new Scene(root, height, width);
		
		primaryStage.setTitle("Chess");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

}
