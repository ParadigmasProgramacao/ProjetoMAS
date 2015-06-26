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

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.DFSubscriber;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;





import java.util.*;


public class PlayerAgent extends Agent {
	
	private static final long serialVersionUID = 1L;
	private AID[] agents;
	private AID table;
	private List<Card> hand;
	private PlayerGui myGui;
	private PlayerAgent myself = this;

	// Put agent initializations here
	protected void setup() {
		table = new AID();
		table.setLocalName("none");
		
		hand = new ArrayList<Card>();

		// Register the dealer service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("blackjack");
		sd.setName("player");
		dfd.addServices(sd);	
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		
		
		//addBehaviour(new findTable());
		
		addBehaviour(new TickerBehaviour(this, 5000) {

			private static final long serialVersionUID = 1L;
			private MessageTemplate mt;

			protected void onTick() {
				// Update the list of seller agents
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("blackjack");
				sd.setName("table");
				template.addServices(sd);
				
				if(!table.getLocalName().equals("none"))
				{
					return;
				}
				
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template); 
					agents = new AID[result.length];
					
					for (int i = 0; i < result.length; ++i) {
						agents[i] = result[i].getName();
						System.out.println(agents[i].getName());
					}
					
					if(agents.length > 0)
					{
						System.out.println(myAgent.getLocalName() + " found a table ");
						myAgent.addBehaviour(new RequestSeat());
						myAgent.addBehaviour(new CanSit());
					}
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		} );
	
	}
	public void updateCatalogue(final String title, final int price) {
		addBehaviour(new OneShotBehaviour() {

			private static final long serialVersionUID = 1L;

			public void action() {
				//catalogue.put(title, new Integer(price));
				System.out.println(title+" inserted into catalogue. Price = "+price);
			}
		} );
	}
	
	public String resposta(final String resp) {
		return resp;
	}
	
	private class MyTurn extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;
		private MessageTemplate mt;
		
		public void action() {
			mt = MessageTemplate.MatchConversationId("another-card");
			ACLMessage msg = myAgent.receive(mt);
			
			if (msg != null) {
				if(msg.getPerformative() == ACLMessage.PROPOSE)
				{
					System.out.println("recebeu proposta, pediu nova carta");
					myGui = new PlayerGui(myself);
					myGui.showGui();
					
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.CONFIRM);
					//sreply.setContent(opcao);
					myAgent.send(reply);
				}
			}
		}
	}
	
	private class CanSit extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;
		private MessageTemplate mt;
		
		public void action() {
			mt = MessageTemplate.MatchConversationId("place-request");
			ACLMessage reply = myAgent.receive(mt);
			if (reply != null) {
				// Recebeu a resposta
				if (reply.getPerformative() == ACLMessage.CONFIRM) {
					//ta falando que tem lugar
					System.out.println(reply.getSender().getLocalName() + " tem lugar vazio, Sentando na Mesa");
					table = reply.getSender();
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.addReceiver(reply.getSender());
					msg.setContent("is_sitting");
					msg.setConversationId("occupying-place");
					msg.setReplyWith("msg"+System.currentTimeMillis());
					myAgent.send(msg);
					
					addBehaviour(new MyTurn());
				}
			}
			else
			{
				block();
			}
		}
	}
	
	private class RequestSeat extends OneShotBehaviour {
		
		private static final long serialVersionUID = 1L;
		private MessageTemplate mt;
		
		public void action() {
			ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
			cfp.setContent("place");
			cfp.setConversationId("place-request");
			cfp.setReplyWith("cfp"+System.currentTimeMillis());
			//Enviar mensagem para as mesas ver se possui lugar para sentar
			for(int i = 0; i < agents.length ; i++)
			{
				cfp.addReceiver(agents[i]);
			}
			myAgent.send(cfp);
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
			// Close the GUI
			// Printout a dismissal message
			System.out.println("player "+getAID().getName()+" terminating.");
		}
}

