package main.java.de.louisbock.BlackJack;

import java.util.LinkedList;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class Hand {
	
	private final int VALUE_MAX = 21;
	private LinkedList<DeckCards> cards = new LinkedList<DeckCards>();
	private Player player;
	private boolean splitHand;
	private double bet = 1;
	private float prizeFactor = 0;
	
	//fx
	private HBox cardBox;
	private VBox handBox;
	private Label playerLabel;
	
	public Hand(Player player, boolean splitHand) {
		this.player = player;
		this.splitHand = splitHand;
		
		//fx
		cardBox = new HBox();
		handBox = new VBox(10);
		playerLabel = new Label();
		
		cardBox.setAlignment(Pos.CENTER);
		playerLabel.setAlignment(Pos.CENTER);
		playerLabel.setFont(Font.font(32));
		playerLabel.setText(player.getName()+ ": " + player.getMoney());
		handBox.getChildren().addAll(cardBox, playerLabel);
		handBox.setAlignment(Pos.CENTER);
	}
	
	public void addCard(DeckCards card) {
		cards.add(card);
	}
	public void discardCards() {
		cards.removeAll(cards);
		cardBox.setStyle("-fx-background-color: #14A92B;");
		prizeFactor = 0;
	}
	
	public int getValue() {
		int value = 0;
		int haseAceAtIndex = -1;
		boolean only7 = true;
		for(DeckCards card : cards) {
			value += card.getValue();
			if(card == DeckCards.ACE) haseAceAtIndex = cards.indexOf(card);
			if(only7 && card != DeckCards.SEVEN) only7 = false;
		}
		if(value > VALUE_MAX) {
			if(haseAceAtIndex > -1) {
				cards.set(haseAceAtIndex, DeckCards.ONE);
				return getValue();
			}
			else {
				return -1;
			}
		}
		if(value == 21 && ((cards.size() == 2)||(cards.size() == 3 && only7))) {
			value = 22;
		}
		return value;
	}
	
	public void printHand() {
		System.out.print("value: " + getValue() + "| prizeFactor: " + prizeFactor + "| Cards: ");
		for(DeckCards c: cards) {
			System.out.print(c + " ");
		}
		System.out.println("");
	}
	
	public void payBet(double ammount) {
		bet = ammount;
		player.subMoney(ammount);
	}
	
	public void doubleBet() {
		player.subMoney(bet);
		bet = bet*2;
	}

	public void calculatePrizeFactor(int dealerValue) {//maybe join with its getter method to one method
		int value = getValue();
		if(value == -1) {
			prizeFactor = 0;
			cardBox.setStyle("-fx-background-color: #630000;");
		}else if(dealerValue < value) {
			if(value == 22) {
				prizeFactor = 2.5f;
				cardBox.setStyle("-fx-background-color: #E2B007;");
			}
			else {
				prizeFactor = 2;
				cardBox.setStyle("-fx-background-color: #00630f;");
			}
		}
		else if(dealerValue == value) {
			prizeFactor = 1;
			cardBox.setStyle("-fx-background-color: #14A92B;");
		}
		else {
			prizeFactor = 0;
			cardBox.setStyle("-fx-background-color: #630000;");
		}
		player.addMoney(prizeFactor*bet);
	}

//--GETTERS------------------------------------------
	
	public Pane getHandBox() {
		cardBox.getChildren().remove(0, cardBox.getChildren().size());
		for(DeckCards card : cards) {
			cardBox.getChildren().add(new ImageView(Table.getImages()[card.getImageIndex()]));
		}
		cardBox.setMaxWidth(cards.size()*Table.getCardWidth());
		if(player.getName().equals("Dealer")) return cardBox;
		
		playerLabel.setText("Value: " + getValue() + "\n" + player.getName()+ ": " + player.getMoney());
		handBox.getChildren().remove(0, handBox.getChildren().size());
		handBox.getChildren().addAll(cardBox, playerLabel);
		return handBox;
	}
	
	public Label getPlayerLabel() {
		playerLabel.setText(player.getName()+ ": " + player.getMoney());
		return playerLabel;
	}
	
	public boolean getHitable() {
		int value = getValue();
		return value > -1 && value < 21;
	}
	
	public boolean getDoubleable() {
		//TODO check money
		return cards.size() == 2 && getHitable();
	}
	
	public boolean getSplitable() {
		//TODO check money
		if(cards.size() != 2) return false;
		if((cards.get(0).equals(DeckCards.ACE) || cards.get(0).equals(DeckCards.ONE))
				&& (cards.get(1).equals(DeckCards.ACE) || cards.get(1).equals(DeckCards.ONE))) return true;
		return cards.get(0).equals(cards.get(1));
	}
	
	public boolean isSplitHand() {
		return splitHand;
	}
	
	public boolean hasCard(DeckCards c) {
		for(DeckCards card : cards) {
			if(card.equals(c)) return true;
		}
		return false;
	}
	
	public DeckCards getFirstCard() {
		return cards.get(0);
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public double getBet() {
		return bet;
	}
}
