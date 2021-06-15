package fxml;

import java.io.Serializable;

public class PlayerRegistration implements Serializable{
	protected int playerId;
	
	public PlayerRegistration(int playerId) {
		this.playerId = playerId;
	}
}
