package main.java.de.louisbock.BlackJack;

import java.util.Collections;
import java.util.LinkedList;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class TableLogic {
	
	private final DataBank DB;
	private final int SHOE_SIZE;
	private final int SHUFFLE_LIMIT;
	private LinkedList<DeckCards> shoe;
	private Player dealer;
	private Hand dealersHand;
	private LinkedList<Hand> playerHands;
	private int currentPlayerIndex = 0;
	private int playerCount = 0;
	private int cardCount = 0;
	
	public TableLogic(int shoeSize) {
		DB = DataBank.getInstance();
		SHOE_SIZE = shoeSize;
		SHUFFLE_LIMIT = (int) Math.ceil((shoeSize*52)*0.25);
		
		//hands
		dealer = DB.loginPlayer("Dealer");
		dealersHand = new Hand(dealer, false);
		playerHands = new LinkedList<Hand>();
		shoe = new LinkedList<DeckCards>();
	}
	
	public boolean addPlayer(String name) {
		if(name.length()<1 || name.length()>16) return false;
		if(playerCount >= Table.getMaxPlayers()) return false;
		String[] names = getPlayerNames();
		for(int i = 0; i < names.length; i++) {
			if(name.equals(names[i])) return true;
		}
		playerHands.add(new Hand(DB.loginPlayer(name), false));
		playerCount++;
		return true;
	}
	
	public void removePlayer(String name) {
		Hand hand = null;
		for(Hand h : playerHands) {
			if(name.equals(h.getPlayer().getName())) {
				hand = h;
				break;
			}
		}
		if(hand != null) {
			playerHands.remove(hand);
			playerCount--;
		}
	}
	
	private void shuffleShoe() {
		cardCount = 0;
		shoe.removeAll(shoe);
		for(int i = 0; i < SHOE_SIZE*4; i++) {
			for(DeckCards c: DeckCards.values()) {
				if(c != DeckCards.ONE) shoe.add(c);
			}
		}
		Collections.shuffle(shoe);
	}
	
	private DeckCards drawCard() {
		try {
			DeckCards card = shoe.pop();
			cardCount += card.getCountingValue();
			return card;
		}catch (Exception e) {
			System.err.println("Attempted to draw from an empty Shoe!");
			System.exit(1);
		}
		return null;
	}
	
	public void startRound() {
		//reset Cards
		if(shoe.size() < SHUFFLE_LIMIT) shuffleShoe();
		
		//reset Hands
		currentPlayerIndex = 0;
		
		//reset cards and first card
		dealersHand.discardCards();
		dealersHand.addCard(drawCard());
		for(Hand hand : playerHands) {
			hand.discardCards();
			hand.addCard(drawCard());
		}
		//second card
		dealersHand.addCard(drawCard());
		for(Hand hand : playerHands) {
			hand.addCard(drawCard());
		}
	}
	
	public void clearSplitHands() {
		var removals = playerHands.stream().filter((d)->{return d.isSplitHand();}).toArray();
		for(Object hand : removals) {
			playerHands.remove(hand);
			playerCount--;
		}
	}
	
	/**
	 * prepares the next person and returns false if all players including the dealer have played
	 * @return was there a next person?
	 */
	public boolean nextPerson() {
		if(currentPlayerIndex >= playerCount-1) {//dealer
			currentPlayerIndex = 0;
//			currentPlayerIndex++; TODO check then delete
			return false;
		}else {//player
			currentPlayerIndex++;
			return true;
		}
	}
	
	public void payBet(double bet) {
		playerHands.get(currentPlayerIndex).payBet(bet);
	}
	
	public void playDealersHand() {
		while(dealersHand.getValue() > -1 && (dealersHand.getValue() < 17 || dealersHand.getValue() == 17  && dealersHand.hasCard(DeckCards.ACE))) {
			dealersHand.addCard(drawCard());
		}
	}
	
	/**
	 * tries to draw card and returns whether the hand had overflow or not(true: still playable)
	 * @return hitable?
	 */
	public boolean playerDrawCard() {
		if(currentPlayerIndex >= playerCount)return false;
		Hand hand = playerHands.get(currentPlayerIndex);
		if(!getHitable()) {
			return false;
		}
		hand.addCard(drawCard());
		return getHitable();
	}
	
	public void playerDoubleHand() {
		playerHands.get(currentPlayerIndex).doubleBet();
		playerDrawCard();
	}

	public void playerSplitHand() {
		Hand hand = playerHands.get(currentPlayerIndex);
		Hand split = new Hand(DB.loginPlayer(hand.getPlayer().getName()), true);
		playerHands.add(currentPlayerIndex + 1, split);
		playerCount++;
		
		DeckCards card = hand.getFirstCard();
		if(card.equals(DeckCards.ONE)) card = DeckCards.ACE;
		hand.discardCards();
		hand.addCard(card);
		hand.addCard(drawCard());
		split.addCard(card);
		split.addCard(drawCard());
	}
	
	public void evaluateRound() {
		int dealerValue = dealersHand.getValue();
		for(Hand hand : playerHands) {
			hand.calculatePrizeFactor(dealerValue);
		}
		DB.updateDB();
	}
	
//	private void printHands() {
//		if(dealersHand == null) {
//			System.out.println("empty hand");
//		}else {
//			dealersHand.printHand();
//		}
//		
//		if(playerHands == null) {
//			System.out.println("empty hands");
//		}else {
//			for(Hand hand : playerHands) {
//				if(hand == null) {
//					System.out.println("empty hand");
//				}else {
//					hand.printHand();
//				}
//			}
//		}
//	}
	
//	private void printShoe() {
//		for(DeckCards c: shoe) {
//			System.out.print(c + " ");
//		}
//		System.out.println("");
//	}
	
	public Pane getDealerBox(boolean hidden) {
		if(hidden) {
			Pane box = dealersHand.getHandBox();
			for(int i = 1; i < box.getChildren().size(); i++) {
				box.getChildren().remove(i);
				box.getChildren().add(new ImageView(Table.getImages()[13]));
			}
			return box;
		}else {
			return dealersHand.getHandBox();
		}
	}
	
	public Pane[] getPlayerBoxes() {
		Pane[] handBoxes = new Pane[playerCount];
		for(int i = 0; i < playerCount; i++) {
			handBoxes[i] = playerHands.get(i).getHandBox();
		}
		return handBoxes;
	}
	
	public String[] getPlayerNames() {
		String[] names = new String[playerCount];
		for(int i = 0; i < names.length; i++) {
			names[i] = playerHands.get(i).getPlayer().getName();
		}
		return names;
	}
	
	public double[] getPlayerBets() {
		double[] bets = new double[playerCount];
		for(int i = 0; i < bets.length; i++) {
			bets[i] = playerHands.get(i).getBet();
		}
		return bets;
	}
	
	public Label[] getPlayerLabels() {
		Label[] labels = new Label[playerCount];
		for(int i = 0; i < labels.length; i++) {
			labels[i] = playerHands.get(i).getPlayerLabel();
		}
		return labels;
	}
	
	public double getPlayerMoney() {
		if(currentPlayerIndex >= playerCount)return -1;
		return playerHands.get(currentPlayerIndex).getPlayer().getMoney();
	}
	
	public boolean getHitable() {
		if(currentPlayerIndex >= playerCount)return false;
		return playerHands.get(currentPlayerIndex).getHitable();
	}
	
	public boolean getDoubleable() {
		if(currentPlayerIndex >= playerCount)return false;
		return playerHands.get(currentPlayerIndex).getDoubleable();
	}
	
	public boolean getSplitable() {
		if(currentPlayerIndex >= playerCount)return false;
		return playerHands.get(currentPlayerIndex).getSplitable();
	}
	
	public int getPlayerCount() {
		return playerCount;
	}
	
	public int getShoeCount() {
		return shoe.size();
	}
	
	public int getCardCount() {
		return cardCount;
	}

	public int getCurrentPlayerIndex() {
		return currentPlayerIndex;
	}
}
