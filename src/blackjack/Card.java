package blackjack;

public class Card {
	private final String[] ranks = {"A", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
	private final String[] suits = {"Ouros", "Espadas", "Copas", "Paus"};
	
	private int value;
	private String rank;
	private String suit;
	
	public Card(int value, int suit_value)
	{
		this.set_value(value);
		this.set_rank(value);
		this.set_suit(suit_value);
	}
	
	public int get_value()
	{
		return this.value;
	}
	
	public String get_rank()
	{
		return this.rank;
	}
	
	public String get_suit()
	{
		return this.suit;
	}
	
	private void set_value(int value)
	{
		this.value = value;
	}
	
	private void set_rank(int value)
	{
		this.rank = ranks[value-1];
	}
	
	private void set_suit(int suit_value)
	{
		this.suit = suits[suit_value];
	}
	

}
