package blackjack;
import java.util.Collections;
import java.util.Stack;

public class Deck {
	private Stack<Card> cards;
	
	
	public Deck(){
		createDeck();
		shuffleDeck();
	}
	
	public void createDeck()
	{
		cards = new Stack<Card>();
		for(int deck_amount = 0; deck_amount < 6; deck_amount++ )
		{
			for(int card_value = 1; card_value < 15; card_value++)
			{
				for(int card_suit = 0; card_suit < 4; card_suit++)
				{
					Card card = new Card(card_value, card_suit);
					cards.push(card);
				}
			}
		}
	}
	
	public void shuffleDeck()
	{
		for(int times = 0; times < 10; times++)
		{
			Collections.shuffle(cards);
		}
	}
	
	public void printDeck()
	{
		Stack<Card> aux = cards;
		while(aux.empty() == false)
		{
			Card peekCard = aux.peek();
			System.out.println("Carta: " + peekCard.get_rank() + peekCard.get_suit());
			aux.pop();
		}
	}
}
