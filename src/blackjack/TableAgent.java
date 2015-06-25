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
	private DealerAgent dealer;
	private List<PlayerAgent> players;
	

	// Put agent initializations here
	protected void setup() {
		// Create the catalogue

		// Create and show the GUI 
		//myGui = new DealerGui(this);
		//myGui.showGui();

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
		
		addBehaviour(new newPlayer());
	}
	
	private class newPlayer extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;
		
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null)
			{
				System.out.println("Recebendo mesnsagem... " + msg.getContent());
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

