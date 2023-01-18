
package main.java.de.louisbock.BlackJack;

public class Player {
	
	private final String NAME;
	private double money;
	
	public Player(String name) {
		this.NAME = name;
		this.money = 100;
	}
	
	public void addMoney(double amount) {
		money += amount;
	}
	
	public void subMoney(double amount) {
		money -= amount;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(o == this) return true;
		if(o.getClass() != this.getClass()) return false;
		Player other = (Player)o;
		return this.NAME.equals(other.getName());
	}
	
	public String getName() {
		return NAME;
	}
	
	public double getMoney() {
		return money;
	}
}
