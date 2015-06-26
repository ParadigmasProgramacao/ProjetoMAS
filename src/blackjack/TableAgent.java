package blackjack;
/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

//package examples.bookTrading;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class TableAgent extends Agent {

	private static final long serialVersionUID = 1L;
	private int seats;
	private Deck deck;
	private List<Card> cards;
	private List<AID> players;
	private boolean onGame = false;
	

	// Put agent initializations here
	protected void setup() {
		

		// Register the dealer service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("blackjack");
		sd.setName("table");
		dfd.addServices(sd);	
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		cards = new ArrayList<Card>();
		players = new ArrayList<AID>();
		deck = new Deck();
		
		addBehaviour(new newPlayer());
		addBehaviour(new Game());
	}
	
	private class GiveCard extends OneShotBehaviour {
		private static final long serialVersionUID = 1L;
		private Card card;
		private AID player;
		
		public GiveCard(AID player, Card card){
			this.card = card;
			this.player = player;
		}
		
		public void action() {
			System.out.println("Distribuindo carta " + card.get_rank()+card.get_suit() + " para o " + player.getLocalName());
		}
	}
	
	private class Turno extends SequentialBehaviour {
		private static final long serialVersionUID = 1L;
		AID player;
		
		public Turno(AID player)
		{
			this.player = player;
		}
		
		public int onEnd(){
			System.out.println("turno do " + player.getLocalName());
			//informar ao player se ele quer outra carta ou parar o jogo.
			ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
			msg.addReceiver(player);
			msg.setContent("another-card");
			msg.setConversationId("another-card");
			msg.setReplyWith("msg"+System.currentTimeMillis());
			myAgent.send(msg);
			
			myAgent.waitUntilStarted();
			MessageTemplate mt = MessageTemplate.MatchConversationId("another-card");
			ACLMessage reply = myAgent.receive(mt);
			
			if(reply!= null)
			{
				System.out.println("recebendo resposta...");
				if(reply.getPerformative() == ACLMessage.CONFIRM)
				{
					System.out.println("pediu outra carta");
					addBehaviour(new GiveCard(player, deck.cards.pop()));
				}
			}
			
			return super.onEnd();
		}
	}

	
	private class Game extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;
		
		public void action() {
			if(players.size() > 0 && onGame == false)
			{
				onGame = true;
				for(int repeticoes = 0; repeticoes < 2; repeticoes ++)
				{
					for(int qntPlayer = 0; qntPlayer < players.size(); qntPlayer++)
					{
						addBehaviour(new GiveCard(players.get(qntPlayer), deck.cards.pop()));
					}
				}
				addBehaviour(new GiveCard(myAgent.getAID(), deck.cards.pop()));
				
				for(int qntPlayer = 0; qntPlayer < players.size(); qntPlayer++)
				{
					addBehaviour(new Turno(players.get(qntPlayer)));
				}
			}
		}
	}
	
	private class ListPlayers extends OneShotBehaviour {
		private static final long serialVersionUID = 1L;
		
		public void action() {
			System.out.println("listando os players: ");
			for(int i = 0; i < players.size(); i++)
			{
				System.out.println(players.get(i).getLocalName());
			}
		}
	}
	
	private class newPlayer extends CyclicBehaviour {
		
		private static final long serialVersionUID = 1L;
		
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP), 
					MessageTemplate.MatchConversationId("place-request"));
			ACLMessage msg = myAgent.receive(mt);
			
			if(msg != null)
			{
				//ACLMessage reply = msg.createReply();
				//reply.setContent(msg.getContent());
				System.out.println("Recebendo mensagem... " + msg.getContent());
				System.out.println("Numero de jogadores: " + players.size());
				ACLMessage reply = msg.createReply();
				if(players.size() < 5)
				{
					reply.setPerformative(ACLMessage.CONFIRM);
					reply.setContent("available");
				}
				else
				{
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
				
			}
			else {
				MessageTemplate mt2 = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), 
						MessageTemplate.MatchConversationId("occupying-place"));
				ACLMessage newReply = myAgent.receive(mt2);
				
				if(newReply != null)
				{
					players.add(newReply.getSender());
					addBehaviour(new ListPlayers());
				}
				else
				{
					block();
				}
			}
		}
	}
	
	
	// Put agent clean-up operations here
		protected void takeDown() {
			// Deregister from the yellow pages
			try {
				DFService.deregister(this);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}

			// Printout a dismissal message
			System.out.println("Table-agent "+getAID().getName()+" terminating.");
		}
}

