import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Piece extends ImageView {
	protected String team;
	
	public Piece(String team) {
		this.team = team.toLowerCase();
	}
	
	protected void loadImage(String type) {
		FileInputStream stream;
		try {
			stream = new FileInputStream("src/imgs/" + this.team + type + ".png");
			this.setImage(new Image(stream));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}


