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

	// Put agent initializations here
	protected void setup() {

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
					}
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		} );
		
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
				System.out.println("agente: " + agents[i].getName());
			}
			myAgent.send(cfp);
			mt = MessageTemplate.and(MessageTemplate.MatchConversationId("place-request"),
					MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
			ACLMessage reply = myAgent.receive(mt);
			if (reply != null) {
				// Recebeu a resposta
				if (reply.getPerformative() == ACLMessage.CONFIRM) {
					//ta falando que tem lugar
					System.out.println(reply.getSender() + " tem lugar vazio");
				}
				else
				{
					System.out.println(reply.getSender()+ "nao tem lugar vazio");
				}
			}
		}
	}

	/**
	   Inner class OfferRequestsServer.
	   This is the behaviour used by Book-seller agents to serve incoming requests 
	   for offer from buyer agents.
	   If the requested book is in the local catalogue the seller agent replies 
	   with a PROPOSE message specifying the price. Otherwise a REFUSE message is
	   sent back.
	 */
	// Servidor de Requisi��es de Ofertas
	//FIPA PROTOCOLS: http://www.fipa.org/specs/fipa00030/
	
	private class findTable extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;
		private MessageTemplate mt;

		public void action() {
			// Update the list of seller agents
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("blackjack");
			sd.setName("table");
			template.addServices(sd);
			try {
				DFAgentDescription[] result = DFService.search(myAgent, template); 
				agents = new AID[result.length];
				
				if(agents.length > 0)
				{
					System.out.println(myAgent.getLocalName() + " found a table");
					
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
					cfp.setContent("place");
					cfp.setConversationId("place-request");
					cfp.setReplyWith("cfp"+System.currentTimeMillis());
					//Enviar mensagem para as mesas ver se possui lugar para sentar
					for(int i = 0; i < agents.length ; i++)
					{
						cfp.addReceiver(agents[i]);
						myAgent.send(cfp);
						cfp.removeReceiver(agents[i]);
					}
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("place-request"),
							MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
					ACLMessage reply = myAgent.receive(mt);
					if (reply != null) {
						// Recebeu a resposta
						if (reply.getPerformative() == ACLMessage.CONFIRM) {
							//ta falando que tem lugar
							System.out.println(reply.getSender() + " tem lugar vazio");
						}
					}
				}
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
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
			// Close the GUI
			// Printout a dismissal message
			System.out.println("player "+getAID().getName()+" terminating.");
		}
}

