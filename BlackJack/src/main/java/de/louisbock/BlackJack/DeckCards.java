package main.java.de.louisbock.BlackJack;

public enum DeckCards {
	ONE(1, 12, -1), 
	TWO(2, 0, 1), 
	THREE(3, 1, 1), 
	FOUR(4, 2, 1), 
	FIVE(5, 3, 1), 
	SIX(6, 4, 1), 
	SEVEN(7, 5, 0), 
	EIGHT(8, 6, 0), 
	NINE(9, 7, 0), 
	TEN(10, 8, -1), 
	JACK(10, 9, -1), 
	QUEEN(10, 10, -1), 
	KING(10, 11, -1), 
	ACE(11, 12, -1);
	
	private int value;
	private int imageIndex;
	private int countingValue;
	
	DeckCards(int value, int imageIndex, int countingValue) {
		this.value = value;
		this.imageIndex = imageIndex;
		this.countingValue = countingValue;
	}

	public int getValue() {
		return value;
	}
	
	public int getImageIndex() {
		return imageIndex;
	}
	
	public int getCountingValue() {
		return countingValue;
	}
}
