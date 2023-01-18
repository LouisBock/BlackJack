package main.java.de.louisbock.BlackJack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import com.google.gson.Gson;


public class DataBank {
	
	static DataBank singleton = null;

	public static synchronized DataBank getInstance() {
		if (singleton == null) {
			singleton = new DataBank();
		}
		return singleton;
	}
	
	private Gson gson = new Gson();
	private LinkedList<Player> playerList;
	
	public DataBank() {
		initDB();
	}
	
	private void initDB() {
		//reading JSON-String
		StringBuffer dbJsonString = new StringBuffer();
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader("res\\file.bin"));
			String line = input.readLine();
			while(line != null) {
				dbJsonString.append(line);
				line = input.readLine();
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		playerList = new LinkedList<Player>();
		Player[] players = gson.fromJson(dbJsonString.toString(), Player[].class);
		if(players != null) {
			for(Player p : players){
				playerList.add(p);
			}
		}
	}
	
	public void updateDB() {
		String dbJson = gson.toJson(playerList);
		
		BufferedWriter output = null;
		try {
			output = new BufferedWriter(new FileWriter("res\\file.bin"));
			output.write(dbJson);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Player loginPlayer(String name) {
		for(Player p : playerList) {
			if(p.getName().equals(name)) return p;
		}
		Player player = new Player(name);
		playerList.add(player);
		return player;
	}
}
